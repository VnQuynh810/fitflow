package io.github.vnquynh810.fitflow.core.domain.usecase

import io.github.vnquynh810.fitflow.core.model.StreakInfo
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object StreakCalculator {
    data class StreakUpdateResult(
        val updatedStreak: StreakInfo,
        val isNewStreakToday: Boolean,
    )

    fun computeAfterWorkout(current: StreakInfo, workoutDate: LocalDate): StreakUpdateResult {
        val lastDate = current.lastWorkoutDate
        if (lastDate == workoutDate) {
            return StreakUpdateResult(updatedStreak = current, isNewStreakToday = false)
        }

        val nextStreak = when {
            lastDate == null -> 1
            ChronoUnit.DAYS.between(lastDate, workoutDate) == 1L -> current.currentStreak + 1
            ChronoUnit.DAYS.between(lastDate, workoutDate) > 1L -> 1
            else -> current.currentStreak.coerceAtLeast(1)
        }

        val nextLongest = maxOf(current.longestStreak, nextStreak)
        return StreakUpdateResult(
            updatedStreak = StreakInfo(
                currentStreak = nextStreak,
                longestStreak = nextLongest,
                lastWorkoutDate = workoutDate,
            ),
            isNewStreakToday = true,
        )
    }
}
