package io.github.vnquynh810.fitflow.feature.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vnquynh810.fitflow.core.designsystem.theme.OrangePrimary
import io.github.vnquynh810.fitflow.core.model.Exercise

@Composable
fun EditPlanRoute(
    dayNumber: Int,
    onBack: () -> Unit,
) {
    val viewModel: EditPlanViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(dayNumber) {
        viewModel.loadDay(dayNumber)
    }

    EditPlanScreen(
        uiState = uiState,
        onBack = onBack,
        onMoveExercise = viewModel::moveExercise,
        onAdjustQuantity = viewModel::adjustQuantity,
        onReplaceExercise = viewModel::replaceExercise,
        onSave = { viewModel.saveChanges(onSaved = onBack) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditPlanScreen(
    uiState: EditPlanUiState,
    onBack: () -> Unit,
    onMoveExercise: (from: Int, to: Int) -> Unit,
    onAdjustQuantity: (index: Int, delta: Int) -> Unit,
    onReplaceExercise: (index: Int, Exercise) -> Unit,
    onSave: () -> Unit,
) {
    var showReplaceDialogForIndex by remember { mutableStateOf<Int?>(null) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragAccumulatedPx by remember { mutableFloatStateOf(0f) }
    var draggedItemOffsetPx by remember { mutableFloatStateOf(0f) }
    val dragStepPx = with(LocalDensity.current) { 72.dp.toPx() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "EDIT PLAN",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 20.sp,
                        )
                        val subtitle = buildString {
                            append("Day ${uiState.dayNumber}")
                            if (uiState.dayTitle.isNotBlank()) {
                                append(" • ${uiState.dayTitle}")
                            }
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "SAVE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
        ) {
            val editablePlan = uiState.editableExercises
            if (uiState.isLoading || editablePlan.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(editablePlan) { index, exercise ->
                        val gifUrl = uiState.gifUrlsByFileName[exercise.gifFileName.trim()]
                        EditExerciseRowItem(
                            index = index,
                            exercise = exercise,
                            gifUrl = gifUrl,
                            isDragging = draggingIndex == index,
                            onDragStart = {
                                draggingIndex = index
                                dragAccumulatedPx = 0f
                                draggedItemOffsetPx = 0f
                            },
                            onDrag = { deltaY ->
                                val currentIndex = draggingIndex ?: return@EditExerciseRowItem
                                dragAccumulatedPx += deltaY
                                draggedItemOffsetPx += deltaY

                                if (dragAccumulatedPx > dragStepPx && currentIndex < editablePlan.lastIndex) {
                                    onMoveExercise(currentIndex, currentIndex + 1)
                                    draggingIndex = currentIndex + 1
                                    dragAccumulatedPx -= dragStepPx
                                    draggedItemOffsetPx -= dragStepPx
                                } else if (dragAccumulatedPx < -dragStepPx && currentIndex > 0) {
                                    onMoveExercise(currentIndex, currentIndex - 1)
                                    draggingIndex = currentIndex - 1
                                    dragAccumulatedPx += dragStepPx
                                    draggedItemOffsetPx += dragStepPx
                                }
                            },
                            onDragEnd = {
                                draggingIndex = null
                                dragAccumulatedPx = 0f
                                draggedItemOffsetPx = 0f
                            },
                            dragOffsetPx = if (draggingIndex == index) draggedItemOffsetPx else 0f,
                            onAdjustQty = { delta -> onAdjustQuantity(index, delta) },
                            onReplace = { showReplaceDialogForIndex = index },
                        )
                    }
                }
            }

            showReplaceDialogForIndex?.let { index ->
                val exerciseToReplace = uiState.editableExercises.getOrNull(index)
                if (exerciseToReplace != null) {
                    ReplaceExerciseDialog(
                        exerciseName = exerciseToReplace.name,
                        category = exerciseToReplace.category,
                        allDatabaseExercises = uiState.catalogExercises,
                        gifUrlsByFileName = uiState.gifUrlsByFileName,
                        onDismiss = { showReplaceDialogForIndex = null },
                        onSelect = { selectedDbExercise ->
                            onReplaceExercise(index, selectedDbExercise)
                            showReplaceDialogForIndex = null
                        },
                    )
                }
            }
        }
    }
}
