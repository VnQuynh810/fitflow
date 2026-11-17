package io.github.vnquynh810.fitflow.core.data

import io.github.vnquynh810.fitflow.core.database.HealthDao
import io.github.vnquynh810.fitflow.core.database.WeightRecordEntity
import io.github.vnquynh810.fitflow.core.datastore.FitFlowPreferencesDataSource
import io.github.vnquynh810.fitflow.core.datastore.FitFlowPreferencesDataSource.Keys
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.UserProfile
import io.github.vnquynh810.fitflow.core.model.WorkoutAudioSettings
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: FitFlowPreferencesDataSource,
    private val healthDao: HealthDao,
) : UserPreferencesRepository {

    override val userProfileFlow: Flow<UserProfile?> = dataSource.userProfileFlow
    override val isOnboardedFlow: Flow<Boolean> = dataSource.isOnboardedFlow
    override val startDateFlow: Flow<LocalDate?> = dataSource.startDateFlow
    override val audioSettingsFlow: Flow<WorkoutAudioSettings> = dataSource.audioSettingsFlow
    override val isStepSensorEnabledFlow: Flow<Boolean> = dataSource.isStepSensorEnabledFlow
    override val customGeminiApiKeyFlow: Flow<String> = dataSource.customGeminiApiKeyFlow

    override suspend fun isOnboardedSnapshot(): Boolean = dataSource.isOnboardedSnapshot()

    override suspend fun saveUserProfile(
        goal: FitnessGoal,
        height: Float,
        weight: Float,
        birthYear: Int,
        targetWeight: Float,
        workoutTime: String,
        equipment: String,
    ) {
        val todayEpoch = LocalDate.now().toEpochDay()
        dataSource.editPreferences { prefs ->
            prefs[Keys.GOAL] = goal.name
            prefs[Keys.EQUIPMENT] = equipment
            prefs[Keys.HEIGHT] = height
            prefs[Keys.WEIGHT] = weight
            prefs[Keys.BIRTH_YEAR] = birthYear
            prefs[Keys.TARGET_WEIGHT] = targetWeight
            if (prefs[Keys.START_DATE_EPOCH] == null) {
                prefs[Keys.START_DATE_EPOCH] = todayEpoch
            }
            prefs[Keys.WORKOUT_TIME] = workoutTime
        }
        healthDao.upsertWeightRecord(
            WeightRecordEntity(dateEpochDay = todayEpoch, weightKg = weight)
        )
    }

    override suspend fun updateGoal(goal: FitnessGoal) {
        dataSource.editPreferences { prefs ->
            prefs[Keys.GOAL] = goal.name
        }
    }

    override suspend fun updateCurrentWeight(weightKg: Float) {
        dataSource.editPreferences { prefs ->
            prefs[Keys.WEIGHT] = weightKg
        }
    }

    override suspend fun setOnboarded(onboarded: Boolean) {
        dataSource.editPreferences { prefs ->
            prefs[Keys.IS_ONBOARDED] = onboarded
        }
    }

    override suspend fun setStepSensorEnabled(enabled: Boolean) {
        dataSource.editPreferences { prefs ->
            prefs[Keys.STEP_SENSOR_ENABLED] = enabled
        }
    }

    override suspend fun updateAudioSettings(
        transform: (WorkoutAudioSettings) -> WorkoutAudioSettings,
    ) {
        val current = audioSettingsFlow.first()
        val updated = transform(current)
        dataSource.editPreferences { prefs ->
            prefs[Keys.BG_MUSIC_ENABLED] = updated.isBgMusicEnabled
            prefs[Keys.BG_MUSIC_VOLUME] = updated.bgMusicVolume
            prefs[Keys.VOICE_GUIDE_ENABLED] = updated.isVoiceGuideEnabled
            prefs[Keys.COACH_NAME] = updated.coachName
            prefs[Keys.COACH_VOLUME] = updated.coachVolume
            prefs[Keys.SOUND_EFFECT_ENABLED] = updated.isSoundEffectEnabled
            prefs[Keys.AUTO_COUNTING] = updated.autoCounting
            prefs[Keys.REST_TIMER] = updated.restTimer
            prefs[Keys.COUNTDOWN] = updated.countdown
        }
    }

    override suspend fun setCustomGeminiApiKey(apiKey: String) {
        dataSource.editPreferences { prefs ->
            prefs[Keys.CUSTOM_GEMINI_API_KEY] = apiKey.trim()
        }
    }

    override suspend fun markGifCacheReady(signature: String, ready: Boolean) {
        dataSource.editPreferences { prefs ->
            prefs[Keys.GIF_CACHE_READY] = ready
            prefs[Keys.GIF_CACHE_SIGNATURE] = signature
        }
    }
}
