package io.github.vnquynh810.fitflow.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.vnquynh810.fitflow.core.domain.usecase.BmiCalculator
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.StreakInfo
import io.github.vnquynh810.fitflow.core.model.UserProfile
import io.github.vnquynh810.fitflow.core.model.WorkoutAudioSettings
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.fitFlowDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "fitflow_preferences_v2"
)

@Singleton
class FitFlowPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.fitFlowDataStore

    object Keys {
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val LEGACY_MIGRATED = booleanPreferencesKey("legacy_migrated_v2")
        val GOAL = stringPreferencesKey("goal")
        val EQUIPMENT = stringPreferencesKey("equipment")
        val HEIGHT = floatPreferencesKey("height")
        val WEIGHT = floatPreferencesKey("weight")
        val BIRTH_YEAR = intPreferencesKey("birth_year")
        val TARGET_WEIGHT = floatPreferencesKey("target_weight")
        val START_DATE_EPOCH = longPreferencesKey("start_date_epoch")
        val WORKOUT_TIME = stringPreferencesKey("workout_time")

        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val LAST_WORKOUT_EPOCH = longPreferencesKey("last_workout_epoch")

        val STEP_BASELINE_DAY = longPreferencesKey("step_baseline_day")
        val STEP_BASELINE_VALUE = intPreferencesKey("step_baseline_value")
        val STEP_SENSOR_ENABLED = booleanPreferencesKey("step_sensor_enabled")

        val BG_MUSIC_ENABLED = booleanPreferencesKey("setting_bg_music_enabled")
        val BG_MUSIC_VOLUME = floatPreferencesKey("setting_bg_music_volume")
        val VOICE_GUIDE_ENABLED = booleanPreferencesKey("setting_voice_guide_enabled")
        val COACH_NAME = stringPreferencesKey("setting_coach_name")
        val COACH_VOLUME = floatPreferencesKey("setting_coach_volume")
        val SOUND_EFFECT_ENABLED = booleanPreferencesKey("setting_sound_effect_enabled")
        val AUTO_COUNTING = stringPreferencesKey("setting_auto_counting")
        val REST_TIMER = stringPreferencesKey("setting_rest_timer")
        val COUNTDOWN = stringPreferencesKey("setting_countdown")

        val GIF_CACHE_READY = booleanPreferencesKey("hybrid_gif_cache_ready")
        val GIF_CACHE_SIGNATURE = stringPreferencesKey("hybrid_gif_cache_signature")
        val CUSTOM_GEMINI_API_KEY = stringPreferencesKey("custom_gemini_api_key")
    }

    val isOnboardedFlow: Flow<Boolean> = dataStore.data.map { it[Keys.IS_ONBOARDED] ?: false }

    val startDateFlow: Flow<LocalDate?> = dataStore.data.map { prefs ->
        prefs[Keys.START_DATE_EPOCH]?.takeIf { it >= 0L }?.let(LocalDate::ofEpochDay)
    }

    val isStepSensorEnabledFlow: Flow<Boolean> = dataStore.data.map {
        it[Keys.STEP_SENSOR_ENABLED] ?: false
    }

    val customGeminiApiKeyFlow: Flow<String> = dataStore.data.map {
        it[Keys.CUSTOM_GEMINI_API_KEY].orEmpty()
    }

    val userProfileFlow: Flow<UserProfile?> = dataStore.data.map { prefs ->
        val height = prefs[Keys.HEIGHT] ?: 0f
        val weight = prefs[Keys.WEIGHT] ?: 0f
        if (height <= 0f || weight <= 0f) return@map null

        val birthYear = prefs[Keys.BIRTH_YEAR] ?: 2000
        val targetWeight = prefs[Keys.TARGET_WEIGHT] ?: weight
        val bmi = BmiCalculator.calculate(height, weight)
        val bmiCategory = BmiCalculator.categoryFor(bmi)
        val goal = prefs[Keys.GOAL]?.let { raw ->
            runCatching { FitnessGoal.valueOf(raw) }.getOrNull()
        } ?: FitnessGoal.WEIGHT_LOSS
        val equipment = prefs[Keys.EQUIPMENT] ?: "bodyweight"
        val workoutTime = prefs[Keys.WORKOUT_TIME] ?: "08:00"

        UserProfile(
            height = height,
            weight = weight,
            birthYear = birthYear,
            targetWeight = targetWeight,
            bmi = bmi,
            bmiCategory = bmiCategory,
            goal = goal,
            equipment = equipment,
            workoutTime = workoutTime,
        )
    }

    val streakInfoFlow: Flow<StreakInfo> = dataStore.data.map { prefs ->
        StreakInfo(
            currentStreak = prefs[Keys.CURRENT_STREAK] ?: 0,
            longestStreak = prefs[Keys.LONGEST_STREAK] ?: 0,
            lastWorkoutDate = prefs[Keys.LAST_WORKOUT_EPOCH]?.takeIf { it >= 0L }?.let(LocalDate::ofEpochDay),
        )
    }

    val audioSettingsFlow: Flow<WorkoutAudioSettings> = dataStore.data.map { prefs ->
        WorkoutAudioSettings(
            isBgMusicEnabled = prefs[Keys.BG_MUSIC_ENABLED] ?: true,
            bgMusicVolume = prefs[Keys.BG_MUSIC_VOLUME] ?: 0.5f,
            isVoiceGuideEnabled = prefs[Keys.VOICE_GUIDE_ENABLED] ?: true,
            coachName = prefs[Keys.COACH_NAME] ?: "James",
            coachVolume = prefs[Keys.COACH_VOLUME] ?: 0.8f,
            isSoundEffectEnabled = prefs[Keys.SOUND_EFFECT_ENABLED] ?: true,
            autoCounting = prefs[Keys.AUTO_COUNTING] ?: "Off",
            restTimer = prefs[Keys.REST_TIMER] ?: "30s",
            countdown = prefs[Keys.COUNTDOWN] ?: "5s",
        )
    }

    suspend fun isOnboardedSnapshot(): Boolean = isOnboardedFlow.first()

    suspend fun isLegacyMigrated(): Boolean =
        dataStore.data.first()[Keys.LEGACY_MIGRATED] ?: false

    suspend fun editPreferences(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit(block)
    }

    suspend fun getStepBaseline(): Pair<LocalDate?, Int> {
        val prefs = dataStore.data.first()
        val day = prefs[Keys.STEP_BASELINE_DAY]?.takeIf { it >= 0L }?.let(LocalDate::ofEpochDay)
        val value = prefs[Keys.STEP_BASELINE_VALUE] ?: -1
        return day to value
    }

    suspend fun setStepBaseline(day: LocalDate, baseline: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.STEP_BASELINE_DAY] = day.toEpochDay()
            prefs[Keys.STEP_BASELINE_VALUE] = baseline
        }
    }

    suspend fun updateStreak(streakInfo: StreakInfo) {
        dataStore.edit { prefs ->
            prefs[Keys.CURRENT_STREAK] = streakInfo.currentStreak
            prefs[Keys.LONGEST_STREAK] = streakInfo.longestStreak
            streakInfo.lastWorkoutDate?.let { prefs[Keys.LAST_WORKOUT_EPOCH] = it.toEpochDay() }
        }
    }
}
