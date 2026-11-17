package io.github.vnquynh810.fitflow.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class StreakAchievedUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completedDates: Set<LocalDate> = emptySet(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StreakAchievedViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    private val goalCompletedDatesFlow = userPreferencesRepository.userProfileFlow
        .flatMapLatest { profile ->
            val goal: FitnessGoal? = profile?.goal
            if (goal == null) {
                flowOf(emptySet())
            } else {
                progressRepository.observeGoalProgress(goal).flatMapLatest { summary ->
                    flowOf(summary.completedDateMap.keys)
                }
            }
        }

    val uiState: StateFlow<StreakAchievedUiState> = combine(
        progressRepository.observeStreakInfo(),
        goalCompletedDatesFlow,
        progressRepository.observeWorkoutLogs(),
    ) { streakInfo, goalDates, logs ->
        val logDates = logs.filter { !it.isRest }.map { LocalDate.ofEpochDay(it.dateEpochDay) }.toSet()
        StreakAchievedUiState(
            currentStreak = streakInfo.currentStreak,
            longestStreak = streakInfo.longestStreak,
            completedDates = goalDates + logDates,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StreakAchievedUiState(),
    )
}
