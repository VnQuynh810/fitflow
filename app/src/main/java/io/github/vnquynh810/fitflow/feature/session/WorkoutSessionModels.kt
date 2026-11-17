package io.github.vnquynh810.fitflow.feature.session

import io.github.vnquynh810.fitflow.core.model.Exercise
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise

internal sealed interface SessionMode {
    data class PlanDay(val goal: FitnessGoal, val dayNumber: Int) : SessionMode
    data class Supplementary(val workoutId: String, val title: String) : SessionMode
}

enum class SessionPhase {
    PREPARING,
    EXERCISING,
    RESTING,
}

data class SessionInstructionDetail(
    val workoutExercise: WorkoutExercise,
    val catalogExercise: Exercise?,
)

sealed interface SessionCompletedEvent {
    data class PlanDayCompleted(
        val dayNumber: Int,
        val activeSeconds: Int,
        val isNewStreak: Boolean,
    ) : SessionCompletedEvent

    data class SupplementaryCompleted(
        val workoutId: String,
        val activeSeconds: Int,
    ) : SessionCompletedEvent
}

data class WorkoutSessionUiState(
    val isLoading: Boolean = true,
    val exercises: List<WorkoutExercise> = emptyList(),
    val gifUrlsByName: Map<String, String> = emptyMap(),
    val currentIndex: Int = 0,
    val phase: SessionPhase = SessionPhase.PREPARING,
    val countdownRemaining: Int = 5,
    val exerciseRemaining: Int = 0,
    val restRemaining: Int = 30,
    val isExerciseRunning: Boolean = false,
    val totalActiveSeconds: Int = 0,
    val showPauseDialog: Boolean = false,
    val showExitDialog: Boolean = false,
    val selectedInstruction: SessionInstructionDetail? = null,
    val isBgMusicEnabled: Boolean = true,
    val isVoiceGuideEnabled: Boolean = true,
) {
    val currentExercise: WorkoutExercise?
        get() = exercises.getOrNull(currentIndex)

    val nextExercise: WorkoutExercise?
        get() = exercises.getOrNull(currentIndex + 1)
}
