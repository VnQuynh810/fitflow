package io.github.vnquynh810.fitflow.feature.session

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vnquynh810.fitflow.core.domain.usecase.BmiCalculator
import java.util.Locale

@Composable
internal fun CompletedStatItem(label: String, value: String, primaryColor: Color, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, color = primaryColor, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun FeedbackButton(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    primaryColor: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) primaryColor.copy(alpha = 0.2f) else Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(text = emoji, fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = if (isSelected) primaryColor else textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
internal fun BmiSummaryCard(
    heightCm: Float,
    weightKg: Float,
    cardColor: Color,
    textColor: Color,
) {
    val bmi = BmiCalculator.calculate(heightCm = heightCm, weightKg = weightKg)
    val (bmiCategoryLabel, categoryColor) = when {
        bmi < 18.5f -> "Underweight" to Color(0xFF42A5F5)
        bmi < 25f -> "Normal" to Color(0xFF66BB6A)
        bmi < 30f -> "Overweight" to Color(0xFFFFA726)
        else -> "Obese" to Color(0xFFEF5350)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "BMI (kg/m²)",
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = String.format(Locale.US, "%.1f", bmi),
                color = textColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(categoryColor),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "$bmiCategoryLabel weight", color = textColor, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            BmiSlider(bmi = bmi)
        }
    }
}

@Composable
private fun BmiSlider(bmi: Float) {
    val fraction = ((bmi - 15f) / (40f - 15f)).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val barHeight = 8.dp.toPx()
                val yOffset = size.height - barHeight
                val brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF42A5F5),
                        Color(0xFF66BB6A),
                        Color(0xFFFFA726),
                        Color(0xFFEF5350),
                    )
                )
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(0f, yOffset),
                    size = Size(width, barHeight),
                    cornerRadius = CornerRadius(barHeight / 2, barHeight / 2),
                )
                val pointerX = width * fraction
                val trianglePath = Path().apply {
                    moveTo(pointerX, yOffset)
                    lineTo(pointerX - 8.dp.toPx(), yOffset - 12.dp.toPx())
                    lineTo(pointerX + 8.dp.toPx(), yOffset - 12.dp.toPx())
                    close()
                }
                drawPath(path = trianglePath, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("15", "18.5", "25", "30", "35", "40").forEach { value ->
                Text(text = value, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
