package io.github.vnquynh810.fitflow.core.domain.usecase

import io.github.vnquynh810.fitflow.core.domain.repository.ExerciseRepository
import io.github.vnquynh810.fitflow.core.domain.repository.WorkoutPlanRepository
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GenerateWorkoutPlanUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
) {
    suspend fun buildEnrichedBasePlan(goal: FitnessGoal): List<DayPlan> {
        exerciseRepository.ensureSeeded()
        val rawPlan = workoutPlanRepository.loadBasePlanForGoal(goal)
        return rawPlan.map { dayPlan ->
            if (dayPlan.isRest) {
                dayPlan
            } else {
                val enriched = dayPlan.workoutExercises.map { ex ->
                    val resolvedGif = ex.gifFileName.ifBlank {
                        exerciseRepository.getGifFileName(ex.name).orEmpty()
                    }
                    ex.copy(gifFileName = resolvedGif)
                }
                dayPlan.copy(workoutExercises = enriched)
            }
        }
    }

    fun observeMergedPlan(goal: FitnessGoal): Flow<List<DayPlan>> =
        workoutPlanRepository.observeCustomDayPlans(goal).map { customMap ->
            val base = buildEnrichedBasePlan(goal)
            base.map { day ->
                val customExercises = customMap[day.dayNumber]
                if (customExercises != null) {
                    val enrichedCustom = customExercises.map { ex ->
                        val resolvedGif = ex.gifFileName.ifBlank {
                            exerciseRepository.getGifFileName(ex.name).orEmpty()
                        }
                        ex.copy(gifFileName = resolvedGif)
                    }
                    day.copy(workoutExercises = enrichedCustom)
                } else {
                    day
                }
            }
        }
}
