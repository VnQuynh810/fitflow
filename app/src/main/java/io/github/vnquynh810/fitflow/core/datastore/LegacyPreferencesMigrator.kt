package io.github.vnquynh810.fitflow.core.datastore

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.vnquynh810.fitflow.core.database.DailyHealthEntity
import io.github.vnquynh810.fitflow.core.database.GoalDayProgressEntity
import io.github.vnquynh810.fitflow.core.database.HealthDao
import io.github.vnquynh810.fitflow.core.database.ProgressDao
import io.github.vnquynh810.fitflow.core.database.WeightRecordEntity
import io.github.vnquynh810.fitflow.core.database.WorkoutLogEntity
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class LegacyPreferencesMigrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataSource: FitFlowPreferencesDataSource,
    private val progressDao: ProgressDao,
    private val healthDao: HealthDao,
) {
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class LegacyWorkoutLogDto(
        val dateEpochDay: Long = 0L,
        val timestampMillis: Long = 0L,
        val dayNumber: Int = 0,
        val goalName: String = "",
        val durationSec: Int = 0,
        val kcal: Int = 0,
        val isRest: Boolean = false,
    )

    suspend fun migrateIfNeeded() = withContext(Dispatchers.IO) {
        if (dataSource.isLegacyMigrated()) return@withContext

        val legacy = context.getSharedPreferences("fitflow_prefs", Context.MODE_PRIVATE)
        if (legacy.all.isEmpty()) {
            dataSource.editPreferences { it[FitFlowPreferencesDataSource.Keys.LEGACY_MIGRATED] = true }
            return@withContext
        }

        // 1. Migrate scalar settings & profile into DataStore
        dataSource.editPreferences { prefs ->
            prefs[FitFlowPreferencesDataSource.Keys.IS_ONBOARDED] =
                legacy.getBoolean("is_onboarded", false)
            legacy.getString("goal", null)?.let { prefs[FitFlowPreferencesDataSource.Keys.GOAL] = it }
            legacy.getString("equipment", null)?.let { prefs[FitFlowPreferencesDataSource.Keys.EQUIPMENT] = it }
            val h = legacy.getFloat("height", 0f)
            val w = legacy.getFloat("weight", 0f)
            if (h > 0f) prefs[FitFlowPreferencesDataSource.Keys.HEIGHT] = h
            if (w > 0f) prefs[FitFlowPreferencesDataSource.Keys.WEIGHT] = w
            val by = legacy.getInt("birth_year", 0)
            if (by > 0) prefs[FitFlowPreferencesDataSource.Keys.BIRTH_YEAR] = by
            val tw = legacy.getFloat("target_weight", 0f)
            if (tw > 0f) prefs[FitFlowPreferencesDataSource.Keys.TARGET_WEIGHT] = tw
            val sd = legacy.getLong("start_date", -1L)
            if (sd >= 0L) prefs[FitFlowPreferencesDataSource.Keys.START_DATE_EPOCH] = sd
            legacy.getString("workout_time", null)?.let { prefs[FitFlowPreferencesDataSource.Keys.WORKOUT_TIME] = it }

            prefs[FitFlowPreferencesDataSource.Keys.CURRENT_STREAK] = legacy.getInt("current_streak", 0)
            prefs[FitFlowPreferencesDataSource.Keys.LONGEST_STREAK] = legacy.getInt("longest_streak", 0)
            val lwd = legacy.getLong("last_workout_date", -1L)
            if (lwd >= 0L) prefs[FitFlowPreferencesDataSource.Keys.LAST_WORKOUT_EPOCH] = lwd

            prefs[FitFlowPreferencesDataSource.Keys.BG_MUSIC_ENABLED] =
                legacy.getBoolean("setting_bg_music_enabled", true)
            prefs[FitFlowPreferencesDataSource.Keys.BG_MUSIC_VOLUME] =
                legacy.getFloat("setting_bg_music_volume", 0.5f)
            prefs[FitFlowPreferencesDataSource.Keys.VOICE_GUIDE_ENABLED] =
                legacy.getBoolean("setting_voice_guide_enabled", true)
            legacy.getString("setting_coach_name", null)?.let {
                prefs[FitFlowPreferencesDataSource.Keys.COACH_NAME] = it
            }
            prefs[FitFlowPreferencesDataSource.Keys.COACH_VOLUME] =
                legacy.getFloat("setting_coach_volume", 0.8f)
            prefs[FitFlowPreferencesDataSource.Keys.SOUND_EFFECT_ENABLED] =
                legacy.getBoolean("setting_sound_effect_enabled", true)
            legacy.getString("setting_auto_counting", null)?.let {
                prefs[FitFlowPreferencesDataSource.Keys.AUTO_COUNTING] = it
            }
            legacy.getString("setting_rest_timer", null)?.let {
                prefs[FitFlowPreferencesDataSource.Keys.REST_TIMER] = it
            }
            legacy.getString("setting_countdown", null)?.let {
                prefs[FitFlowPreferencesDataSource.Keys.COUNTDOWN] = it
            }
            prefs[FitFlowPreferencesDataSource.Keys.LEGACY_MIGRATED] = true
        }

        // 2. Migrate weight history ("epochDay:weight;...")
        val weightRaw = legacy.getString("weight_history", "").orEmpty()
        if (weightRaw.isNotBlank()) {
            val weightEntities = weightRaw.split(";").mapNotNull { token ->
                val parts = token.split(":")
                if (parts.size == 2) {
                    val epoch = parts[0].toLongOrNull() ?: return@mapNotNull null
                    val kg = parts[1].toFloatOrNull() ?: return@mapNotNull null
                    WeightRecordEntity(dateEpochDay = epoch, weightKg = kg)
                } else null
            }
            if (weightEntities.isNotEmpty()) {
                healthDao.upsertAllWeightRecords(weightEntities)
            }
        }

        // 3. Migrate daily health history
        val healthRaw = legacy.getString("health_history", "").orEmpty()
        if (healthRaw.isNotBlank()) {
            val healthEntities = healthRaw.split(";").mapNotNull { token ->
                val parts = token.split(",")
                if (parts.size < 5) return@mapNotNull null
                val epochDay = parts[0].toLongOrNull() ?: return@mapNotNull null
                val steps = parts[1].toIntOrNull() ?: return@mapNotNull null
                val waterIntake = parts[2].toIntOrNull() ?: return@mapNotNull null
                val waterGoal = parts[3].toIntOrNull() ?: return@mapNotNull null
                val stepGoal = if (parts.size >= 6) parts[4].toIntOrNull() ?: 6000 else 6000
                val source = if (parts.size >= 6) parts[5] else parts[4]
                DailyHealthEntity(
                    dateEpochDay = epochDay,
                    steps = steps,
                    waterIntakeMl = waterIntake,
                    waterGoalMl = waterGoal,
                    stepGoal = stepGoal,
                    stepSource = source,
                )
            }
            if (healthEntities.isNotEmpty()) {
                healthDao.upsertAllDailyHealth(healthEntities)
            }
        }

        // 4. Migrate per-goal progress
        FitnessGoal.entries.forEach { goal ->
            val daysSet = legacy.getString("completed_days_${goal.name}", "")
                .orEmpty()
                .split(",")
                .mapNotNull { it.toIntOrNull() }
                .toSet()

            val dayToEpoch = mutableMapOf<Int, Long>()
            legacy.getString("completed_date_map_${goal.name}", "")
                .orEmpty()
                .split(",")
                .forEach { token ->
                    val parts = token.split(":")
                    if (parts.size == 2) {
                        val epoch = parts[0].toLongOrNull()
                        val dayNum = parts[1].toIntOrNull()
                        if (epoch != null && dayNum != null) {
                            dayToEpoch[dayNum] = epoch
                        }
                    }
                }

            val partialMap = legacy.getString("partial_progress_${goal.name}", "")
                .orEmpty()
                .split(",")
                .mapNotNull { token ->
                    val parts = token.split(":")
                    if (parts.size == 2) {
                        val d = parts[0].toIntOrNull() ?: return@mapNotNull null
                        val idx = parts[1].toIntOrNull() ?: return@mapNotNull null
                        d to idx
                    } else null
                }
                .toMap()

            val allDays = daysSet + partialMap.keys
            val entities = allDays.map { dayNum ->
                GoalDayProgressEntity(
                    goalName = goal.name,
                    dayNumber = dayNum,
                    isCompleted = dayNum in daysSet,
                    completedEpochDay = dayToEpoch[dayNum],
                    completedTimestampMillis = null,
                    partialExerciseIndex = if (dayNum in daysSet) null else partialMap[dayNum],
                )
            }
            if (entities.isNotEmpty()) {
                progressDao.upsertAllProgress(entities)
            }
        }

        // 5. Migrate global workout logs JSON
        val logsRaw = legacy.getString("global_workout_logs", "").orEmpty()
        if (logsRaw.isNotBlank()) {
            val logs = runCatching { json.decodeFromString<List<LegacyWorkoutLogDto>>(logsRaw) }
                .getOrDefault(emptyList())
                .map {
                    WorkoutLogEntity(
                        dateEpochDay = it.dateEpochDay,
                        timestampMillis = it.timestampMillis,
                        dayNumber = it.dayNumber,
                        goalName = it.goalName,
                        durationSec = it.durationSec,
                        kcal = it.kcal,
                        isRest = it.isRest,
                    )
                }
            if (logs.isNotEmpty()) {
                progressDao.insertWorkoutLogs(logs)
            }
        }
    }
}
