package io.github.vnquynh810.fitflow.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.vnquynh810.fitflow.core.model.DailyHealthMetrics
import io.github.vnquynh810.fitflow.core.model.Exercise
import io.github.vnquynh810.fitflow.core.model.StepSource
import io.github.vnquynh810.fitflow.core.model.WeightRecord
import io.github.vnquynh810.fitflow.core.model.WorkoutLogEntry
import java.time.LocalDate

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val name: String,
    val difficulty: String,
    @ColumnInfo(name = "exercise_type") val exerciseType: String,
    @ColumnInfo(name = "log_type") val logType: String,
    @ColumnInfo(name = "target_muscles") val targetMuscles: List<String>,
    val equipment: List<String>,
    val instructions: List<String>,
    @ColumnInfo(name = "local_gifs") val localGifs: List<String>,
) {
    fun toDomain(): Exercise = Exercise(
        name = name,
        difficulty = difficulty,
        exerciseType = exerciseType,
        logType = logType,
        targetMuscles = targetMuscles,
        equipment = equipment,
        instructions = instructions,
        localGifs = localGifs,
    )
}

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateEpochDay: Long,
    val timestampMillis: Long,
    val dayNumber: Int,
    val goalName: String,
    val durationSec: Int,
    val kcal: Int,
    val isRest: Boolean,
) {
    fun toDomain(): WorkoutLogEntry = WorkoutLogEntry(
        id = id,
        dateEpochDay = dateEpochDay,
        timestampMillis = timestampMillis,
        dayNumber = dayNumber,
        goalName = goalName,
        durationSec = durationSec,
        kcal = kcal,
        isRest = isRest,
    )
}

@Entity(tableName = "weight_records")
data class WeightRecordEntity(
    @PrimaryKey val dateEpochDay: Long,
    val weightKg: Float,
) {
    fun toDomain(): WeightRecord = WeightRecord(
        date = LocalDate.ofEpochDay(dateEpochDay),
        weightKg = weightKg,
    )
}

@Entity(tableName = "daily_health")
data class DailyHealthEntity(
    @PrimaryKey val dateEpochDay: Long,
    val steps: Int,
    val waterIntakeMl: Int,
    val waterGoalMl: Int,
    val stepGoal: Int,
    val stepSource: String,
) {
    fun toDomain(): DailyHealthMetrics = DailyHealthMetrics(
        date = LocalDate.ofEpochDay(dateEpochDay),
        steps = steps,
        waterIntakeMl = waterIntakeMl,
        waterGoalMl = waterGoalMl,
        stepGoal = stepGoal,
        stepSource = runCatching { StepSource.valueOf(stepSource) }.getOrDefault(StepSource.MANUAL),
    )
}

@Entity(
    tableName = "goal_day_progress",
    primaryKeys = ["goalName", "dayNumber"],
)
data class GoalDayProgressEntity(
    val goalName: String,
    val dayNumber: Int,
    val isCompleted: Boolean = false,
    val completedEpochDay: Long? = null,
    val completedTimestampMillis: Long? = null,
    val partialExerciseIndex: Int? = null,
)

@Entity(
    tableName = "custom_day_plans",
    primaryKeys = ["goalName", "dayNumber"],
)
data class CustomDayPlanEntity(
    val goalName: String,
    val dayNumber: Int,
    val exercisesJson: String,
)
