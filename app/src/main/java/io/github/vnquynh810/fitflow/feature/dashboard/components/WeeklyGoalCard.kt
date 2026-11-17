package io.github.vnquynh810.fitflow.feature.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.Lucide
import io.github.vnquynh810.fitflow.R
import io.github.vnquynh810.fitflow.core.model.WorkoutLogEntry
import java.time.LocalDate

@Composable
fun WeeklyGoalCard(
    globalWorkoutLogs: List<WorkoutLogEntry>,
    currentStreak: Int,
    longestStreak: Int,
    onViewHistory: () -> Unit,
    onToggleDay: (Int) -> Unit,
) {
    val today = LocalDate.now()
    val todayIndex = if (today.dayOfWeek.value == 7) 0 else today.dayOfWeek.value
    val startOfWeek = today.minusDays(todayIndex.toLong())
    val completedDates = remember(globalWorkoutLogs) {
        globalWorkoutLogs.map { LocalDate.ofEpochDay(it.dateEpochDay) }.toSet()
    }

    var todayOffsetX by remember { mutableFloatStateOf(0f) }
    var rowWidth by remember { mutableFloatStateOf(0f) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_weekly_goal),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                IconButton(
                    onClick = onViewHistory,
                    modifier = Modifier.size(20.dp),
                ) {
                    Icon(
                        Lucide.Eye,
                        contentDescription = "View History",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        rowWidth = coords.size.width.toFloat()
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                for (i in 0..6) {
                    val date = startOfWeek.plusDays(i.toLong())
                    val isToday = i == todayIndex
                    val isPast = i < todayIndex
                    val isCompleted = date in completedDates

                    val bgColor = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.onBackground
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        isCompleted -> Color.White
                        isToday -> MaterialTheme.colorScheme.background
                        isPast -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = if (isToday) {
                            Modifier.onGloballyPositioned { coords ->
                                todayOffsetX = coords.positionInParent().x + coords.size.width / 2f
                            }
                        } else {
                            Modifier
                        },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(bgColor, CircleShape)
                                .clickable { onToggleDay(i) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${date.dayOfMonth}",
                                fontSize = 14.sp,
                                fontWeight = if (isToday || isCompleted) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val arrowColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
            if (rowWidth > 0f && todayOffsetX > 0f) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                ) {
                    val arrowW = 16.dp.toPx()
                    val arrowH = 8.dp.toPx()
                    val tipX = todayOffsetX.coerceIn(arrowW / 2f, size.width - arrowW / 2f)
                    val path = Path().apply {
                        moveTo(tipX, 0f)
                        lineTo(tipX + arrowW / 2f, arrowH)
                        lineTo(tipX - arrowW / 2f, arrowH)
                        close()
                    }
                    drawPath(path = path, color = arrowColor)
                }
            }

            if (currentStreak > 0 || longestStreak > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "CURRENT STREAK",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(text = "🔥", fontSize = 16.sp)
                            Text(
                                text = "$currentStreak day${if (currentStreak != 1) "s" else ""}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF6B00),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)),
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LONGEST STREAK",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "$longestStreak day${if (longestStreak != 1) "s" else ""}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                            )
                            if (currentStreak > 0 && currentStreak >= longestStreak) {
                                Text(text = "🏆", fontSize = 16.sp)
                            }
                        }
                        if (currentStreak > 0 && currentStreak >= longestStreak) {
                            Text(
                                text = "NEW RECORD!",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color(0xFFFFAA00),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("💪", fontSize = 22.sp)
                    }
                    Text(
                        text = quoteForDay(todayIndex),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun quoteForDay(dayIndex: Int): String = when (dayIndex) {
    0 -> stringResource(R.string.dashboard_quote_sunday)
    1 -> stringResource(R.string.dashboard_quote_monday)
    2 -> stringResource(R.string.dashboard_quote_tuesday)
    3 -> stringResource(R.string.dashboard_quote_wednesday)
    4 -> stringResource(R.string.dashboard_quote_thursday)
    5 -> stringResource(R.string.dashboard_quote_friday)
    6 -> stringResource(R.string.dashboard_quote_saturday)
    else -> stringResource(R.string.dashboard_quote_default)
}
