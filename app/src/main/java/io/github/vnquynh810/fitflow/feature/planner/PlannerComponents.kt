package io.github.vnquynh810.fitflow.feature.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Coffee
import com.composables.icons.lucide.Lucide
import io.github.vnquynh810.fitflow.core.model.DayPlan

@Composable
internal fun DayPlanItemCard(
    dayPlan: DayPlan,
    isCompleted: Boolean,
    isCurrent: Boolean,
    partialIndex: Int?,
    onClick: () -> Unit,
    onRestClick: () -> Unit,
) {
    val isActive = !isCompleted && (isCurrent || partialIndex != null)
    val bgColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
    val subTextColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isActive) Color.Transparent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 20.dp)
                .defaultMinSize(minHeight = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Day ${dayPlan.dayNumber}",
                    color = textColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitleText = when {
                    isCompleted -> "Finished"
                    dayPlan.isRest -> "Rest"
                    partialIndex != null -> {
                        val total = dayPlan.workoutExercises.size
                        val percent = if (total > 0) (partialIndex * 100) / total else 0
                        "$percent% Completed"
                    }
                    else -> "${dayPlan.workoutExercises.size} Exercises"
                }
                Text(
                    text = subtitleText,
                    color = subTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Finished",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else if (isActive) {
                if (dayPlan.isRest) {
                    Button(
                        onClick = onRestClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(40.dp),
                    ) {
                        Icon(Lucide.Coffee, contentDescription = "Rest", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REST", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(40.dp),
                    ) {
                        Text(
                            text = if (partialIndex != null) "CONTINUE" else "START",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                        )
                    }
                }
            } else if (dayPlan.isRest) {
                Icon(
                    Lucide.Coffee,
                    contentDescription = "Rest",
                    tint = subTextColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
internal fun ResetPlanDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Reset toàn bộ tiến trình?",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        text = {
            Text(
                text = "Toàn bộ ngày đã tập của kế hoạch này sẽ bị xoá và bạn sẽ bắt đầu lại từ Day 1.\n\nHành động này không thể hoàn tác.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                lineHeight = 20.sp,
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Huỷ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Reset",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onError,
                )
            }
        },
    )
}
