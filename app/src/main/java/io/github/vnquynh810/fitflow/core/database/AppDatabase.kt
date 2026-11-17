package io.github.vnquynh810.fitflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutLogEntity::class,
        WeightRecordEntity::class,
        DailyHealthEntity::class,
        GoalDayProgressEntity::class,
        CustomDayPlanEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(RoomJsonConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun progressDao(): ProgressDao
    abstract fun healthDao(): HealthDao
}
