package io.github.vnquynh810.fitflow.feature.planner

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.vnquynh810.fitflow.core.domain.repository.ExerciseRepository
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.GenerateWorkoutPlanUseCase
import io.github.vnquynh810.fitflow.core.domain.usecase.GetSupplementaryWorkoutUseCase
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.Exercise
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.SupplementaryWorkout
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise
import io.github.vnquynh810.fitflow.core.platform.GifUrlProvider
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DayDetailTarget {
    data class PlanDay(val dayNumber: Int) : DayDetailTarget
    data class Supplementary(val workoutId: String) : DayDetailTarget
}

data class SelectedWorkoutExerciseDetail(
    val workoutExercise: WorkoutExercise,
    val catalogExercise: Exercise?,
)

data class DayDetailUiState(
    val isLoading: Boolean = true,
    val dayPlan: DayPlan? = null,
    val goal: FitnessGoal? = null,
    val supplementary: SupplementaryWorkout? = null,
    val partialIndex: Int? = null,
    val gifUrlsByFileName: Map<String, String> = emptyMap(),
    val selectedExercise: SelectedWorkoutExerciseDetail? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DayDetailViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val getSupplementaryWorkoutUseCase: GetSupplementaryWorkoutUseCase,
    private val progressRepository: ProgressRepository,
    private val exerciseRepository: ExerciseRepository,
    private val gifUrlProvider: GifUrlProvider,
    private val imageLoader: ImageLoader,
) : ViewModel() {

    private val targetFlow = MutableStateFlow<DayDetailTarget?>(null)
    private val selectedExerciseFlow = MutableStateFlow<SelectedWorkoutExerciseDetail?>(null)

    private val contentFlow = targetFlow
        .flatMapLatest { target ->
            when (target) {
                null -> flowOf(DayDetailUiState(isLoading = true))
                is DayDetailTarget.PlanDay -> observePlanDay(target.dayNumber)
                is DayDetailTarget.Supplementary -> observeSupplementary(target.workoutId)
            }
        }
        .onEach { state ->
            prefetchGifs(state.gifUrlsByFileName.values.toList())
        }

    val uiState: StateFlow<DayDetailUiState> = combine(
        contentFlow,
        selectedExerciseFlow,
    ) { content, selected ->
        content.copy(selectedExercise = selected)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayDetailUiState(),
    )

    fun loadDay(dayNumber: Int) {
        val next = DayDetailTarget.PlanDay(dayNumber)
        if (targetFlow.value != next) {
            selectedExerciseFlow.value = null
            targetFlow.value = next
        }
    }

    fun loadSupplementary(workoutId: String) {
        val next = DayDetailTarget.Supplementary(workoutId)
        if (targetFlow.value != next) {
            selectedExerciseFlow.value = null
            targetFlow.value = next
        }
    }

    fun selectExercise(workoutExercise: WorkoutExercise) {
        viewModelScope.launch {
            val catalog = exerciseRepository.findBestMatchByName(workoutExercise.name)
            selectedExerciseFlow.value = SelectedWorkoutExerciseDetail(
                workoutExercise = workoutExercise,
                catalogExercise = catalog,
            )
        }
    }

    fun clearSelectedExercise() {
        selectedExerciseFlow.value = null
    }

    private fun observePlanDay(dayNumber: Int) =
        userPreferencesRepository.userProfileFlow.flatMapLatest { profile ->
            val goal = profile?.goal ?: FitnessGoal.WEIGHT_LOSS
            combine(
                generateWorkoutPlanUseCase.observeMergedPlan(goal),
                progressRepository.observeGoalProgress(goal),
            ) { plan, progress ->
                val dayPlan = plan.find { it.dayNumber == dayNumber }
                val gifMap = resolveGifMap(dayPlan?.workoutExercises.orEmpty())
                DayDetailUiState(
                    isLoading = false,
                    dayPlan = dayPlan,
                    goal = goal,
                    supplementary = null,
                    partialIndex = progress.partialProgressMap[dayNumber],
                    gifUrlsByFileName = gifMap,
                )
            }
        }

    private fun observeSupplementary(workoutId: String) = flow {
        val workout = getSupplementaryWorkoutUseCase(workoutId)
        val dayPlan = workout?.toDayPlan()
        val gifMap = resolveGifMap(dayPlan?.workoutExercises.orEmpty())
        emit(
            DayDetailUiState(
                isLoading = false,
                dayPlan = dayPlan,
                goal = null,
                supplementary = workout,
                partialIndex = null,
                gifUrlsByFileName = gifMap,
            )
        )
    }

    private fun resolveGifMap(exercises: List<WorkoutExercise>): Map<String, String> =
        exercises
            .mapNotNull { ex ->
                val file = ex.gifFileName.trim()
                if (file.isEmpty()) null else file to gifUrlProvider.resolve(file)
            }
            .toMap()

    private fun prefetchGifs(urls: List<String>) {
        urls.filter { it.isNotBlank() }.take(8).forEach { url ->
            imageLoader.enqueue(
                ImageRequest.Builder(appContext)
                    .data(url)
                    .memoryCacheKey(url)
                    .diskCacheKey(url)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            )
        }
    }
}
