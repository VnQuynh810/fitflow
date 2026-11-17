package io.github.vnquynh810.fitflow.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.GenerateWorkoutPlanUseCase
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class DaySummaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val workoutPlan: List<DayPlan> = emptyList(),
    val completedDays: Set<Int> = emptySet(),
    val completedDateMap: Map<LocalDate, Int> = emptyMap(),
    val currentStreak: Int = 0,
    val startDate: LocalDate? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DaySummaryViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    private var initializedKey: Pair<Int, Long>? = null
    private val manualSelectedDateFlow = MutableStateFlow<LocalDate?>(null)
    private val routeArgsFlow = MutableStateFlow(1 to -1L)

    private val planAndProgressFlow = userPreferencesRepository.userProfileFlow
        .flatMapLatest { profile ->
            val goal = profile?.goal ?: FitnessGoal.WEIGHT_LOSS
            combine(
                generateWorkoutPlanUseCase.observeMergedPlan(goal),
                progressRepository.observeGoalProgress(goal),
            ) { plan, progress ->
                plan to progress
            }
        }

    val uiState: StateFlow<DaySummaryUiState> = combine(
        routeArgsFlow,
        manualSelectedDateFlow,
        planAndProgressFlow,
        progressRepository.observeStreakInfo(),
        userPreferencesRepository.startDateFlow,
    ) { (dayNumber, epochDay), manualDate, (plan, progress), streakInfo, startDate ->
        val resolvedDate = manualDate ?: when {
            epochDay != -1L && epochDay > 0L -> LocalDate.ofEpochDay(epochDay)
            startDate != null && dayNumber > 0 -> startDate.plusDays((dayNumber - 1).toLong())
            else -> LocalDate.now()
        }
        DaySummaryUiState(
            selectedDate = resolvedDate,
            workoutPlan = plan,
            completedDays = progress.completedDays,
            completedDateMap = progress.completedDateMap,
            currentStreak = streakInfo.currentStreak,
            startDate = startDate,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DaySummaryUiState(),
    )

    fun initialize(dayNumber: Int, epochDay: Long) {
        val key = dayNumber to epochDay
        if (initializedKey != key) {
            initializedKey = key
            manualSelectedDateFlow.value = null
            routeArgsFlow.value = key
        }
    }

    fun selectDate(date: LocalDate) {
        manualSelectedDateFlow.value = date
    }
}
