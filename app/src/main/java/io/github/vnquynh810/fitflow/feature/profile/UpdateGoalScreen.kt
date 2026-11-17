package io.github.vnquynh810.fitflow.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vnquynh810.fitflow.core.model.FitnessGoal

@Composable
fun UpdateGoalRoute(
    onBack: () -> Unit,
    onGoalSelectedForProvisioning: (FitnessGoal) -> Unit,
) {
    val viewModel: UpdateGoalViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.provisioningGoalEvents.collect { selectedGoal ->
            onGoalSelectedForProvisioning(selectedGoal)
        }
    }

    UpdateGoalScreen(
        uiState = uiState,
        onSelectGoal = viewModel::selectGoal,
        onFinalize = viewModel::onFinalizeClicked,
        onDismissResumeDialog = viewModel::dismissResumeDialog,
        onConfirmResetToDayOne = { viewModel.confirmGoalChoice(resetProgress = true) },
        onConfirmResumeExisting = { viewModel.confirmGoalChoice(resetProgress = false) },
        onBack = onBack,
    )
}

@Composable
internal fun UpdateGoalScreen(
    uiState: UpdateGoalUiState,
    onSelectGoal: (FitnessGoal) -> Unit,
    onFinalize: () -> Unit,
    onDismissResumeDialog: () -> Unit,
    onConfirmResetToDayOne: () -> Unit,
    onConfirmResumeExisting: () -> Unit,
    onBack: () -> Unit,
) {
    uiState.resumePrompt?.let { prompt ->
        ResumeGoalDialog(
            prompt = prompt,
            onDismiss = onDismissResumeDialog,
            onStartFromDayOne = onConfirmResetToDayOne,
            onResumeProgress = onConfirmResumeExisting,
        )
    }

    val currentGoal = uiState.currentGoal
    val selectedGoal = uiState.selectedGoal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row {
                    Text(
                        text = "FITNESS GOAL ",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "UPDATE",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "CHOOSE YOUR NEW GOAL",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FitnessGoal.entries
                    .filter { it != currentGoal }
                    .forEach { goal ->
                        GoalOptionCard(
                            title = goal.title,
                            desc = goal.description,
                            isSelected = selectedGoal == goal,
                            onClick = { onSelectGoal(goal) },
                        )
                    }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CURRENT GOAL",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentGoal.title,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = currentGoal.description,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(MaterialTheme.colorScheme.background),
        ) {
            Button(
                onClick = onFinalize,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "FINALIZE PROTOCOL",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalOptionCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onBackground
    }
    val descColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Column {
            Text(
                text = title.uppercase(),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Text(
                text = desc.uppercase(),
                color = descColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun ResumeGoalDialog(
    prompt: ResumeGoalPrompt,
    onDismiss: () -> Unit,
    onStartFromDayOne: () -> Unit,
    onResumeProgress: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Khôi phục tiến trình?",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        text = {
            Text(
                text = "Bạn đã hoàn thành ${prompt.completedDaysCount} ngày của kế hoạch " +
                    "${prompt.goal.title} trước đó.\n\n" +
                    "Bạn muốn tập tiếp Day ${prompt.completedDaysCount + 1} hay bắt đầu lại từ Day 1?",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                lineHeight = 20.sp,
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onStartFromDayOne,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Day 1",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onResumeProgress,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Tiếp tục Day ${prompt.completedDaysCount + 1}",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
    )
}
