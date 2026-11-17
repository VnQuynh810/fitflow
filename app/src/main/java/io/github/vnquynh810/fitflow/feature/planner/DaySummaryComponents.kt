package io.github.vnquynh810.fitflow.feature.planner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CircularProgressSection(
    isCompleted: Boolean,
    isRestDay: Boolean,
    noPlan: Boolean,
    animatedProgress: Float,
    totalMinutes: Int,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)

    val emoji = when {
        noPlan -> "😴"
        isRestDay -> "🧘"
        isCompleted -> "🏆"
        else -> "😊"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                val inset = strokeWidth / 2f

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                )

                if (!noPlan && !isRestDay) {
                    drawArc(
                        brush = Brush.sweepGradient(colors = listOf(primaryColor, secondaryColor, primaryColor)),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    )
                }
            }
            Text(emoji, fontSize = 44.sp)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (isCompleted) totalMinutes.toString() else "0",
            fontWeight = FontWeight.Black,
            fontSize = 42.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = when {
                noPlan -> "No plan for this day"
                isRestDay -> "Rest day"
                else -> "/$totalMinutes min"
            },
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        )

        Spacer(Modifier.height(12.dp))

        val (pillText, pillColor) = when {
            noPlan -> "OUT OF PLAN" to MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            isRestDay -> "REST DAY" to MaterialTheme.colorScheme.secondary
            isCompleted -> "COMPLETED" to MaterialTheme.colorScheme.primary
            else -> "PENDING" to MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(pillColor.copy(alpha = 0.12f))
                .border(1.dp, pillColor.copy(alpha = 0.3f), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Text(
                text = pillText,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = pillColor,
            )
        }
    }
}

@Composable
internal fun PlanProgressSection(
    completedDaysCount: Int,
    totalDaysCount: Int,
    modifier: Modifier = Modifier,
) {
    val progress = if (totalDaysCount > 0) completedDaysCount.toFloat() / totalDaysCount else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Plan Progress",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "$completedDaysCount / $totalDaysCount Days",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
internal fun OverviewSection(
    totalKcal: Int,
    activityCount: Int,
    exerciseCount: Int,
    currentStreak: Int,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Overview",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                label = "Total",
                value = "$totalKcal Kcal",
                icon = Icons.Default.LocalFireDepartment,
                iconTint = MaterialTheme.colorScheme.primary,
            )
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                label = "Activity",
                value = activityCount.toString(),
                icon = Icons.Default.FitnessCenter,
                iconTint = MaterialTheme.colorScheme.secondary,
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                label = "Exercises",
                value = exerciseCount.toString(),
                icon = Icons.Default.Schedule,
                iconTint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
            OverviewStatCard(
                modifier = Modifier.weight(1f),
                label = "Streak",
                value = "$currentStreak Day",
                icon = Icons.Default.Whatshot,
                iconTint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun OverviewStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
) {
    Card(
        modifier = modifier.border(
            1.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f),
            RoundedCornerShape(20.dp),
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                    letterSpacing = 0.5.sp,
                )
            }
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
