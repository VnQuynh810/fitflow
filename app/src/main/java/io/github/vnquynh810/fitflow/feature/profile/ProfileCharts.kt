package io.github.vnquynh810.fitflow.feature.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vnquynh810.fitflow.core.model.WeightRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun WeightTrackingSection(
    currentWeight: Float?,
    targetWeight: Float?,
    weightHistory: List<WeightRecord>,
    onUpdateWeightClick: () -> Unit,
) {
    val latest = weightHistory.lastOrNull()?.weightKg ?: currentWeight
    val progressText = if (latest != null && targetWeight != null) {
        val diff = latest - targetWeight
        "${String.format(Locale.US, "%.1f", diff)} kg to goal"
    } else {
        "No target set"
    }
    val todayLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(32.dp),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Weight",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = todayLabel,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            RoundedCornerShape(16.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }

            Text(
                text = latest?.let { String.format(Locale.US, "%.1f", it) } ?: "-",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Text(
                text = progressText,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            WeightHistoryMiniChart(
                history = weightHistory,
                color = MaterialTheme.colorScheme.primary,
            )

            Button(
                onClick = onUpdateWeightClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    text = "UPDATE WEIGHT",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

@Composable
internal fun WeightHistoryMiniChart(
    history: List<WeightRecord>,
    color: Color,
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No weight records yet",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 12.sp,
            )
        }
        return
    }

    val maxValue = history.maxOfOrNull { it.weightKg } ?: 1f
    val minValue = history.minOfOrNull { it.weightKg } ?: 0f
    val range = (maxValue - minValue).coerceAtLeast(1f)
    val yMax = maxValue + (range * 0.1f)
    val yMin = (minValue - (range * 0.1f)).coerceAtLeast(0f)
    val yRange = (yMax - yMin).coerceAtLeast(1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 24.dp, end = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
        ) {
            Text(String.format(Locale.US, "%.1f", yMax), fontSize = 10.sp, color = Color(0xFFAEB5C4), fontWeight = FontWeight.Medium)
            Text(String.format(Locale.US, "%.1f", (yMax + yMin) / 2), fontSize = 10.sp, color = Color(0xFFAEB5C4), fontWeight = FontWeight.Medium)
            Text(String.format(Locale.US, "%.1f", yMin), fontSize = 10.sp, color = Color(0xFFAEB5C4), fontWeight = FontWeight.Medium)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                repeat(3) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), thickness = 1.dp)
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp, start = 4.dp, end = 4.dp),
            ) {
                val width = size.width
                val height = size.height
                val stepX = if (history.size > 1) width / (history.size - 1) else width / 2
                val startX = if (history.size == 1) width / 2 else 0f

                val coordinates = history.mapIndexed { index, record ->
                    val ratio = ((record.weightKg - yMin) / yRange).coerceIn(0f, 1f)
                    Offset(startX + index * stepX, height - (ratio * height))
                }

                if (coordinates.size > 1) {
                    val path = Path().apply {
                        moveTo(coordinates.first().x, coordinates.first().y)
                        for (i in 1 until coordinates.size) {
                            lineTo(coordinates[i].x, coordinates[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.7f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }

                coordinates.forEach { offset ->
                    drawCircle(color = color, radius = 4.dp.toPx(), center = offset)
                    drawCircle(color = Color(0xFF171A21), radius = 2.dp.toPx(), center = offset)
                }

                val paint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.parseColor("#AEB5C4")
                    this.textSize = 10.sp.toPx()
                    this.textAlign = android.graphics.Paint.Align.CENTER
                    this.isAntiAlias = true
                }

                val labelIndices = if (history.size <= 5) {
                    history.indices.toList()
                } else {
                    listOf(0, history.size / 2, history.lastIndex)
                }

                labelIndices.forEach { index ->
                    val x = startX + index * stepX
                    val date = history[index].date
                    drawContext.canvas.nativeCanvas.drawText(
                        "${date.dayOfMonth}/${date.monthValue}",
                        x,
                        height + 18.dp.toPx(),
                        paint,
                    )
                }
            }
        }
    }
}
