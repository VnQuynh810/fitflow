package io.github.vnquynh810.fitflow.feature.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vnquynh810.fitflow.core.model.WorkoutLogEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WorkoutHistoryRoute(
    onBack: () -> Unit,
) {
    val viewModel: WorkoutHistoryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WorkoutHistoryScreen(
        globalWorkoutLogs = uiState.workoutLogs,
        onBack = onBack,
    )
}

@Composable
internal fun WorkoutHistoryScreen(
    globalWorkoutLogs: List<WorkoutLogEntry>,
    onBack: () -> Unit,
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }

    val historyEntries = remember(globalWorkoutLogs) {
        globalWorkoutLogs.sortedByDescending { it.timestampMillis }
    }
    val completedDates = remember(historyEntries) {
        historyEntries.map { LocalDate.ofEpochDay(it.dateEpochDay) }.toSet()
    }

    val today = LocalDate.now()
    val todayIndex = if (today.dayOfWeek.value == 7) 0 else today.dayOfWeek.value
    val startOfWeek = today.minusDays(todayIndex.toLong())
    val endOfWeek = startOfWeek.plusDays(6)

    val weekEntries = remember(historyEntries, startOfWeek, endOfWeek) {
        historyEntries.filter {
            val date = LocalDate.ofEpochDay(it.dateEpochDay)
            !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)
        }
    }
    val weekTotalSeconds = remember(weekEntries) { weekEntries.sumOf { it.durationSec } }
    val weekTotalKcal = remember(weekEntries) { weekEntries.sumOf { it.kcal } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "History",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        item {
            HistoryCalendarSection(
                displayedMonth = displayedMonth,
                completedDates = completedDates,
                today = today,
                onPrevMonth = { displayedMonth = displayedMonth.minusMonths(1) },
                onNextMonth = { displayedMonth = displayedMonth.plusMonths(1) },
            )
        }

        item { Spacer(Modifier.height(28.dp)) }

        item {
            Text(
                text = "Weekly Report",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        item {
            WeekSummaryCard(
                startOfWeek = startOfWeek,
                endOfWeek = endOfWeek,
                workoutCount = weekEntries.size,
                totalSeconds = weekTotalSeconds,
                totalKcal = weekTotalKcal,
            )
            Spacer(Modifier.height(2.dp))
        }

        if (weekEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No workouts this week yet.\nKeep going! 💪",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            items(weekEntries) { entry ->
                WorkoutHistoryItem(entry = entry)
            }
        }
    }
}

@Composable
private fun HistoryCalendarSection(
    displayedMonth: YearMonth,
    completedDates: Set<LocalDate>,
    today: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onBg = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = onBg)
            }
            Text(
                text = "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${displayedMonth.year}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = onBg,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = onBg)
            }
        }

        Spacer(Modifier.height(8.dp))

        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            daysOfWeek.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onBg.copy(alpha = 0.4f),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        val firstDay = displayedMonth.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7
        val daysInMonth = displayedMonth.lengthOfMonth()
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - firstDayOfWeek + 1
                    val date = if (dayNum in 1..daysInMonth) displayedMonth.atDay(dayNum) else null

                    val isToday = date == today
                    val isCompleted = date != null && date in completedDates

                    val bgColor by animateColorAsState(
                        targetValue = when {
                            isCompleted -> primary
                            isToday -> onBg
                            else -> Color.Transparent
                        },
                        animationSpec = tween(200),
                        label = "dayBg",
                    )
                    val textColor = when {
                        isCompleted || isToday -> MaterialTheme.colorScheme.background
                        else -> onBg.copy(alpha = if (date != null) 0.8f else 0f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (date != null) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(bgColor, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$dayNum",
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday || isCompleted) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor,
                                )
                            }
                            if (isToday && !isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .size(4.dp)
                                        .background(primary, CircleShape),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
