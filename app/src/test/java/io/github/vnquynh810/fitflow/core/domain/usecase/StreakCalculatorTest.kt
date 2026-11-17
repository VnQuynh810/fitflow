package io.github.vnquynh810.fitflow.core.domain.usecase

import io.github.vnquynh810.fitflow.core.model.StreakInfo
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakCalculatorTest {

    @Test
    fun `first ever workout starts streak at 1 and marks new streak`() {
        val date = LocalDate.of(2026, 11, 17)
        val result = StreakCalculator.computeAfterWorkout(StreakInfo(), date)

        assertTrue(result.isNewStreakToday)
        assertEquals(1, result.updatedStreak.currentStreak)
        assertEquals(1, result.updatedStreak.longestStreak)
        assertEquals(date, result.updatedStreak.lastWorkoutDate)
    }

    @Test
    fun `second workout on same day keeps streak unchanged and does not re-trigger celebration`() {
        val date = LocalDate.of(2026, 11, 17)
        val existing = StreakInfo(currentStreak = 4, longestStreak = 6, lastWorkoutDate = date)

        val result = StreakCalculator.computeAfterWorkout(existing, date)

        assertFalse(result.isNewStreakToday)
        assertEquals(4, result.updatedStreak.currentStreak)
        assertEquals(6, result.updatedStreak.longestStreak)
    }

    @Test
    fun `consecutive next day workout increments streak and updates longest record`() {
        val yesterday = LocalDate.of(2026, 11, 16)
        val today = LocalDate.of(2026, 11, 17)
        val existing = StreakInfo(currentStreak = 5, longestStreak = 5, lastWorkoutDate = yesterday)

        val result = StreakCalculator.computeAfterWorkout(existing, today)

        assertTrue(result.isNewStreakToday)
        assertEquals(6, result.updatedStreak.currentStreak)
        assertEquals(6, result.updatedStreak.longestStreak)
        assertEquals(today, result.updatedStreak.lastWorkoutDate)
    }

    @Test
    fun `gap of two or more days resets current streak to 1 while preserving longest record`() {
        val threeDaysAgo = LocalDate.of(2026, 11, 14)
        val today = LocalDate.of(2026, 11, 17)
        val existing = StreakInfo(currentStreak = 9, longestStreak = 12, lastWorkoutDate = threeDaysAgo)

        val result = StreakCalculator.computeAfterWorkout(existing, today)

        assertTrue(result.isNewStreakToday)
        assertEquals(1, result.updatedStreak.currentStreak)
        assertEquals(12, result.updatedStreak.longestStreak)
    }
}
