package io.github.vnquynh810.fitflow.core.domain.repository

import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow

interface WorkoutPlanRepository {
    fun observeCustomDayPlans(goal: FitnessGoal): Flow<Map<Int, List<WorkoutExercise>>>
    suspend fun loadBasePlanForGoal(goal: FitnessGoal): List<DayPlan>
    suspend fun saveCustomDayPlan(goal: FitnessGoal, dayNumber: Int, exercises: List<WorkoutExercise>)
    suspend fun clearCustomDayPlans(goal: FitnessGoal)
}
