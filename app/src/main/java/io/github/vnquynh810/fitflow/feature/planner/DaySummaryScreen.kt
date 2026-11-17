package io.github.vnquynh810.fitflow.feature.planner

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DaySummaryRoute(
    dayNumber: Int,
    epochDay: Long,
    onBack: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenWorkoutDay: (Int) -> Unit,
) {
    val viewModel: DaySummaryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(dayNumber, epochDay) {
        viewModel.initialize(dayNumber, epochDay)
    }

    DayWorkoutSummaryScreen(
        uiState = uiState,
        onBack = onBack,
        onNavigateDay = viewModel::selectDate,
        onOpenPlanner = onOpenPlanner,
        onOpenWorkoutDay = onOpenWorkoutDay,
    )
}

@Composable
internal fun DayWorkoutSummaryScreen(
    uiState: DaySummaryUiState,
    onBack: () -> Unit,
    onNavigateDay: (LocalDate) -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenPlanner: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenWorkoutDay: (Int) -> Unit,
) {
    BackHandler(onBack = onBack)

    val selectedDate = uiState.selectedDate
    val workoutPlan = uiState.workoutPlan
    val completedDays = uiState.completedDays
    val completedDateMap = uiState.completedDateMap

    val nextPlannedDay = workoutPlan
        .filter { !it.isRest }
        .firstOrNull { it.dayNumber !in completedDays }

    val resolvedDayNumber: Int? = completedDateMap[selectedDate]
        ?: if (selectedDate == LocalDate.now()) nextPlannedDay?.dayNumber else null

    val dayPlan = resolvedDayNumber?.let { dn -> workoutPlan.find { it.dayNumber == dn } }
    val isCompleted = selectedDate in completedDateMap
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }

    val totalKcal = dayPlan?.workoutExercises?.sumOf { it.kcal } ?: 0
    val totalMinutes = dayPlan?.workoutExercises?.sumOf {
        if (it.durationSec > 0) it.durationSec else it.sets * 45
    }?.div(60) ?: 0
    val exerciseCount = dayPlan?.workoutExercises?.size ?: 0
    val totalSets = dayPlan?.workoutExercises?.sumOf { it.sets } ?: 0

    val progressFraction = if (isCompleted && dayPlan != null && !dayPlan.isRest) 1f else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "summary_progress",
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            DayWorkoutTopBar(
                selectedDate = selectedDate,
                dateFormatter = dateFormatter,
                onBack = onBack,
                onPrevDay = { onNavigateDay(selectedDate.minusDays(1)) },
                onNextDay = { onNavigateDay(selectedDate.plusDays(1)) },
            )
        }
        item {
            Spacer(Modifier.height(24.dp))
            CircularProgressSection(
                isCompleted = isCompleted,
                isRestDay = dayPlan?.isRest == true,
                noPlan = dayPlan == null,
                animatedProgress = animatedProgress,
                totalMinutes = totalMinutes,
            )
        }
        item {
            Spacer(Modifier.height(28.dp))
            PlanProgressSection(
                completedDaysCount = completedDays.size,
                totalDaysCount = workoutPlan.size,
            )
        }
        item {
            Spacer(Modifier.height(28.dp))
            OverviewSection(
                totalKcal = totalKcal,
                activityCount = totalSets,
                exerciseCount = exerciseCount,
                currentStreak = uiState.currentStreak,
            )
        }
    }
}

@Composable
private fun DayWorkoutTopBar(
    selectedDate: LocalDate,
    dateFormatter: DateTimeFormatter,
    onBack: () -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f), CircleShape),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = "WORKOUTS",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onPrevDay,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), CircleShape),
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous day",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp),
                )
            }

            Text(
                text = selectedDate.format(dateFormatter),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            IconButton(
                onClick = onNextDay,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), CircleShape),
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next day",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)),
        )
    }
}
