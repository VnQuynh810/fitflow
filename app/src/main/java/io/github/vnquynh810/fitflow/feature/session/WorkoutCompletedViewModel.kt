package io.github.vnquynh810.fitflow.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.HealthRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.GenerateWorkoutPlanUseCase
import io.github.vnquynh810.fitflow.core.domain.usecase.GetSupplementaryWorkoutUseCase
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.UserProfile
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class CompletedRouteKey(
    val dayNumber: Int? = null,
    val supplementaryId: String? = null,
)

data class WorkoutCompletedUiState(
    val isLoading: Boolean = true,
    val dayPlan: DayPlan? = null,
    val userProfile: UserProfile? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutCompletedViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val getSupplementaryWorkoutUseCase: GetSupplementaryWorkoutUseCase,
    private val healthRepository: HealthRepository,
) : ViewModel() {

    private val routeKeyFlow = MutableStateFlow<CompletedRouteKey?>(null)

    private val dayPlanFlow = routeKeyFlow.flatMapLatest { key ->
        when {
            key == null -> flowOf(null)
            !key.supplementaryId.isNullOrBlank() -> flow {
                emit(getSupplementaryWorkoutUseCase(key.supplementaryId)?.toDayPlan())
            }
            key.dayNumber != null -> userPreferencesRepository.userProfileFlow.flatMapLatest { profile ->
                val goal = profile?.goal ?: FitnessGoal.WEIGHT_LOSS
                generateWorkoutPlanUseCase.observeMergedPlan(goal).map { plan ->
                    plan.find { it.dayNumber == key.dayNumber }
                }
            }
            else -> flowOf(null)
        }
    }

    val uiState: StateFlow<WorkoutCompletedUiState> = combine(
        routeKeyFlow,
        dayPlanFlow,
        userPreferencesRepository.userProfileFlow,
    ) { key, dayPlan, profile ->
        WorkoutCompletedUiState(
            isLoading = key == null,
            dayPlan = dayPlan,
            userProfile = profile,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutCompletedUiState(),
    )

    fun initialize(dayNumber: Int?, supplementaryId: String?) {
        val next = CompletedRouteKey(dayNumber = dayNumber, supplementaryId = supplementaryId)
        if (routeKeyFlow.value != next) {
            routeKeyFlow.value = next
        }
    }

    fun saveWeight(weightKg: Float, date: LocalDate) {
        viewModelScope.launch {
            healthRepository.recordWeight(weightKg = weightKg, date = date)
        }
    }
}
