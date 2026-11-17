package io.github.vnquynh810.fitflow.feature.planner

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vnquynh810.fitflow.core.designsystem.component.ExerciseInstructionSheetForWorkout
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise

@Composable
fun DayDetailRoute(
    dayNumber: Int,
    onBack: () -> Unit,
    onStartSession: (startIndex: Int) -> Unit,
    onEditPlan: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: DayDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(dayNumber) {
        viewModel.loadDay(dayNumber)
    }

    WorkoutDayDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onStartSession = onStartSession,
        onEditPlan = onEditPlan,
        onOpenSettings = onOpenSettings,
        onSelectExercise = viewModel::selectExercise,
        onCloseExerciseSheet = viewModel::clearSelectedExercise,
    )
}

@Composable
fun SupplementaryDetailRoute(
    workoutId: String,
    onBack: () -> Unit,
    onStartSession: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: DayDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(workoutId) {
        viewModel.loadSupplementary(workoutId)
    }

    WorkoutDayDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onStartSession = { _ -> onStartSession() },
        onEditPlan = {
            Toast.makeText(
                context,
                "You can't change supplementary workout plans",
                Toast.LENGTH_SHORT,
            ).show()
        },
        onOpenSettings = onOpenSettings,
        onSelectExercise = viewModel::selectExercise,
        onCloseExerciseSheet = viewModel::clearSelectedExercise,
    )
}

@Composable
internal fun WorkoutDayDetailScreen(
    uiState: DayDetailUiState,
    onBack: () -> Unit,
    onStartSession: (Int) -> Unit,
    onEditPlan: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectExercise: (WorkoutExercise) -> Unit,
    onCloseExerciseSheet: () -> Unit,
) {
    val dayPlan = uiState.dayPlan
    if (uiState.isLoading || dayPlan == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val partialIndex = uiState.partialIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            item {
                DayDetailHeaderAndSummary(
                    dayPlan = dayPlan,
                    goal = uiState.goal,
                    supplementary = uiState.supplementary,
                    onBack = onBack,
                    onOpenSettings = onOpenSettings,
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Exercise",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Plan",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onEditPlan() },
                    )
                }
            }
            itemsIndexed(dayPlan.workoutExercises) { index, exercise ->
                val isCompleted = partialIndex != null && index < partialIndex
                val gifUrl = uiState.gifUrlsByFileName[exercise.gifFileName.trim()]
                ExerciseExpandableItem(
                    exercise = exercise,
                    gifUrl = gifUrl,
                    isCompleted = isCompleted,
                    onClick = { onSelectExercise(exercise) },
                )
            }
        }

        DayDetailBottomBar(
            totalExercises = dayPlan.workoutExercises.size,
            partialIndex = partialIndex,
            onStartSession = onStartSession,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        uiState.selectedExercise?.let { detail ->
            ExerciseInstructionSheetForWorkout(
                workoutExercise = detail.workoutExercise,
                catalogExercise = detail.catalogExercise,
                onClose = onCloseExerciseSheet,
            )
        }
    }
}

@Composable
private fun DayDetailBottomBar(
    totalExercises: Int,
    partialIndex: Int?,
    onStartSession: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.background,
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY,
                )
            )
            .padding(24.dp),
    ) {
        if (partialIndex != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = { onStartSession(0) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    Text(
                        text = "RESTART",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                        ),
                    )
                }
                Button(
                    onClick = { onStartSession(partialIndex) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    val percent = if (totalExercises > 0) (partialIndex * 100) / totalExercises else 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CONTINUE",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                            ),
                        )
                        Text(
                            text = "$percent% completed",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        } else {
            Button(
                onClick = { onStartSession(0) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    text = "START",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    ),
                )
            }
        }
    }
}
