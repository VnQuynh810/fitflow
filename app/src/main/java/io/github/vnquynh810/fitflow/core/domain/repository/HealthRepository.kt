package io.github.vnquynh810.fitflow.core.domain.repository

import io.github.vnquynh810.fitflow.core.model.DailyHealthMetrics
import io.github.vnquynh810.fitflow.core.model.StepSource
import io.github.vnquynh810.fitflow.core.model.WeightRecord
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    fun observeTodayMetrics(defaultWaterGoalMl: Int): Flow<DailyHealthMetrics>
    fun observeHealthHistory(limitDays: Int = 90): Flow<List<DailyHealthMetrics>>
    fun observeWeightHistory(): Flow<List<WeightRecord>>
    suspend fun getTodayMetricsSnapshot(defaultWaterGoalMl: Int): DailyHealthMetrics
    suspend fun addWater(amountMl: Int, defaultWaterGoalMl: Int)
    suspend fun setWaterGoal(goalMl: Int, defaultWaterGoalMl: Int)
    suspend fun setStepGoal(goalSteps: Int, defaultWaterGoalMl: Int)
    suspend fun updateSensorSteps(counterValue: Int, defaultWaterGoalMl: Int)
    suspend fun incrementSteps(delta: Int, defaultWaterGoalMl: Int, source: StepSource)
    suspend fun recordWeight(weightKg: Float, date: LocalDate = LocalDate.now())
}
