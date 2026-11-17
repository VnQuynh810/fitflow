package io.github.vnquynh810.fitflow.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Bot
import com.composables.icons.lucide.HeartPulse
import com.composables.icons.lucide.Lucide
import io.github.vnquynh810.fitflow.core.designsystem.component.ExerciseInstructionSheetForLibrary
import io.github.vnquynh810.fitflow.core.designsystem.component.PushYourLimitsSection
import io.github.vnquynh810.fitflow.core.designsystem.theme.FitflowTheme
import io.github.vnquynh810.fitflow.core.model.Exercise
import io.github.vnquynh810.fitflow.feature.dashboard.components.DailyChallengeCard
import io.github.vnquynh810.fitflow.feature.dashboard.components.DashboardLibraryCard
import io.github.vnquynh810.fitflow.feature.dashboard.components.WeeklyGoalCard
import java.time.LocalDate

@Composable
fun DashboardRoute(
    onOpenChatbot: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenSupplementary: (String) -> Unit,
    onOpenDaySummary: (dayNumber: Int, epochDay: Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenChangeGoal: () -> Unit,
    onOpenLibrary: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onOpenChatbot = onOpenChatbot,
        onOpenProfile = onOpenProfile,
        onOpenPlanner = onOpenPlanner,
        onOpenSupplementary = onOpenSupplementary,
        onOpenDaySummary = onOpenDaySummary,
        onOpenHistory = onOpenHistory,
        onOpenChangeGoal = onOpenChangeGoal,
        onOpenLibrary = onOpenLibrary,
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onOpenChatbot: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenPlanner: () -> Unit = {},
    onOpenSupplementary: (String) -> Unit = {},
    onOpenDaySummary: (Int, Long) -> Unit = { _, _ -> },
    onOpenHistory: () -> Unit = {},
    onOpenChangeGoal: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
) {
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        DashboardTopBar(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
            onOpenChatbot = onOpenChatbot,
            onOpenProfile = onOpenProfile,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 24.dp,
            ),
        ) {
            item {
                WeeklyGoalCard(
                    globalWorkoutLogs = uiState.globalWorkoutLogs,
                    currentStreak = uiState.currentStreak,
                    longestStreak = uiState.longestStreak,
                    onViewHistory = onOpenHistory,
                    onToggleDay = { weekIndex ->
                        val today = LocalDate.now()
                        val todayIndex = if (today.dayOfWeek.value == 7) 0 else today.dayOfWeek.value
                        val startOfWeek = today.minusDays(todayIndex.toLong())
                        val date = startOfWeek.plusDays(weekIndex.toLong())
                        val dayNum = uiState.completedDateMap[date] ?: -1
                        onOpenDaySummary(dayNum, date.toEpochDay())
                    },
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            if (uiState.currentDayPlan != null) {
                item {
                    DailyChallengeCard(
                        goal = uiState.activeGoal,
                        currentDay = uiState.currentDayPlan.dayNumber,
                        totalDays = uiState.totalWorkoutDays,
                        onClick = onOpenPlanner,
                        onOpenGoalSwitcher = onOpenChangeGoal,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            item {
                PushYourLimitsSection(
                    workouts = uiState.supplementaryWorkouts,
                    onWorkoutClick = onOpenSupplementary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                DashboardLibraryCard(
                    searchQuery = uiState.librarySearchQuery,
                    selectedMuscleGroup = uiState.libraryMuscleGroup,
                    muscleGroups = uiState.libraryMuscleGroups,
                    filteredExercises = uiState.libraryExercises,
                    onSearchQueryChange = { onEvent(DashboardUiEvent.UpdateLibrarySearch(it)) },
                    onMuscleGroupChange = { onEvent(DashboardUiEvent.SelectLibraryMuscle(it)) },
                    onViewAll = onOpenLibrary,
                    onExerciseClick = { selectedExercise = it },
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

@Composable
private fun DashboardTopBar(
    modifier: Modifier = Modifier,
    onOpenChatbot: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "FIT",
                color = Color(0xFFFF6B00),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
            )
            Text(
                text = "FLOW",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onOpenProfile,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            RoundedCornerShape(50),
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(50),
                        ),
                ) {
                    Icon(
                        Lucide.HeartPulse,
                        contentDescription = "Report",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "REPORT",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.offset(y = (-4).dp),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onOpenChatbot,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            RoundedCornerShape(50),
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(50),
                        ),
                ) {
                    Icon(
                        Lucide.Bot,
                        contentDescription = "AI Coach",
                        tint = Color(0xFFFF6B00),
                    )
                }
                Text(
                    text = "FITBRO AI",
                    fontSize = 9.sp,
                    color = Color(0xFFFF6B00),
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.offset(y = (-4).dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    FitflowTheme {
        DashboardScreen(
            uiState = DashboardUiState(),
            onEvent = {},
        )
    }
}
