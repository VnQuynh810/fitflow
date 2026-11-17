package io.github.vnquynh810.fitflow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResumeGoalPrompt(
    val goal: FitnessGoal,
    val completedDaysCount: Int,
)

data class UpdateGoalUiState(
    val currentGoal: FitnessGoal = FitnessGoal.WEIGHT_LOSS,
    val selectedGoal: FitnessGoal = FitnessGoal.MUSCLE_GAIN,
    val resumePrompt: ResumeGoalPrompt? = null,
)

@HiltViewModel
class UpdateGoalViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    private val userSelectedGoalFlow = MutableStateFlow<FitnessGoal?>(null)
    private val resumePromptFlow = MutableStateFlow<ResumeGoalPrompt?>(null)

    private val provisioningGoalChannel = Channel<FitnessGoal>(Channel.BUFFERED)
    val provisioningGoalEvents: Flow<FitnessGoal> = provisioningGoalChannel.receiveAsFlow()

    val uiState: StateFlow<UpdateGoalUiState> = combine(
        userPreferencesRepository.userProfileFlow,
        userSelectedGoalFlow,
        resumePromptFlow,
    ) { profile, manualSelection, prompt ->
        val currentGoal = profile?.goal ?: FitnessGoal.WEIGHT_LOSS
        val defaultAlternative = FitnessGoal.entries.firstOrNull { it != currentGoal }
            ?: FitnessGoal.WEIGHT_LOSS
        val effectiveSelection = manualSelection
            ?.takeIf { it != currentGoal }
            ?: defaultAlternative

        UpdateGoalUiState(
            currentGoal = currentGoal,
            selectedGoal = effectiveSelection,
            resumePrompt = prompt,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateGoalUiState(),
    )

    fun selectGoal(goal: FitnessGoal) {
        userSelectedGoalFlow.value = goal
    }

    fun onFinalizeClicked() {
        val targetGoal = uiState.value.selectedGoal
        viewModelScope.launch {
            val completedCount = progressRepository.getCompletedCountForGoal(targetGoal)
            if (completedCount > 0) {
                resumePromptFlow.value = ResumeGoalPrompt(
                    goal = targetGoal,
                    completedDaysCount = completedCount,
                )
            } else {
                commitGoalAndProceed(goal = targetGoal, resetProgress = false)
            }
        }
    }

    fun dismissResumeDialog() {
        resumePromptFlow.value = null
    }

    fun confirmGoalChoice(resetProgress: Boolean) {
        val prompt = resumePromptFlow.value ?: return
        resumePromptFlow.value = null
        viewModelScope.launch {
            commitGoalAndProceed(goal = prompt.goal, resetProgress = resetProgress)
        }
    }

    private suspend fun commitGoalAndProceed(goal: FitnessGoal, resetProgress: Boolean) {
        if (resetProgress) {
            progressRepository.clearProgressForGoal(goal)
        }
        userPreferencesRepository.updateGoal(goal)
        provisioningGoalChannel.send(goal)
    }
}
