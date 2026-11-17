package io.github.vnquynh810.fitflow.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable
    data object Dashboard : AppRoute

    @Serializable
    data class Onboarding(val isRecalibrate: Boolean = false) : AppRoute

    @Serializable
    data object WorkoutSetup : AppRoute

    @Serializable
    data class Provisioning(val goalName: String) : AppRoute

    @Serializable
    data object Planner : AppRoute

    @Serializable
    data class DayDetail(val dayNumber: Int) : AppRoute

    @Serializable
    data class RestDayDetail(val dayNumber: Int) : AppRoute

    @Serializable
    data class EditPlan(val dayNumber: Int) : AppRoute

    @Serializable
    data class DayWorkoutSummary(val dayNumber: Int, val epochDay: Long = -1L) : AppRoute

    @Serializable
    data class WorkoutSession(val dayNumber: Int, val startIndex: Int = 0) : AppRoute

    @Serializable
    data class StreakAchieved(val dayNumber: Int, val activeSeconds: Int = 0) : AppRoute

    @Serializable
    data class WorkoutCompleted(
        val dayNumber: Int = -1,
        val supplementaryId: String? = null,
        val activeSeconds: Int = 0,
    ) : AppRoute

    @Serializable
    data class SupplementaryDetail(val workoutId: String) : AppRoute

    @Serializable
    data class SupplementarySession(val workoutId: String) : AppRoute

    @Serializable
    data object Library : AppRoute

    @Serializable
    data object Profile : AppRoute

    @Serializable
    data object History : AppRoute

    @Serializable
    data object UpdateGoal : AppRoute

    @Serializable
    data class WorkoutSettings(val inSession: Boolean = false) : AppRoute

    @Serializable
    data object Chatbot : AppRoute
}
