package io.github.vnquynh810.fitflow.core.data

import io.github.vnquynh810.fitflow.core.database.GoalDayProgressEntity
import io.github.vnquynh810.fitflow.core.database.ProgressDao
import io.github.vnquynh810.fitflow.core.database.WorkoutLogEntity
import io.github.vnquynh810.fitflow.core.datastore.FitFlowPreferencesDataSource
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.StreakCalculator
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.GoalProgressSummary
import io.github.vnquynh810.fitflow.core.model.StreakInfo
import io.github.vnquynh810.fitflow.core.model.WorkoutLogEntry
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao,
    private val preferencesDataSource: FitFlowPreferencesDataSource,
) : ProgressRepository {

    override fun observeGoalProgress(goal: FitnessGoal): Flow<GoalProgressSummary> =
        progressDao.observeProgressForGoal(goal.name).map { rows ->
            val completedDays = mutableSetOf<Int>()
            val completedDateMap = mutableMapOf<LocalDate, Int>()
            val partialMap = mutableMapOf<Int, Int>()

            rows.forEach { row ->
                if (row.isCompleted) {
                    completedDays.add(row.dayNumber)
                    row.completedEpochDay?.let { epoch ->
                        completedDateMap[LocalDate.ofEpochDay(epoch)] = row.dayNumber
                    }
                } else if (row.partialExerciseIndex != null) {
                    partialMap[row.dayNumber] = row.partialExerciseIndex
                }
            }

            GoalProgressSummary(
                completedDays = completedDays,
                completedDateMap = completedDateMap,
                partialProgressMap = partialMap,
            )
        }

    override fun observeWorkoutLogs(): Flow<List<WorkoutLogEntry>> =
        progressDao.observeWorkoutLogs().map { list -> list.map { it.toDomain() } }

    override fun observeStreakInfo(): Flow<StreakInfo> =
        preferencesDataSource.streakInfoFlow

    override suspend fun getCompletedCountForGoal(goal: FitnessGoal): Int =
        progressDao.countCompletedDays(goal.name)

    override suspend fun savePartialProgress(
        goal: FitnessGoal,
        dayNumber: Int,
        exerciseIndex: Int,
    ) {
        val existing = progressDao.getDayProgress(goal.name, dayNumber)
        if (existing?.isCompleted == true) return
        progressDao.upsertProgress(
            GoalDayProgressEntity(
                goalName = goal.name,
                dayNumber = dayNumber,
                isCompleted = false,
                partialExerciseIndex = exerciseIndex,
            )
        )
    }

    override suspend fun clearPartialProgress(goal: FitnessGoal, dayNumber: Int) {
        val existing = progressDao.getDayProgress(goal.name, dayNumber) ?: return
        if (!existing.isCompleted) {
            progressDao.upsertProgress(existing.copy(partialExerciseIndex = null))
        }
    }

    override suspend fun clearProgressForGoal(goal: FitnessGoal) {
        progressDao.deleteProgressForGoal(goal.name)
    }

    override suspend fun recordWorkoutCompletion(
        goal: FitnessGoal?,
        dayNumber: Int,
        completedDate: LocalDate,
        timestampMillis: Long,
        durationSec: Int,
        kcal: Int,
        isRest: Boolean,
        supplementaryTitle: String?,
    ): Boolean {
        val goalLabel = supplementaryTitle ?: goal?.name ?: "WORKOUT"
        progressDao.insertWorkoutLog(
            WorkoutLogEntity(
                dateEpochDay = completedDate.toEpochDay(),
                timestampMillis = timestampMillis,
                dayNumber = dayNumber,
                goalName = goalLabel,
                durationSec = durationSec,
                kcal = kcal,
                isRest = isRest,
            )
        )

        if (goal == null || dayNumber <= 0) {
            return false
        }

        val existing = progressDao.getDayProgress(goal.name, dayNumber)
        if (existing?.isCompleted == true) {
            return false
        }

        progressDao.upsertProgress(
            GoalDayProgressEntity(
                goalName = goal.name,
                dayNumber = dayNumber,
                isCompleted = true,
                completedEpochDay = completedDate.toEpochDay(),
                completedTimestampMillis = timestampMillis,
                partialExerciseIndex = null,
            )
        )

        val currentStreak = preferencesDataSource.streakInfoFlow.first()
        val streakResult = StreakCalculator.computeAfterWorkout(currentStreak, completedDate)
        if (streakResult.isNewStreakToday) {
            preferencesDataSource.updateStreak(streakResult.updatedStreak)
        }
        return streakResult.isNewStreakToday
    }
}
