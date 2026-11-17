package io.github.vnquynh810.fitflow.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.ExerciseRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.repository.WorkoutPlanRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.GenerateWorkoutPlanUseCase
import io.github.vnquynh810.fitflow.core.model.Exercise
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise
import io.github.vnquynh810.fitflow.core.platform.GifUrlProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditPlanUiState(
    val isLoading: Boolean = true,
    val dayNumber: Int = 1,
    val dayTitle: String = "",
    val editableExercises: List<WorkoutExercise> = emptyList(),
    val catalogExercises: List<Exercise> = emptyList(),
    val gifUrlsByFileName: Map<String, String> = emptyMap(),
)

@HiltViewModel
class EditPlanViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val exerciseRepository: ExerciseRepository,
    private val gifUrlProvider: GifUrlProvider,
) : ViewModel() {

    private var loadedDayNumber: Int? = null
    private var activeGoal: FitnessGoal = FitnessGoal.WEIGHT_LOSS

    private val editStateFlow = MutableStateFlow(EditPlanUiState())

    val uiState: StateFlow<EditPlanUiState> = combine(
        editStateFlow,
        exerciseRepository.observeAll(),
    ) { editState, allExercises ->
        val gifMap = buildMap {
            editState.editableExercises.forEach { ex ->
                val f = ex.gifFileName.trim()
                if (f.isNotEmpty()) put(f, gifUrlProvider.resolve(f))
            }
            allExercises.forEach { dbEx ->
                val f = dbEx.localGifs.firstOrNull()?.trim().orEmpty()
                if (f.isNotEmpty()) put(f, gifUrlProvider.resolve(f))
            }
        }
        editState.copy(
            catalogExercises = allExercises,
            gifUrlsByFileName = gifMap,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditPlanUiState(),
    )

    fun loadDay(dayNumber: Int) {
        if (loadedDayNumber == dayNumber && !editStateFlow.value.isLoading) return
        loadedDayNumber = dayNumber
        viewModelScope.launch {
            val goal = userPreferencesRepository.userProfileFlow.first()?.goal ?: FitnessGoal.WEIGHT_LOSS
            activeGoal = goal
            val mergedPlan = generateWorkoutPlanUseCase.observeMergedPlan(goal).first()
            val dayPlan = mergedPlan.find { it.dayNumber == dayNumber }
            editStateFlow.value = EditPlanUiState(
                isLoading = false,
                dayNumber = dayNumber,
                dayTitle = dayPlan?.title.orEmpty(),
                editableExercises = dayPlan?.workoutExercises.orEmpty(),
            )
        }
    }

    fun moveExercise(from: Int, to: Int) {
        editStateFlow.update { current ->
            val list = current.editableExercises.toMutableList()
            if (from !in list.indices || to !in list.indices) return@update current
            val item = list.removeAt(from)
            list.add(to, item)
            current.copy(editableExercises = list)
        }
    }

    fun adjustQuantity(index: Int, delta: Int) {
        editStateFlow.update { current ->
            val updated = current.editableExercises.mapIndexed { i, ex ->
                if (i != index) {
                    ex
                } else if (ex.durationSec > 0) {
                    ex.copy(durationSec = (ex.durationSec + delta * 5).coerceAtLeast(5))
                } else {
                    ex.copy(reps = (ex.reps + delta).coerceAtLeast(1))
                }
            }
            current.copy(editableExercises = updated)
        }
    }

    fun replaceExercise(index: Int, selectedDbExercise: Exercise) {
        editStateFlow.update { current ->
            val target = current.editableExercises.getOrNull(index) ?: return@update current
            val gifFile = selectedDbExercise.localGifs.firstOrNull().orEmpty()
            val isRepBased = selectedDbExercise.logType.equals("reps", ignoreCase = true)
            val replacement = WorkoutExercise(
                category = selectedDbExercise.exerciseType,
                name = selectedDbExercise.name,
                sets = target.sets,
                reps = if (isRepBased) 12 else 0,
                kcal = 45,
                durationSec = if (!isRepBased) 60 else 0,
                gifFileName = gifFile,
                description = selectedDbExercise.instructions.joinToString("\n"),
            )
            val updated = current.editableExercises.mapIndexed { i, ex ->
                if (i == index) replacement else ex
            }
            current.copy(editableExercises = updated)
        }
    }

    fun saveChanges(onSaved: () -> Unit) {
        val snapshot = editStateFlow.value
        if (snapshot.editableExercises.isEmpty()) {
            onSaved()
            return
        }
        viewModelScope.launch {
            workoutPlanRepository.saveCustomDayPlan(
                goal = activeGoal,
                dayNumber = snapshot.dayNumber,
                exercises = snapshot.editableExercises,
            )
            onSaved()
        }
    }
}
