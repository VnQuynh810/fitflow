package io.github.vnquynh810.fitflow.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Footprints
import com.composables.icons.lucide.GlassWater
import com.composables.icons.lucide.Lucide
import io.github.vnquynh810.fitflow.R
import io.github.vnquynh810.fitflow.core.model.DailyHealthMetrics

@Composable
fun HealthMetricsSection(
    metrics: DailyHealthMetrics,
    isActivityRecognitionGranted: Boolean,
    isStepSensorEnabled: Boolean,
    isStepTrackingActive: Boolean,
    onUnlockStepSensor: () -> Unit,
    onAddWater: (Int) -> Unit,
    onSetWaterGoal: (Int) -> Unit,
    onSetStepGoal: (Int) -> Unit,
) {
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var showStepGoalDialog by remember { mutableStateOf(false) }
    var showAddWaterDialog by remember { mutableStateOf(false) }
    var waterGoalInput by remember(metrics.waterGoalMl) { mutableStateOf(metrics.waterGoalMl.toString()) }
    var stepGoalInput by remember(metrics.stepGoal) { mutableStateOf(metrics.stepGoal.toString()) }
    var customWaterInput by remember { mutableStateOf("") }

    val stepProgress = (metrics.steps.toFloat() / metrics.stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val waterProgress = (metrics.waterIntakeMl.toFloat() / metrics.waterGoalMl.coerceAtLeast(1)).coerceIn(0f, 1f)

    Text(
        text = stringResource(R.string.dashboard_health_metrics),
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp,
    )
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MetricHorizontalCard(
            modifier = Modifier.weight(1f),
            icon = Lucide.Footprints,
            iconTint = MaterialTheme.colorScheme.primary,
            value = metrics.steps.toString(),
            unit = "/ ${metrics.stepGoal} STEPS",
            progress = stepProgress,
            progressColor = MaterialTheme.colorScheme.primary,
            buttonText = when {
                !isActivityRecognitionGranted -> stringResource(R.string.dashboard_unlock)
                !isStepSensorEnabled -> stringResource(R.string.dashboard_manual_mode)
                isStepTrackingActive -> stringResource(R.string.dashboard_live_sensor)
                else -> stringResource(R.string.dashboard_sensor_ready)
            },
            onClick = onUnlockStepSensor,
            secondaryButtonText = "SET GOAL",
            onSecondaryClick = { showStepGoalDialog = true },
        )
        MetricHorizontalCard(
            modifier = Modifier.weight(1f),
            icon = Lucide.GlassWater,
            iconTint = MaterialTheme.colorScheme.secondary,
            value = metrics.waterIntakeMl.toString(),
            unit = "/ ${metrics.waterGoalMl} ML",
            progress = waterProgress,
            progressColor = MaterialTheme.colorScheme.secondary,
            buttonText = "+ ADD WATER",
            onClick = { showAddWaterDialog = true },
            secondaryButtonText = "GOAL",
            onSecondaryClick = { showWaterGoalDialog = true },
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = when {
            !isActivityRecognitionGranted -> stringResource(R.string.dashboard_steps_permission_off)
            !isStepSensorEnabled -> stringResource(R.string.dashboard_steps_unavailable)
            !isStepTrackingActive -> stringResource(R.string.dashboard_steps_tracking_paused)
            else -> stringResource(R.string.dashboard_steps_live_active)
        },
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
    )

    if (showAddWaterDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddWaterDialog = false
                customWaterInput = ""
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.GlassWater,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Text("Add Water", fontWeight = FontWeight.Black, fontSize = 22.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(150, 250, 350, 500).forEach { amount ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                    .clickable {
                                        onAddWater(amount)
                                        showAddWaterDialog = false
                                        customWaterInput = ""
                                    }
                                    .padding(vertical = 12.dp),
                            ) {
                                Text(
                                    text = "${amount}ml",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = customWaterInput,
                        onValueChange = { customWaterInput = it.filter(Char::isDigit) },
                        singleLine = true,
                        label = { Text("Custom amount (ml)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        ),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = customWaterInput.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onAddWater(parsed)
                            showAddWaterDialog = false
                            customWaterInput = ""
                        }
                    },
                    enabled = customWaterInput.toIntOrNull()?.let { it > 0 } == true,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                ) { Text("Add", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddWaterDialog = false
                    customWaterInput = ""
                }) {
                    Text(
                        stringResource(R.string.common_cancel),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            },
        )
    }

    if (showWaterGoalDialog) {
        GoalNumberDialog(
            title = stringResource(R.string.dashboard_water_goal_title),
            icon = Lucide.GlassWater,
            accentColor = MaterialTheme.colorScheme.secondary,
            presets = listOf(1500 to "1.5L", 2000 to "2.0L", 2500 to "2.5L", 3000 to "3.0L"),
            inputValue = waterGoalInput,
            onInputChange = { waterGoalInput = it.filter(Char::isDigit) },
            onSelectPreset = {
                onSetWaterGoal(it)
                showWaterGoalDialog = false
            },
            onSave = {
                waterGoalInput.toIntOrNull()?.takeIf { it > 0 }?.let {
                    onSetWaterGoal(it)
                    showWaterGoalDialog = false
                }
            },
            onDismiss = { showWaterGoalDialog = false },
        )
    }

    if (showStepGoalDialog) {
        GoalNumberDialog(
            title = "Set Step Goal",
            icon = Lucide.Footprints,
            accentColor = MaterialTheme.colorScheme.primary,
            presets = listOf(5000 to "5K", 8000 to "8K", 10000 to "10K", 12000 to "12K"),
            inputValue = stepGoalInput,
            onInputChange = { stepGoalInput = it.filter(Char::isDigit) },
            onSelectPreset = {
                onSetStepGoal(it)
                showStepGoalDialog = false
            },
            onSave = {
                stepGoalInput.toIntOrNull()?.takeIf { it > 0 }?.let {
                    onSetStepGoal(it)
                    showStepGoalDialog = false
                }
            },
            onDismiss = { showStepGoalDialog = false },
        )
    }
}

@Composable
private fun GoalNumberDialog(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    presets: List<Pair<Int, String>>,
    inputValue: String,
    onInputChange: (String) -> Unit,
    onSelectPreset: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                }
                Text(title, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { (goalVal, label) ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.1f))
                                .clickable { onSelectPreset(goalVal) }
                                .padding(vertical = 12.dp),
                        ) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                    }
                }
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = onInputChange,
                    singleLine = true,
                    label = { Text(stringResource(R.string.dashboard_goal_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        focusedLabelColor = accentColor,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(stringResource(R.string.common_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.common_cancel),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        },
    )
}

@Composable
private fun MetricHorizontalCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    unit: String,
    progress: Float,
    progressColor: Color,
    buttonText: String,
    onClick: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.border(
            1.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
            RoundedCornerShape(24.dp),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                if (secondaryButtonText != null && onSecondaryClick != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f))
                            .clickable { onSecondaryClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = secondaryButtonText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 1.sp,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.12f),
            )
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = buttonText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}
