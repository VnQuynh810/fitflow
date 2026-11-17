package io.github.vnquynh810.fitflow.core.domain.usecase

import io.github.vnquynh810.fitflow.core.domain.repository.ExerciseRepository
import io.github.vnquynh810.fitflow.core.domain.repository.WorkoutPlanRepository
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.Exercise
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.WorkoutAudioSettings
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GenerateWorkoutPlanUseCaseTest {

    private class FakeExerciseRepository : ExerciseRepository {
        var seeded = false
        private val catalog = listOf(
            Exercise(
                name = "Burpee",
                difficulty = "intermediate",
                exerciseType = "Cardio",
                logType = "reps",
                targetMuscles = listOf("Full Body"),
                equipment = emptyList(),
                instructions = listOf("Drop into squat and jump."),
                localGifs = listOf("burpee_1.gif"),
            ),
            Exercise(
                name = "Plank",
                difficulty = "beginner",
                exerciseType = "Core",
                logType = "duration",
                targetMuscles = listOf("Abs"),
                equipment = emptyList(),
                instructions = listOf("Hold straight line."),
                localGifs = listOf("plank_1.gif"),
            ),
        )

        override suspend fun ensureSeeded() {
            seeded = true
        }

        override fun observeAll(): Flow<List<Exercise>> = MutableStateFlow(catalog)

        override suspend fun getGifFileName(exerciseName: String): String? =
            findBestMatchByName(exerciseName)?.localGifs?.firstOrNull()

        override suspend fun findBestMatchByName(exerciseName: String): Exercise? =
            catalog.firstOrNull { it.name.equals(exerciseName, ignoreCase = true) }
    }

    private class FakeWorkoutPlanRepository : WorkoutPlanRepository {
        val customFlow = MutableStateFlow<Map<Int, List<WorkoutExercise>>>(emptyMap())

        override fun observeCustomDayPlans(goal: FitnessGoal): Flow<Map<Int, List<WorkoutExercise>>> =
            customFlow

        override suspend fun loadBasePlanForGoal(goal: FitnessGoal): List<DayPlan> = listOf(
            DayPlan(
                dayNumber = 1,
                isRest = false,
                title = "Day 1",
                workoutExercises = listOf(
                    WorkoutExercise("Cardio", "Burpee", sets = 3, reps = 12, kcal = 80, durationSec = 0)
                ),
            ),
            DayPlan(dayNumber = 2, isRest = true, title = "Recovery", workoutExercises = emptyList()),
        )

        override suspend fun saveCustomDayPlan(
            goal: FitnessGoal,
            dayNumber: Int,
            exercises: List<WorkoutExercise>,
        ) {
            customFlow.value = customFlow.value + (dayNumber to exercises)
        }

        override suspend fun clearCustomDayPlans(goal: FitnessGoal) {
            customFlow.value = emptyMap()
        }
    }

    @Test
    fun `observeMergedPlan enriches base exercises with catalog GIFs and applies custom overrides`() = runTest {
        val exerciseRepo = FakeExerciseRepository()
        val planRepo = FakeWorkoutPlanRepository()
        val useCase = GenerateWorkoutPlanUseCase(exerciseRepo, planRepo)

        val initialPlan = useCase.observeMergedPlan(FitnessGoal.WEIGHT_LOSS).first()
        assertEquals(true, exerciseRepo.seeded)
        assertEquals("burpee_1.gif", initialPlan[0].workoutExercises[0].gifFileName)

        planRepo.saveCustomDayPlan(
            FitnessGoal.WEIGHT_LOSS,
            dayNumber = 1,
            exercises = listOf(
                WorkoutExercise("Core", "Plank", sets = 3, reps = 0, kcal = 30, durationSec = 45)
            ),
        )

        val overriddenPlan = useCase.observeMergedPlan(FitnessGoal.WEIGHT_LOSS).first()
        assertEquals("Plank", overriddenPlan[0].workoutExercises[0].name)
        assertEquals("plank_1.gif", overriddenPlan[0].workoutExercises[0].gifFileName)
    }

    @Test
    fun `WorkoutAudioSettings parses Off and second strings accurately`() {
        val settings = WorkoutAudioSettings(restTimer = "45s", countdown = "Off")
        assertEquals(45, settings.restDurationSeconds)
        assertEquals(0, settings.countdownDurationSeconds)
    }
}
