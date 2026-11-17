package io.github.vnquynh810.fitflow.core.domain.repository

import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.UserProfile
import io.github.vnquynh810.fitflow.core.model.WorkoutAudioSettings
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userProfileFlow: Flow<UserProfile?>
    val isOnboardedFlow: Flow<Boolean>
    val startDateFlow: Flow<LocalDate?>
    val audioSettingsFlow: Flow<WorkoutAudioSettings>
    val isStepSensorEnabledFlow: Flow<Boolean>
    val customGeminiApiKeyFlow: Flow<String>

    suspend fun isOnboardedSnapshot(): Boolean
    suspend fun saveUserProfile(
        goal: FitnessGoal,
        height: Float,
        weight: Float,
        birthYear: Int,
        targetWeight: Float,
        workoutTime: String,
        equipment: String = "bodyweight",
    )
    suspend fun updateGoal(goal: FitnessGoal)
    suspend fun updateCurrentWeight(weightKg: Float)
    suspend fun setOnboarded(onboarded: Boolean)
    suspend fun setStepSensorEnabled(enabled: Boolean)
    suspend fun updateAudioSettings(transform: (WorkoutAudioSettings) -> WorkoutAudioSettings)
    suspend fun setCustomGeminiApiKey(apiKey: String)
    suspend fun markGifCacheReady(signature: String, ready: Boolean)
}
