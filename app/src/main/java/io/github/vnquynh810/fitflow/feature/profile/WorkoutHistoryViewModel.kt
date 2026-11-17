package io.github.vnquynh810.fitflow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.model.WorkoutLogEntry
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class WorkoutHistoryUiState(
    val workoutLogs: List<WorkoutLogEntry> = emptyList(),
)

@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    progressRepository: ProgressRepository,
) : ViewModel() {

    val uiState: StateFlow<WorkoutHistoryUiState> = progressRepository.observeWorkoutLogs()
        .map { logs ->
            WorkoutHistoryUiState(
                workoutLogs = logs.sortedByDescending { it.timestampMillis }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WorkoutHistoryUiState(),
        )
}
