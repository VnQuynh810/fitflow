package io.github.vnquynh810.fitflow.feature.onboarding

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.repository.WorkoutPlanRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.GenerateWorkoutPlanUseCase
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.platform.GifUrlProvider
import io.github.vnquynh810.fitflow.core.platform.NetworkMonitor
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class PlanProvisioningState(
    val firstSegmentProgress: Float = 0f,
    val secondSegmentProgress: Float = 0f,
    val isCompleted: Boolean = false,
    val requiresMobileDataConsent: Boolean = false,
    val isNoNetwork: Boolean = false,
    val isInProgress: Boolean = false,
    val hasError: Boolean = false,
    val statusMessage: String = "Creating your plan...",
)

@HiltViewModel
class ProvisioningViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val gifUrlProvider: GifUrlProvider,
    private val networkMonitor: NetworkMonitor,
    private val imageLoader: ImageLoader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanProvisioningState())
    val uiState: StateFlow<PlanProvisioningState> = _uiState.asStateFlow()

    private var provisioningJob: Job? = null
    private var pendingGoal: FitnessGoal? = null
    private var pendingRemoteUrls: List<String> = emptyList()
    private var mobileDataConsentGranted = false

    fun startProvisioning(goalName: String) {
        val goal = runCatching { FitnessGoal.valueOf(goalName) }
            .getOrDefault(FitnessGoal.WEIGHT_LOSS)

        if (provisioningJob?.isActive == true && pendingGoal == goal) return
        if (_uiState.value.isCompleted && pendingGoal == goal) return

        pendingGoal = goal
        mobileDataConsentGranted = false
        pendingRemoteUrls = emptyList()

        _uiState.value = PlanProvisioningState(
            firstSegmentProgress = 0f,
            secondSegmentProgress = 0f,
            isCompleted = false,
            isInProgress = true,
            statusMessage = "Creating your plan...",
        )

        provisioningJob = viewModelScope.launch {
            runCatching {
                preferencesRepository.markGifCacheReady(goal.name, ready = false)
                preferencesRepository.updateGoal(goal)
                workoutPlanRepository.clearCustomDayPlans(goal)

                val generatedPlan = generateWorkoutPlanUseCase.buildEnrichedBasePlan(goal)
                _uiState.update {
                    it.copy(
                        firstSegmentProgress = 1f,
                        statusMessage = "Preparing resources...",
                    )
                }

                val remoteUrls = collectUnbundledFirstWeekGifUrls(generatedPlan)
                pendingRemoteUrls = remoteUrls

                if (remoteUrls.isEmpty()) {
                    completeProvisioning(goal.name)
                    return@launch
                }

                handlePrefetchGate(goal, remoteUrls)
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isInProgress = false,
                        hasError = true,
                        statusMessage = "Unable to finish setup. Please retry.",
                    )
                }
            }
        }
    }

    fun confirmMobileData() {
        val urls = pendingRemoteUrls
        val goal = pendingGoal ?: return
        if (urls.isEmpty()) return

        mobileDataConsentGranted = true
        provisioningJob = viewModelScope.launch {
            runCatching {
                handlePrefetchGate(goal, urls)
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isInProgress = false,
                        hasError = true,
                        statusMessage = "Unable to finish setup. Please retry.",
                    )
                }
            }
        }
    }

    fun retry() {
        if (provisioningJob?.isActive == true) return
        val goal = pendingGoal ?: FitnessGoal.WEIGHT_LOSS
        val urls = pendingRemoteUrls

        if (urls.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    hasError = false,
                    isNoNetwork = false,
                    isInProgress = true,
                    statusMessage = "Retrying setup...",
                )
            }
            provisioningJob = viewModelScope.launch {
                runCatching {
                    handlePrefetchGate(goal, urls)
                }.onFailure {
                    _uiState.update {
                        it.copy(
                            isInProgress = false,
                            hasError = true,
                            statusMessage = "Unable to finish setup. Please retry.",
                        )
                    }
                }
            }
        } else {
            startProvisioning(goal.name)
        }
    }

    private suspend fun handlePrefetchGate(goal: FitnessGoal, remoteUrls: List<String>) {
        val network = networkMonitor.currentSnapshot()
        when {
            !network.isConnected -> {
                _uiState.update {
                    it.copy(
                        isInProgress = false,
                        isNoNetwork = true,
                        requiresMobileDataConsent = false,
                        statusMessage = "Connect to Wi-Fi or mobile data to continue setup.",
                    )
                }
            }
            network.isCellular && !network.isWifi && !mobileDataConsentGranted -> {
                _uiState.update {
                    it.copy(
                        isInProgress = false,
                        isNoNetwork = false,
                        requiresMobileDataConsent = true,
                        statusMessage = "Using mobile data may consume data. Continue?",
                    )
                }
            }
            else -> {
                runPrefetch(goal.name, remoteUrls)
            }
        }
    }

    private suspend fun runPrefetch(goalSignature: String, urls: List<String>) {
        _uiState.update {
            it.copy(
                isInProgress = true,
                isNoNetwork = false,
                requiresMobileDataConsent = false,
                hasError = false,
                secondSegmentProgress = 0f,
                statusMessage = "Finalizing setup...",
            )
        }

        val total = urls.size.coerceAtLeast(1)
        withContext(Dispatchers.IO) {
            urls.forEachIndexed { index, url ->
                runCatching {
                    imageLoader.execute(
                        ImageRequest.Builder(context)
                            .data(url)
                            .memoryCacheKey(url)
                            .diskCacheKey(url)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()
                    )
                }
                val progress = (index + 1) / total.toFloat()
                _uiState.update { it.copy(secondSegmentProgress = progress) }
            }
        }

        completeProvisioning(goalSignature)
    }

    private suspend fun completeProvisioning(goalSignature: String) {
        preferencesRepository.markGifCacheReady(goalSignature, ready = true)
        pendingRemoteUrls = emptyList()
        _uiState.update {
            it.copy(
                firstSegmentProgress = 1f,
                secondSegmentProgress = 1f,
                isInProgress = false,
                isCompleted = true,
                isNoNetwork = false,
                requiresMobileDataConsent = false,
                hasError = false,
                statusMessage = "Setup complete",
            )
        }
    }

    private fun collectUnbundledFirstWeekGifUrls(plan: List<DayPlan>): List<String> {
        return plan.asSequence()
            .filter { !it.isRest }
            .take(7)
            .flatMap { it.workoutExercises.asSequence() }
            .map { it.gifFileName.trim() }
            .filter { it.isNotEmpty() && !gifUrlProvider.isBundledLocally(it) }
            .map { gifUrlProvider.resolve(it) }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()
    }
}
