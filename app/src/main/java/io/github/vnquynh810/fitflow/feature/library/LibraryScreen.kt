package io.github.vnquynh810.fitflow.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vnquynh810.fitflow.core.designsystem.component.ExerciseInstructionSheetForLibrary
import io.github.vnquynh810.fitflow.core.designsystem.theme.FitflowTheme
import io.github.vnquynh810.fitflow.core.model.Exercise

@Composable
fun LibraryRoute(
    onBack: () -> Unit,
) {
    val viewModel: LibraryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::setSearchQuery,
        onCategorySelect = viewModel::setCategory,
        onDifficultySelect = viewModel::setDifficulty,
        onMuscleGroupSelect = viewModel::setMuscleGroup,
        onBack = onBack,
    )
}

@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onDifficultySelect: (String) -> Unit,
    onMuscleGroupSelect: (String) -> Unit,
    onBack: () -> Unit = {},
) {
    val filterState = uiState.filterState
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            RoundedCornerShape(12.dp),
                        ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Column {
                    Text(
                        text = "KNOWLEDGE",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                    )
                    Row {
                        Text(
                            text = "LIBRARY",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Normal,
                        )
                    }
                }
            }
        }

        TextField(
            value = filterState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(56.dp),
            placeholder = {
                Text(
                    text = "Find exercise...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = {
                if (filterState.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            item {
                FilterSection(title = "CATEGORY") {
                    uiState.categories.forEach { category ->
                        LibraryFilterChip(
                            label = category,
                            selected = filterState.category == category,
                            onClick = { onCategorySelect(category) },
                        )
                    }
                }
            }

            item {
                FilterSection(title = "DIFFICULTY") {
                    uiState.difficulties.forEach { difficulty ->
                        LibraryFilterChip(
                            label = difficulty,
                            selected = filterState.difficulty == difficulty,
                            onClick = { onDifficultySelect(difficulty) },
                        )
                    }
                }
            }

            item {
                FilterSection(title = "MUSCLE GROUP") {
                    uiState.muscleGroups.forEach { group ->
                        LibraryFilterChip(
                            label = group,
                            selected = filterState.muscleGroup == group,
                            onClick = { onMuscleGroupSelect(group) },
                        )
                    }
                }
            }

            item {
                Text(
                    text = "${uiState.filteredExercises.size} exercises found",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }

            items(
                items = uiState.filteredExercises,
                key = { it.name },
            ) { exercise ->
                ExerciseListItem(
                    exercise = exercise,
                    onClick = { selectedExercise = exercise },
                )
            }
        }
    }

    selectedExercise?.let { exercise ->
        ExerciseInstructionSheetForLibrary(
            exercise = exercise,
            onClose = { selectedExercise = null },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    FitflowTheme {
        LibraryScreen(
            uiState = LibraryUiState(),
            onSearchQueryChange = {},
            onCategorySelect = {},
            onDifficultySelect = {},
            onMuscleGroupSelect = {},
        )
    }
}
