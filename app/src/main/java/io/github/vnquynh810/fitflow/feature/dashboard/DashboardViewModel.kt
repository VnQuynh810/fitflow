package io.github.vnquynh810.fitflow.feature.dashboard

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.ExerciseRepository
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.GenerateWorkoutPlanUseCase
import io.github.vnquynh810.fitflow.core.domain.usecase.SupplementaryWorkoutCatalog
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.Exercise
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.SupplementaryWorkout
import io.github.vnquynh810.fitflow.core.model.WorkoutLogEntry
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Immutable
data class DashboardUiState(
    val activeGoal: FitnessGoal = FitnessGoal.WEIGHT_LOSS,
    val currentDayPlan: DayPlan? = null,
    val totalWorkoutDays: Int = 0,
    val completedDateMap: Map<LocalDate, Int> = emptyMap(),
    val globalWorkoutLogs: List<WorkoutLogEntry> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val supplementaryWorkouts: List<SupplementaryWorkout> = SupplementaryWorkoutCatalog.all(),
    val librarySearchQuery: String = "",
    val libraryMuscleGroup: String = "ALL",
    val libraryMuscleGroups: List<String> = listOf("ALL"),
    val libraryExercises: List<Exercise> = emptyList(),
)

sealed interface DashboardUiEvent {
    data class UpdateLibrarySearch(val query: String) : DashboardUiEvent
    data class SelectLibraryMuscle(val muscleGroup: String) : DashboardUiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    preferencesRepository: UserPreferencesRepository,
    progressRepository: ProgressRepository,
    exerciseRepository: ExerciseRepository,
    generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
) : ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")
    private val selectedMuscleFlow = MutableStateFlow("ALL")

    private val planAndProgressFlow = preferencesRepository.userProfileFlow.flatMapLatest { profile ->
        val goal = profile?.goal ?: FitnessGoal.WEIGHT_LOSS
        combine(
            generateWorkoutPlanUseCase.observeMergedPlan(goal),
            progressRepository.observeGoalProgress(goal),
        ) { plan, progress ->
            Triple(goal, plan, progress)
        }
    }

    private val libraryDataFlow = combine(
        exerciseRepository.observeAll(),
        searchQueryFlow,
        selectedMuscleFlow,
    ) { exercises, query, muscle ->
        val groups = listOf("ALL") + exercises.flatMap { it.targetMuscles }.distinct().sorted()
        val filtered = exercises.filter { exercise ->
            val muscleMatch = muscle == "ALL" || exercise.targetMuscles.contains(muscle)
            val searchMatch = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
            muscleMatch && searchMatch
        }
        Triple(query, muscle, groups to filtered)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        planAndProgressFlow,
        progressRepository.observeWorkoutLogs(),
        progressRepository.observeStreakInfo(),
        libraryDataFlow,
    ) { (goal, plan, progress), logs, streak, (query, muscle, groupsAndFiltered) ->
        val (groups, filteredExercises) = groupsAndFiltered
        val totalWorkoutDays = plan.count { !it.isRest }
        val nextDayPlan = plan.filter { !it.isRest }
            .firstOrNull { it.dayNumber !in progress.completedDays }

        DashboardUiState(
            activeGoal = goal,
            currentDayPlan = nextDayPlan,
            totalWorkoutDays = totalWorkoutDays,
            completedDateMap = progress.completedDateMap,
            globalWorkoutLogs = logs,
            currentStreak = streak.currentStreak,
            longestStreak = streak.longestStreak,
            librarySearchQuery = query,
            libraryMuscleGroup = muscle,
            libraryMuscleGroups = groups,
            libraryExercises = filteredExercises,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.UpdateLibrarySearch -> {
                searchQueryFlow.update { event.query }
            }
            is DashboardUiEvent.SelectLibraryMuscle -> {
                selectedMuscleFlow.update { event.muscleGroup }
            }
        }
    }
}
