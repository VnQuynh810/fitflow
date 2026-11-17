package io.github.vnquynh810.fitflow.core.model

import java.time.LocalDate

enum class StepSource {
    SENSOR,
    MANUAL,
}

data class DailyHealthMetrics(
    val date: LocalDate,
    val steps: Int = 0,
    val waterIntakeMl: Int = 0,
    val waterGoalMl: Int = 2000,
    val stepGoal: Int = 6000,
    val stepSource: StepSource = StepSource.MANUAL,
)

data class WeightRecord(
    val date: LocalDate,
    val weightKg: Float,
)

data class WorkoutLogEntry(
    val id: Long = 0L,
    val dateEpochDay: Long,
    val timestampMillis: Long,
    val dayNumber: Int,
    val goalName: String,
    val durationSec: Int,
    val kcal: Int,
    val isRest: Boolean,
)

data class GoalProgressSummary(
    val completedDays: Set<Int> = emptySet(),
    val completedDateMap: Map<LocalDate, Int> = emptyMap(),
    val partialProgressMap: Map<Int, Int> = emptyMap(),
)

data class StreakInfo(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastWorkoutDate: LocalDate? = null,
)

data class WorkoutAudioSettings(
    val isBgMusicEnabled: Boolean = true,
    val bgMusicVolume: Float = 0.5f,
    val isVoiceGuideEnabled: Boolean = true,
    val coachName: String = "James",
    val coachVolume: Float = 0.8f,
    val isSoundEffectEnabled: Boolean = true,
    val autoCounting: String = "Off",
    val restTimer: String = "30s",
    val countdown: String = "5s",
) {
    val restDurationSeconds: Int
        get() = parseSecondsSetting(restTimer, defaultSec = 30)

    val countdownDurationSeconds: Int
        get() = parseSecondsSetting(countdown, defaultSec = 5)

    private fun parseSecondsSetting(raw: String, defaultSec: Int): Int = when {
        raw.equals("Off", ignoreCase = true) -> 0
        raw.endsWith("s", ignoreCase = true) -> raw.dropLast(1).toIntOrNull() ?: defaultSec
        else -> defaultSec
    }
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val imageUriString: String? = null,
)
