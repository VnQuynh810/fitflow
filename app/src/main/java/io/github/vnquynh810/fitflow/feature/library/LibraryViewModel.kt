package io.github.vnquynh810.fitflow.feature.library

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.ExerciseRepository
import io.github.vnquynh810.fitflow.core.model.Exercise
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class LibraryFilterState(
    val searchQuery: String = "",
    val category: String = "ALL",
    val difficulty: String = "ALL",
    val muscleGroup: String = "ALL",
)

@Immutable
data class LibraryUiState(
    val filterState: LibraryFilterState = LibraryFilterState(),
    val filteredExercises: List<Exercise> = emptyList(),
    val categories: List<String> = listOf("ALL"),
    val difficulties: List<String> = listOf("ALL"),
    val muscleGroups: List<String> = listOf("ALL"),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    exerciseRepository: ExerciseRepository,
) : ViewModel() {

    init {
        viewModelScope.launch {
            exerciseRepository.ensureSeeded()
        }
    }

    private val _filterState = MutableStateFlow(LibraryFilterState())
    val filterState: StateFlow<LibraryFilterState> = _filterState.asStateFlow()

    val allExercises: StateFlow<List<Exercise>> = exerciseRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredExercises: StateFlow<List<Exercise>> = combine(
        allExercises,
        _filterState,
    ) { list, filter ->
        list.filter { exercise ->
            val categoryMatch = filter.category == "ALL" || exercise.exerciseType == filter.category
            val difficultyMatch =
                filter.difficulty == "ALL" || exercise.difficulty == filter.difficulty
            val muscleMatch =
                filter.muscleGroup == "ALL" || exercise.targetMuscles.contains(filter.muscleGroup)
            val searchMatch = filter.searchQuery.isEmpty() || exercise.name.contains(
                filter.searchQuery,
                ignoreCase = true,
            )
            categoryMatch && difficultyMatch && muscleMatch && searchMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<String>> = allExercises.map { list ->
        listOf("ALL") + list.map { it.exerciseType }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf("ALL"))

    val difficulties: StateFlow<List<String>> = allExercises.map { list ->
        val order = listOf("beginner", "intermediate", "advanced")
        listOf("ALL") + list.map { it.difficulty }.distinct().sortedBy { difficulty ->
            val idx = order.indexOf(difficulty)
            if (idx >= 0) idx else Int.MAX_VALUE
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf("ALL"))

    val muscleGroups: StateFlow<List<String>> = allExercises.map { list ->
        listOf("ALL") + list.flatMap { it.targetMuscles }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf("ALL"))

    val uiState: StateFlow<LibraryUiState> = combine(
        _filterState,
        filteredExercises,
        categories,
        difficulties,
        muscleGroups,
    ) { filter, filtered, cats, diffs, muscles ->
        LibraryUiState(
            filterState = filter,
            filteredExercises = filtered,
            categories = cats,
            difficulties = diffs,
            muscleGroups = muscles,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun setCategory(category: String) {
        _filterState.update { it.copy(category = category) }
    }

    fun setDifficulty(difficulty: String) {
        _filterState.update { it.copy(difficulty = difficulty) }
    }

    fun setMuscleGroup(group: String) {
        _filterState.update { it.copy(muscleGroup = group) }
    }
}
