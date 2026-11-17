package io.github.vnquynh810.fitflow.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vnquynh810.fitflow.R
import java.time.Year
import java.util.Locale
import kotlin.math.abs

@Composable
fun BirthYearStep(
    birthYear: Int,
    onYearChange: (Int) -> Unit,
) {
    val currentYear = Year.now().value
    StepLayout(
        title = stringResource(R.string.onboarding_birth_year_title),
        subtitle = stringResource(R.string.onboarding_birth_year_subtitle),
    ) {
        YearWheelPicker(
            selectedYear = birthYear,
            years = (1950..currentYear).toList(),
            onYearChange = onYearChange,
        )
        Text(
            text = "$birthYear",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun TargetWeightStep(
    targetWeight: Float,
    currentWeight: Float,
    onTargetChange: (Float) -> Unit,
) {
    val safeCurrentWeight = if (currentWeight > 0f) currentWeight else 60f
    val weightDiff = targetWeight - safeCurrentWeight
    val diffPercentage = (abs(weightDiff) / safeCurrentWeight) * 100
    val isLosing = weightDiff < 0
    var useKg by remember { mutableStateOf(true) }
    val kgValue = targetWeight.coerceIn(30f, 150f)
    val lbsValue = kgValue * 2.20462f

    StepLayout(
        title = stringResource(R.string.onboarding_target_weight_title),
        subtitle = stringResource(R.string.onboarding_target_weight_subtitle),
    ) {
        UnitToggle(
            left = stringResource(R.string.onboarding_unit_kg),
            right = stringResource(R.string.onboarding_unit_lb),
            isLeftSelected = useKg,
            onToggle = { useKg = it },
        )
        Spacer(Modifier.height(20.dp))
        RulerPicker(
            selectedValue = kgValue.toInt(),
            values = (30..150).toList(),
            majorTickEvery = 5,
            minorTickStep = 1,
            onSelect = { onTargetChange(it.toFloat()) },
            majorTickColor = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = if (useKg) {
                stringResource(R.string.onboarding_target_weight_value_kg_format, kgValue.toInt())
            } else {
                stringResource(R.string.onboarding_weight_value_lb_format, lbsValue)
            },
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.onboarding_target_challenge_title),
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isLosing) {
                        stringResource(
                            R.string.onboarding_target_losing_body_format,
                            String.format(Locale.getDefault(), "%.1f", diffPercentage),
                        )
                    } else {
                        stringResource(
                            R.string.onboarding_target_gaining_body_format,
                            String.format(Locale.getDefault(), "%.1f", diffPercentage),
                        )
                    },
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTimeStep(
    workoutTime: String,
    keySeed: Any = Unit,
    onTimeSelected: (String) -> Unit,
) {
    key(keySeed) {
        val initialHour = workoutTime.split(":").getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
        val initialMinute = workoutTime.split(":").getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true,
        )

        StepLayout(
            title = stringResource(R.string.onboarding_reminder_title),
            subtitle = stringResource(R.string.onboarding_reminder_subtitle),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = FontWeight.Black),
                ) {
                    TimeInput(
                        state = timePickerState,
                        modifier = Modifier,
                        colors = TimePickerDefaults.colors(
                            selectorColor = MaterialTheme.colorScheme.primary,
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                            periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
                LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                    onTimeSelected(
                        String.format(
                            Locale.US,
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute,
                        )
                    )
                }
                Text(
                    text = stringResource(R.string.onboarding_reminder_helper_text),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
