package io.github.vnquynh810.fitflow.core.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutExercise(
    val category: String,
    val name: String,
    val sets: Int,
    val reps: Int,
    val kcal: Int,
    val durationSec: Int,
    val gifFileName: String = "",
    val description: String = "",
)

@Serializable
data class DayPlan(
    val dayNumber: Int,
    val isRest: Boolean,
    val workoutExercises: List<WorkoutExercise>,
    val title: String = "",
    val difficulty: String = "Intermediate",
    val muscleGroup: String = "",
)

data class SupplementaryWorkout(
    val id: String,
    val title: String,
    val subtitle: String,
    val difficulty: String,
    val durationMinutes: Int,
    val muscleGroup: String,
    val gradientStartHex: Long,
    val gradientEndHex: Long,
    val bannerKey: String,
    val mirrorImage: Boolean = true,
    val imageOffsetDp: Int = 0,
    val exercises: List<WorkoutExercise>,
) {
    fun toDayPlan(): DayPlan = DayPlan(
        dayNumber = 0,
        isRest = false,
        workoutExercises = exercises,
        title = title,
        difficulty = difficulty,
        muscleGroup = muscleGroup,
    )
}
