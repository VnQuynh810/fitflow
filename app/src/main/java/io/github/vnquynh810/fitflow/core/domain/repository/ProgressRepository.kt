package io.github.vnquynh810.fitflow.core.domain.repository

import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.GoalProgressSummary
import io.github.vnquynh810.fitflow.core.model.StreakInfo
import io.github.vnquynh810.fitflow.core.model.WorkoutLogEntry
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun observeGoalProgress(goal: FitnessGoal): Flow<GoalProgressSummary>
    fun observeWorkoutLogs(): Flow<List<WorkoutLogEntry>>
    fun observeStreakInfo(): Flow<StreakInfo>
    suspend fun getCompletedCountForGoal(goal: FitnessGoal): Int
    suspend fun savePartialProgress(goal: FitnessGoal, dayNumber: Int, exerciseIndex: Int)
    suspend fun clearPartialProgress(goal: FitnessGoal, dayNumber: Int)
    suspend fun clearProgressForGoal(goal: FitnessGoal)
    suspend fun recordWorkoutCompletion(
        goal: FitnessGoal?,
        dayNumber: Int,
        completedDate: LocalDate,
        timestampMillis: Long,
        durationSec: Int,
        kcal: Int,
        isRest: Boolean,
        supplementaryTitle: String? = null,
    ): Boolean
}
