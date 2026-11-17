package io.github.vnquynh810.fitflow.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.GenerateWorkoutPlanUseCase
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.GoalProgressSummary
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlannerUiState(
    val isLoading: Boolean = true,
    val goal: FitnessGoal? = null,
    val goalTitle: String = "Workout Plan",
    val workoutPlan: List<DayPlan> = emptyList(),
    val completedDays: Set<Int> = emptySet(),
    val partialProgressMap: Map<Int, Int> = emptyMap(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    val uiState: StateFlow<PlannerUiState> = userPreferencesRepository.userProfileFlow
        .flatMapLatest { profile ->
            val goal = profile?.goal
            if (goal == null) {
                flowOf(
                    PlannerUiState(
                        isLoading = false,
                        goal = null,
                        goalTitle = "Workout Plan",
                    )
                )
            } else {
                combine(
                    generateWorkoutPlanUseCase.observeMergedPlan(goal),
                    progressRepository.observeGoalProgress(goal),
                ) { plan, progress: GoalProgressSummary ->
                    PlannerUiState(
                        isLoading = false,
                        goal = goal,
                        goalTitle = goal.title,
                        workoutPlan = plan,
                        completedDays = progress.completedDays,
                        partialProgressMap = progress.partialProgressMap,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlannerUiState(),
        )

    fun markRestDayComplete(dayNumber: Int, onCompleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            val goal = uiState.value.goal ?: userPreferencesRepository.userProfileFlow.first()?.goal
            progressRepository.recordWorkoutCompletion(
                goal = goal,
                dayNumber = dayNumber,
                completedDate = LocalDate.now(),
                timestampMillis = System.currentTimeMillis(),
                durationSec = 0,
                kcal = 0,
                isRest = true,
            )
            onCompleted?.invoke()
        }
    }

    fun resetPlan() {
        viewModelScope.launch {
            val goal = uiState.value.goal ?: userPreferencesRepository.userProfileFlow.first()?.goal ?: return@launch
            progressRepository.clearProgressForGoal(goal)
        }
    }
}
