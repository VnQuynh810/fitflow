package io.github.vnquynh810.fitflow.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.vnquynh810.fitflow.core.database.CustomDayPlanEntity
import io.github.vnquynh810.fitflow.core.database.ProgressDao
import io.github.vnquynh810.fitflow.core.domain.repository.WorkoutPlanRepository
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class WorkoutPlanRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val progressDao: ProgressDao,
) : WorkoutPlanRepository {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class PlanFileDto(
        val planId: String = "",
        val title: String = "",
        val days: List<DayPlan> = emptyList(),
    )

    override fun observeCustomDayPlans(goal: FitnessGoal): Flow<Map<Int, List<WorkoutExercise>>> =
        progressDao.observeCustomPlansForGoal(goal.name).map { entities ->
            entities.mapNotNull { entity ->
                val list = runCatching {
                    json.decodeFromString<List<WorkoutExercise>>(entity.exercisesJson)
                }.getOrNull() ?: return@mapNotNull null
                entity.dayNumber to list
            }.toMap()
        }

    override suspend fun loadBasePlanForGoal(goal: FitnessGoal): List<DayPlan> =
        withContext(Dispatchers.IO) {
            val planId = when (goal) {
                FitnessGoal.WEIGHT_LOSS -> "weight-loss"
                FitnessGoal.MUSCLE_GAIN -> "muscle-gain"
                FitnessGoal.ENDURANCE -> "endurance"
                FitnessGoal.MAINTENANCE -> "maintenance"
            }
            runCatching {
                context.assets.open("plans/$planId.json").bufferedReader().use { reader ->
                    json.decodeFromString<PlanFileDto>(reader.readText()).days
                }
            }.getOrDefault(emptyList())
        }

    override suspend fun saveCustomDayPlan(
        goal: FitnessGoal,
        dayNumber: Int,
        exercises: List<WorkoutExercise>,
    ) {
        progressDao.upsertCustomPlan(
            CustomDayPlanEntity(
                goalName = goal.name,
                dayNumber = dayNumber,
                exercisesJson = json.encodeToString(exercises),
            )
        )
    }

    override suspend fun clearCustomDayPlans(goal: FitnessGoal) {
        progressDao.deleteCustomPlansForGoal(goal.name)
    }
}
