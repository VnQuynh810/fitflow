package io.github.vnquynh810.fitflow.feature.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PlannerRoute(
    onDayClick: (dayNumber: Int, isRest: Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: PlannerViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlannerScreen(
        uiState = uiState,
        onDayClick = onDayClick,
        onMarkRestComplete = { dayNumber -> viewModel.markRestDayComplete(dayNumber) },
        onResetPlan = viewModel::resetPlan,
        onOpenSettings = onOpenSettings,
        onBack = onBack,
    )
}

@Composable
internal fun PlannerScreen(
    uiState: PlannerUiState,
    onDayClick: (dayNumber: Int, isRest: Boolean) -> Unit,
    onMarkRestComplete: (Int) -> Unit,
    onResetPlan: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        ResetPlanDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                showResetDialog = false
                onResetPlan()
            },
        )
    }

    val workoutPlan = uiState.workoutPlan
    val completedDays = uiState.completedDays
    val realCurrentDay = workoutPlan.firstOrNull { it.dayNumber !in completedDays }?.dayNumber ?: -1
    val daysLeft = (workoutPlan.size - completedDays.size).coerceAtLeast(0)
    val progress = if (workoutPlan.isNotEmpty()) completedDays.size.toFloat() / workoutPlan.size else 0f

    val listState = rememberLazyListState()
    val headerHeightPx = with(LocalDensity.current) { 200.dp.toPx() }
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeightPx = with(LocalDensity.current) { (56.dp + topPadding).toPx() }

    val topBarAlpha by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / (headerHeightPx * 0.5f)).coerceIn(0f, 1f)
        }
    }

    val isProgressBarPinned by remember {
        derivedStateOf {
            val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == 1 }
            if (itemInfo != null) {
                itemInfo.offset <= topBarHeightPx
            } else {
                listState.firstVisibleItemIndex > 1
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                PlannerHeroHeader(goalTitle = uiState.goalTitle)
            }

            item {
                PlannerProgressBarSection(
                    daysLeft = daysLeft,
                    progress = progress,
                    modifier = Modifier.alpha(if (isProgressBarPinned) 0f else 1f),
                )
            }

            items(workoutPlan, key = { it.dayNumber }) { dayPlan ->
                val isCompleted = dayPlan.dayNumber in completedDays
                val isCurrent = dayPlan.dayNumber == realCurrentDay
                val partialIndex = uiState.partialProgressMap[dayPlan.dayNumber]

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                ) {
                    DayPlanItemCard(
                        dayPlan = dayPlan,
                        isCompleted = isCompleted,
                        isCurrent = isCurrent,
                        partialIndex = partialIndex,
                        onClick = { onDayClick(dayPlan.dayNumber, dayPlan.isRest) },
                        onRestClick = { onMarkRestComplete(dayPlan.dayNumber) },
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        thickness = 1.dp,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RESET PLAN",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = topBarAlpha))
                .padding(top = topPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                    if (topBarAlpha > 0.3f) {
                        Text(
                            text = uiState.goalTitle,
                            color = Color.White.copy(alpha = (topBarAlpha - 0.3f) / 0.7f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.padding(end = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Workout Settings",
                        tint = Color.White,
                    )
                }
            }
        }

        if (isProgressBarPinned) {
            PlannerProgressBarSection(
                daysLeft = daysLeft,
                progress = progress,
                modifier = Modifier.padding(top = 56.dp + topPadding),
            )
        }
    }
}

@Composable
private fun PlannerHeroHeader(goalTitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        )
                    )
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp),
        ) {
            Text(
                text = goalTitle,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                ),
        )
    }
}

@Composable
private fun PlannerProgressBarSection(
    daysLeft: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$daysLeft",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "days left",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}
