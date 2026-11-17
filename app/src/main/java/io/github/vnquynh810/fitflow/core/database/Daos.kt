package io.github.vnquynh810.fitflow.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises")
    suspend fun getAllSnapshot(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Query("SELECT * FROM exercises WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getByNameIgnoreCase(name: String): ExerciseEntity?
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM goal_day_progress WHERE goalName = :goalName")
    fun observeProgressForGoal(goalName: String): Flow<List<GoalDayProgressEntity>>

    @Query("SELECT * FROM goal_day_progress WHERE goalName = :goalName AND dayNumber = :dayNumber LIMIT 1")
    suspend fun getDayProgress(goalName: String, dayNumber: Int): GoalDayProgressEntity?

    @Query("SELECT COUNT(*) FROM goal_day_progress WHERE goalName = :goalName AND isCompleted = 1")
    suspend fun countCompletedDays(goalName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: GoalDayProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllProgress(entities: List<GoalDayProgressEntity>)

    @Query("DELETE FROM goal_day_progress WHERE goalName = :goalName")
    suspend fun deleteProgressForGoal(goalName: String)

    @Query("SELECT * FROM workout_logs ORDER BY timestampMillis DESC")
    fun observeWorkoutLogs(): Flow<List<WorkoutLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLogs(logs: List<WorkoutLogEntity>)

    @Query("SELECT * FROM custom_day_plans WHERE goalName = :goalName")
    fun observeCustomPlansForGoal(goalName: String): Flow<List<CustomDayPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomPlan(entity: CustomDayPlanEntity)

    @Query("DELETE FROM custom_day_plans WHERE goalName = :goalName")
    suspend fun deleteCustomPlansForGoal(goalName: String)
}

@Dao
interface HealthDao {
    @Query("SELECT * FROM daily_health WHERE dateEpochDay = :epochDay LIMIT 1")
    fun observeDailyHealth(epochDay: Long): Flow<DailyHealthEntity?>

    @Query("SELECT * FROM daily_health WHERE dateEpochDay = :epochDay LIMIT 1")
    suspend fun getDailyHealth(epochDay: Long): DailyHealthEntity?

    @Query("SELECT * FROM daily_health ORDER BY dateEpochDay DESC LIMIT :limitDays")
    fun observeRecentHealth(limitDays: Int): Flow<List<DailyHealthEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyHealth(entity: DailyHealthEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllDailyHealth(entities: List<DailyHealthEntity>)

    @Query("SELECT * FROM weight_records ORDER BY dateEpochDay ASC")
    fun observeWeightHistory(): Flow<List<WeightRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeightRecord(entity: WeightRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllWeightRecords(entities: List<WeightRecordEntity>)
}
