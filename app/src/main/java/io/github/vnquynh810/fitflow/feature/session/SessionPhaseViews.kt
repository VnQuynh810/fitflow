package io.github.vnquynh810.fitflow.feature.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise

@Composable
internal fun ColumnScope.PreparingPhaseContent(
    currentExercise: WorkoutExercise?,
    gifUrl: String?,
    countdownRemaining: Int,
    isBgMusicEnabled: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleMusic: () -> Unit,
    onOpenInstructions: () -> Unit,
    onSkip: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val buttonBgColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant

    ExerciseMediaArea(
        gifUrl = gifUrl,
        prepareOverlay = true,
        prepareCountdown = countdownRemaining,
        isBgMusicEnabled = isBgMusicEnabled,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onToggleMusic = onToggleMusic,
        modifier = Modifier.weight(1f),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "GET READY",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = primaryColor,
                fontSize = 32.sp,
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = currentExercise?.name.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onOpenInstructions,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "Help",
                    modifier = Modifier.size(20.dp),
                    tint = iconColor,
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBgColor,
                contentColor = textColor,
            ),
            shape = RoundedCornerShape(28.dp),
        ) {
            Text("Skip", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
internal fun ColumnScope.ExercisingPhaseContent(
    currentExercise: WorkoutExercise?,
    gifUrl: String?,
    remainingSeconds: Int,
    isRunning: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    isBgMusicEnabled: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleMusic: () -> Unit,
    onOpenInstructions: () -> Unit,
    onPrevious: () -> Unit,
    onPrimaryAction: () -> Unit,
    onNext: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val reps = currentExercise?.reps ?: 0

    ExerciseMediaArea(
        gifUrl = gifUrl,
        prepareOverlay = false,
        prepareCountdown = 0,
        isBgMusicEnabled = isBgMusicEnabled,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onToggleMusic = onToggleMusic,
        modifier = Modifier.weight(1f),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
    ) {
        Text(
            text = if (reps > 0) "× $reps" else "${remainingSeconds}s",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 56.sp,
                lineHeight = 56.sp,
            ),
            color = textColor,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = currentExercise?.name ?: "Exercise",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onOpenInstructions,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "Help",
                    modifier = Modifier.size(20.dp),
                    tint = iconColor,
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedIconButton(
            onClick = onPrevious,
            enabled = hasPrevious,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
        ) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = textColor)
        }

        Button(
            onClick = onPrimaryAction,
            modifier = Modifier
                .height(64.dp)
                .weight(1f)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(32.dp),
        ) {
            val icon = when {
                reps > 0 -> Icons.Default.Check
                isRunning -> Icons.Default.Pause
                else -> Icons.Default.PlayArrow
            }
            val desc = when {
                reps > 0 -> "Complete exercise"
                isRunning -> "Pause timer"
                else -> "Start timer"
            }
            Icon(icon, contentDescription = desc, modifier = Modifier.size(36.dp))
        }

        OutlinedIconButton(
            onClick = onNext,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
        ) {
            Icon(
                imageVector = if (hasNext) Icons.Default.SkipNext else Icons.Default.Check,
                contentDescription = if (hasNext) "Next" else "Finish",
                tint = textColor,
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}
