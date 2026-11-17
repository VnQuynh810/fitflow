package io.github.vnquynh810.fitflow.core.data

import io.github.vnquynh810.fitflow.core.database.DailyHealthEntity
import io.github.vnquynh810.fitflow.core.database.HealthDao
import io.github.vnquynh810.fitflow.core.database.WeightRecordEntity
import io.github.vnquynh810.fitflow.core.datastore.FitFlowPreferencesDataSource
import io.github.vnquynh810.fitflow.core.domain.repository.HealthRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.model.DailyHealthMetrics
import io.github.vnquynh810.fitflow.core.model.StepSource
import io.github.vnquynh810.fitflow.core.model.WeightRecord
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val healthDao: HealthDao,
    private val preferencesDataSource: FitFlowPreferencesDataSource,
    private val userPreferencesRepository: UserPreferencesRepository,
) : HealthRepository {

    override fun observeTodayMetrics(defaultWaterGoalMl: Int): Flow<DailyHealthMetrics> {
        val today = LocalDate.now()
        return healthDao.observeDailyHealth(today.toEpochDay()).map { entity ->
            entity?.toDomain() ?: DailyHealthMetrics(
                date = today,
                steps = 0,
                waterIntakeMl = 0,
                waterGoalMl = defaultWaterGoalMl,
                stepGoal = 6000,
                stepSource = StepSource.MANUAL,
            )
        }
    }

    override fun observeHealthHistory(limitDays: Int): Flow<List<DailyHealthMetrics>> =
        healthDao.observeRecentHealth(limitDays).map { list ->
            list.map { it.toDomain() }.sortedBy { it.date }
        }

    override fun observeWeightHistory(): Flow<List<WeightRecord>> =
        healthDao.observeWeightHistory().map { list -> list.map { it.toDomain() } }

    override suspend fun getTodayMetricsSnapshot(defaultWaterGoalMl: Int): DailyHealthMetrics {
        val today = LocalDate.now()
        return healthDao.getDailyHealth(today.toEpochDay())?.toDomain()
            ?: DailyHealthMetrics(
                date = today,
                steps = 0,
                waterIntakeMl = 0,
                waterGoalMl = defaultWaterGoalMl,
                stepGoal = 6000,
                stepSource = StepSource.MANUAL,
            )
    }

    override suspend fun addWater(amountMl: Int, defaultWaterGoalMl: Int) {
        if (amountMl == 0) return
        val current = getTodayMetricsSnapshot(defaultWaterGoalMl)
        val updated = current.copy(
            waterIntakeMl = (current.waterIntakeMl + amountMl).coerceAtLeast(0)
        )
        persistMetrics(updated)
    }

    override suspend fun setWaterGoal(goalMl: Int, defaultWaterGoalMl: Int) {
        if (goalMl <= 0) return
        val current = getTodayMetricsSnapshot(defaultWaterGoalMl)
        persistMetrics(current.copy(waterGoalMl = goalMl))
    }

    override suspend fun setStepGoal(goalSteps: Int, defaultWaterGoalMl: Int) {
        if (goalSteps <= 0) return
        val current = getTodayMetricsSnapshot(defaultWaterGoalMl)
        persistMetrics(current.copy(stepGoal = goalSteps))
    }

    override suspend fun updateSensorSteps(counterValue: Int, defaultWaterGoalMl: Int) {
        val today = LocalDate.now()
        val (baselineDay, baselineValue) = preferencesDataSource.getStepBaseline()
        val baseline = if (baselineDay == today && baselineValue >= 0) {
            baselineValue
        } else {
            preferencesDataSource.setStepBaseline(today, counterValue)
            counterValue
        }
        val todaySteps = (counterValue - baseline).coerceAtLeast(0)
        val current = getTodayMetricsSnapshot(defaultWaterGoalMl)
        persistMetrics(current.copy(steps = todaySteps, stepSource = StepSource.SENSOR))
    }

    override suspend fun incrementSteps(
        delta: Int,
        defaultWaterGoalMl: Int,
        source: StepSource,
    ) {
        if (delta <= 0) return
        val current = getTodayMetricsSnapshot(defaultWaterGoalMl)
        persistMetrics(
            current.copy(
                steps = (current.steps + delta).coerceAtLeast(0),
                stepSource = source,
            )
        )
    }

    override suspend fun recordWeight(weightKg: Float, date: LocalDate) {
        if (weightKg <= 0f) return
        healthDao.upsertWeightRecord(
            WeightRecordEntity(dateEpochDay = date.toEpochDay(), weightKg = weightKg)
        )
        if (!date.isAfter(LocalDate.now())) {
            userPreferencesRepository.updateCurrentWeight(weightKg)
        }
    }

    private suspend fun persistMetrics(metrics: DailyHealthMetrics) {
        healthDao.upsertDailyHealth(
            DailyHealthEntity(
                dateEpochDay = metrics.date.toEpochDay(),
                steps = metrics.steps,
                waterIntakeMl = metrics.waterIntakeMl,
                waterGoalMl = metrics.waterGoalMl,
                stepGoal = metrics.stepGoal,
                stepSource = metrics.stepSource.name,
            )
        )
    }
}
