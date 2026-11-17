package io.github.vnquynh810.fitflow.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vnquynh810.fitflow.core.designsystem.component.HealthMetricsSection
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

@Composable
fun ProfileRoute(
    onBack: () -> Unit,
    onReCalibrate: () -> Unit,
    onRequestStepPermission: () -> Unit,
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        onRecordWeight = viewModel::recordWeight,
        onReCalibrate = onReCalibrate,
        onUnlockStepSensor = {
            if (!uiState.isActivityRecognitionGranted) {
                onRequestStepPermission()
            } else {
                viewModel.ensureStepTracking()
            }
        },
        onAddWater = viewModel::addWater,
        onSetWaterGoal = viewModel::setWaterGoal,
        onSetStepGoal = viewModel::setStepGoal,
        onDemoNotification = viewModel::triggerDemoNotification,
        onBack = onBack,
    )
}

@Composable
internal fun ProfileScreen(
    uiState: ProfileUiState,
    onRecordWeight: (Float, LocalDate) -> Unit,
    onReCalibrate: () -> Unit,
    onUnlockStepSensor: () -> Unit,
    onAddWater: (Int) -> Unit,
    onSetWaterGoal: (Int) -> Unit,
    onSetStepGoal: (Int) -> Unit,
    onDemoNotification: () -> Unit,
    onBack: () -> Unit,
) {
    val userProfile = uiState.userProfile
    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    val todayLabelIndex = dayLabels.indices.firstOrNull { idx ->
        weekStart.plusDays(idx.toLong()) == today
    } ?: -1

    val latestRecordedWeight = uiState.weightHistory.lastOrNull()?.weightKg ?: userProfile?.weight ?: 70f
    var currentWeightKg by remember(latestRecordedWeight) { mutableFloatStateOf(latestRecordedWeight) }
    var showWeightSheet by remember { mutableStateOf(false) }

    val weeklyDayDates = remember(weekStart) {
        (0..6).map { offset -> weekStart.plusDays(offset.toLong()) }
    }
    val logsByDate = remember(uiState.globalWorkoutLogs) {
        uiState.globalWorkoutLogs.groupBy { LocalDate.ofEpochDay(it.dateEpochDay) }
    }
    val weeklyDurationSeconds = remember(weeklyDayDates, logsByDate) {
        weeklyDayDates.map { date -> logsByDate[date]?.sumOf { it.durationSec } ?: 0 }
    }
    val weeklyMinutes = remember(weeklyDurationSeconds) {
        weeklyDurationSeconds.map { seconds -> (seconds / 60f).roundToInt() }
    }
    val weeklyKcal = remember(weeklyDayDates, logsByDate) {
        weeklyDayDates.map { date -> logsByDate[date]?.sumOf { it.kcal } ?: 0 }
    }
    val weeklyTotalMinutes = remember(weeklyDurationSeconds) {
        (weeklyDurationSeconds.sum() / 60f).roundToInt()
    }
    val weeklyTotalKcal = remember(weeklyKcal) { weeklyKcal.sum() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(end = 12.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = "REPORT",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
            )
        }

        ProfileWelcomeCard(userProfile = userProfile)

        Spacer(modifier = Modifier.height(16.dp))

        uiState.todayHealthMetrics?.let { todayMetrics ->
            HealthMetricsSection(
                metrics = todayMetrics,
                isActivityRecognitionGranted = uiState.isActivityRecognitionGranted,
                isStepSensorEnabled = uiState.isStepSensorEnabled,
                isStepTrackingActive = uiState.isStepTrackingActive,
                onUnlockStepSensor = onUnlockStepSensor,
                onAddWater = onAddWater,
                onSetWaterGoal = onSetWaterGoal,
                onSetStepGoal = onSetStepGoal,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompact = maxWidth < 360.dp
            if (isCompact) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeeklyChartCard(
                        title = "Duration",
                        totalValue = weeklyTotalMinutes.toString(),
                        unit = "min",
                        color = Color(0xFF3B66FF),
                        values = weeklyMinutes,
                        labels = dayLabels,
                        highlightedLabelIndex = todayLabelIndex,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    WeeklyChartCard(
                        title = "Calories",
                        totalValue = weeklyTotalKcal.toString(),
                        unit = "kcal",
                        color = Color(0xFFFF6A00),
                        values = weeklyKcal,
                        labels = dayLabels,
                        highlightedLabelIndex = todayLabelIndex,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeeklyChartCard(
                        title = "Duration",
                        totalValue = weeklyTotalMinutes.toString(),
                        unit = "min",
                        color = Color(0xFF3B66FF),
                        values = weeklyMinutes,
                        labels = dayLabels,
                        highlightedLabelIndex = todayLabelIndex,
                        modifier = Modifier.weight(1f),
                    )
                    WeeklyChartCard(
                        title = "Calories",
                        totalValue = weeklyTotalKcal.toString(),
                        unit = "kcal",
                        color = Color(0xFFFF6A00),
                        values = weeklyKcal,
                        labels = dayLabels,
                        highlightedLabelIndex = todayLabelIndex,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        WeightTrackingSection(
            currentWeight = currentWeightKg,
            targetWeight = userProfile?.targetWeight,
            weightHistory = uiState.weightHistory,
            onUpdateWeightClick = { showWeightSheet = true },
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "OPERATIONS",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))

        ProfileOperationCard(
            title = "RE-CALIBRATE BODY STATS",
            subtitle = "REVIEW YOUR ONBOARDING SETUP",
            icon = Icons.Default.Refresh,
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = onReCalibrate,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileOperationCard(
            title = "DEMO NOTIFICATION",
            subtitle = "SEND A TEST REMINDER IN 5S",
            icon = Icons.Default.NotificationsActive,
            iconTint = MaterialTheme.colorScheme.tertiary,
            onClick = onDemoNotification,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showWeightSheet) {
        WeightPickerSheet(
            initialWeight = currentWeightKg,
            onDismiss = { showWeightSheet = false },
            onSave = { newWeight, date ->
                currentWeightKg = newWeight
                onRecordWeight(newWeight, date)
                showWeightSheet = false
            },
        )
    }
}
