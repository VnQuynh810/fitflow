package io.github.vnquynh810.fitflow.feature.onboarding

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.platform.WorkoutReminderScheduler
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class OnboardingUiState(
    val selectedGoal: FitnessGoal = FitnessGoal.WEIGHT_LOSS,
    val height: Float = 160f,
    val weight: Float = 60f,
    val birthYear: Int = 2000,
    val targetWeight: Float = 60f,
    val workoutTime: String = "08:00",
    val equipment: String = "bodyweight",
    val isPreloaded: Boolean = false,
    val isSaving: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: WorkoutReminderScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var preloadRequested = false

    fun initialize(isRecalibrate: Boolean) {
        if (preloadRequested) return
        preloadRequested = true

        if (!isRecalibrate) {
            _uiState.update { it.copy(isPreloaded = true) }
            return
        }

        viewModelScope.launch {
            val profile = preferencesRepository.userProfileFlow.first()
            if (profile != null) {
                _uiState.update {
                    it.copy(
                        selectedGoal = profile.goal,
                        height = profile.height,
                        weight = profile.weight,
                        birthYear = profile.birthYear,
                        targetWeight = profile.targetWeight,
                        workoutTime = profile.workoutTime,
                        equipment = profile.equipment,
                        isPreloaded = true,
                    )
                }
            } else {
                _uiState.update { it.copy(isPreloaded = true) }
            }
        }
    }

    fun updateGoal(goal: FitnessGoal) {
        _uiState.update { it.copy(selectedGoal = goal) }
    }

    fun updateWeight(weight: Float) {
        _uiState.update { it.copy(weight = weight) }
    }

    fun updateHeight(height: Float) {
        _uiState.update { it.copy(height = height) }
    }

    fun updateTargetWeight(targetWeight: Float) {
        _uiState.update { it.copy(targetWeight = targetWeight) }
    }

    fun updateBirthYear(birthYear: Int) {
        _uiState.update { it.copy(birthYear = birthYear) }
    }

    fun updateWorkoutTime(workoutTime: String) {
        _uiState.update { it.copy(workoutTime = workoutTime) }
    }

    fun completeOnboarding(onCompleted: () -> Unit) {
        val snapshot = _uiState.value
        if (snapshot.isSaving) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            preferencesRepository.saveUserProfile(
                goal = snapshot.selectedGoal,
                height = snapshot.height,
                weight = snapshot.weight,
                birthYear = snapshot.birthYear,
                targetWeight = snapshot.targetWeight,
                workoutTime = snapshot.workoutTime,
                equipment = snapshot.equipment,
            )
            preferencesRepository.setOnboarded(true)
            reminderScheduler.scheduleDailyReminder(snapshot.workoutTime)
            _uiState.update { it.copy(isSaving = false) }
            onCompleted()
        }
    }
}
