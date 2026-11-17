package io.github.vnquynh810.fitflow.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vnquynh810.fitflow.R
import io.github.vnquynh810.fitflow.core.domain.usecase.BmiCalculator
import io.github.vnquynh810.fitflow.core.model.BmiCategory
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import java.util.Locale

@Composable
fun GoalStep(
    selectedGoal: FitnessGoal,
    onGoalSelected: (FitnessGoal) -> Unit,
) {
    StepLayout(
        title = stringResource(R.string.onboarding_goal_title),
        subtitle = stringResource(R.string.onboarding_goal_subtitle),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FitnessGoal.entries.forEach { goal ->
                val isSelected = goal == selectedGoal
                Surface(
                    onClick = { onGoalSelected(goal) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(0.1f)
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(goal.titleRes()),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            )
                            Text(
                                text = stringResource(goal.descriptionRes()),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        RadioButton(selected = isSelected, onClick = null)
                    }
                }
            }
        }
    }
}

private fun FitnessGoal.titleRes(): Int = when (this) {
    FitnessGoal.WEIGHT_LOSS -> R.string.onboarding_goal_weight_loss_title
    FitnessGoal.MUSCLE_GAIN -> R.string.onboarding_goal_muscle_gain_title
    FitnessGoal.ENDURANCE -> R.string.onboarding_goal_endurance_title
    FitnessGoal.MAINTENANCE -> R.string.onboarding_goal_maintenance_title
}

private fun FitnessGoal.descriptionRes(): Int = when (this) {
    FitnessGoal.WEIGHT_LOSS -> R.string.onboarding_goal_weight_loss_desc
    FitnessGoal.MUSCLE_GAIN -> R.string.onboarding_goal_muscle_gain_desc
    FitnessGoal.ENDURANCE -> R.string.onboarding_goal_endurance_desc
    FitnessGoal.MAINTENANCE -> R.string.onboarding_goal_maintenance_desc
}

@Composable
fun HeightStep(
    height: Float,
    onHeightChange: (Float) -> Unit,
) {
    var useCm by remember { mutableStateOf(true) }
    val cmValue = height.toInt().coerceIn(120, 220)
    val inchValue = (cmValue / 2.54f).toInt()
    val feet = inchValue / 12
    val inches = inchValue % 12

    StepLayout(
        title = stringResource(R.string.onboarding_height_title),
        subtitle = stringResource(R.string.onboarding_height_subtitle),
    ) {
        UnitToggle(
            left = stringResource(R.string.onboarding_unit_cm),
            right = stringResource(R.string.onboarding_unit_ft),
            isLeftSelected = useCm,
            onToggle = { useCm = it },
        )
        Spacer(Modifier.height(20.dp))
        RulerPicker(
            selectedValue = cmValue,
            values = (120..220).toList(),
            majorTickEvery = 5,
            minorTickStep = 1,
            onSelect = { onHeightChange(it.toFloat()) },
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = if (useCm) {
                stringResource(R.string.onboarding_height_value_cm_format, cmValue)
            } else {
                stringResource(R.string.onboarding_height_value_ft_in_format, feet, inches)
            },
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun WeightStep(
    weight: Float,
    onWeightChange: (Float) -> Unit,
) {
    var useKg by remember { mutableStateOf(true) }
    val kgValue = weight.coerceIn(30f, 150f)
    val lbsValue = kgValue * 2.20462f

    StepLayout(
        title = stringResource(R.string.onboarding_weight_title),
        subtitle = stringResource(R.string.onboarding_weight_subtitle),
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
            onSelect = { onWeightChange(it.toFloat()) },
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = if (useKg) {
                stringResource(R.string.onboarding_weight_value_kg_format, kgValue)
            } else {
                stringResource(R.string.onboarding_weight_value_lb_format, lbsValue)
            },
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun BmiInfoStep(
    weight: Float,
    height: Float,
) {
    val bmi = BmiCalculator.calculate(heightCm = height, weightKg = weight)
    val category = BmiCalculator.categoryFor(bmi)

    val categoryLabel = when (category) {
        BmiCategory.UNDERWEIGHT -> stringResource(R.string.onboarding_bmi_label_underweight)
        BmiCategory.NORMAL -> stringResource(R.string.onboarding_bmi_label_normal)
        BmiCategory.OVERWEIGHT -> stringResource(R.string.onboarding_bmi_label_overweight)
    }

    val guidance = when (category) {
        BmiCategory.UNDERWEIGHT -> stringResource(R.string.onboarding_bmi_guidance_underweight)
        BmiCategory.NORMAL -> stringResource(R.string.onboarding_bmi_guidance_normal)
        BmiCategory.OVERWEIGHT -> stringResource(R.string.onboarding_bmi_guidance_overweight)
    }

    StepLayout(
        title = stringResource(R.string.onboarding_bmi_title),
        subtitle = stringResource(R.string.onboarding_bmi_subtitle),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", bmi),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Normal,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = categoryLabel,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            ),
        ) {
            Text(
                text = guidance,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
