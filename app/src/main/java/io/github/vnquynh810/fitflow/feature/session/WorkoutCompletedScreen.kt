package io.github.vnquynh810.fitflow.feature.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.UserProfile
import java.time.LocalDate
import java.util.Locale

@Composable
fun WorkoutCompletedRoute(
    dayNumber: Int?,
    supplementaryId: String?,
    activeSeconds: Int,
    onFinishToDashboard: () -> Unit,
) {
    val viewModel: WorkoutCompletedViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(dayNumber, supplementaryId) {
        viewModel.initialize(dayNumber = dayNumber, supplementaryId = supplementaryId)
    }

    WorkoutCompletedScreen(
        dayPlan = uiState.dayPlan,
        fallbackDayNumber = dayNumber,
        totalActiveSeconds = activeSeconds,
        userProfile = uiState.userProfile,
        onSaveWeight = viewModel::saveWeight,
        onNext = onFinishToDashboard,
    )
}

@Composable
internal fun WorkoutCompletedScreen(
    dayPlan: DayPlan?,
    fallbackDayNumber: Int?,
    totalActiveSeconds: Int,
    userProfile: UserProfile?,
    onSaveWeight: (Float, LocalDate) -> Unit,
    onNext: () -> Unit,
) {
    val totalExercises = dayPlan?.workoutExercises?.size ?: 0
    val estimatedCalories = dayPlan?.workoutExercises?.sumOf { it.kcal } ?: 0
    val timeFormatted = String.format(
        Locale.US,
        "%02d:%02d",
        totalActiveSeconds / 60,
        totalActiveSeconds % 60,
    )

    var selectedFeedback by remember { mutableStateOf<String?>(null) }
    val profileWeight = userProfile?.weight ?: 70f
    var currentWeightKg by remember(profileWeight) { mutableFloatStateOf(profileWeight) }
    var showWeightSheet by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val headerSubtitle = remember(dayPlan, fallbackDayNumber) {
        when {
            dayPlan != null && dayPlan.dayNumber == 0 -> dayPlan.title
            dayPlan != null && dayPlan.title.isNotBlank() -> "Day ${dayPlan.dayNumber} · ${dayPlan.title}"
            dayPlan != null -> "Day ${dayPlan.dayNumber}"
            fallbackDayNumber != null -> "Day $fallbackDayNumber"
            else -> ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors = listOf(primaryColor, Color(0xFF1E1E1E))))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp, vertical = 48.dp),
        ) {
            Column {
                Text(
                    text = "Workout\nCompleted!",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 42.sp,
                )
                if (headerSubtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = headerSubtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    CompletedStatItem("Exercises", totalExercises.toString(), primaryColor, textColor)
                    CompletedStatItem("Calories Burned", estimatedCalories.toString(), primaryColor, textColor)
                    CompletedStatItem("Time", timeFormatted, primaryColor, textColor)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "How do you feel?",
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your feedback will help us provide more suitable workouts for you",
                        color = subTextColor,
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        FeedbackButton("😩", "Too hard", selectedFeedback == "Too hard", { selectedFeedback = "Too hard" }, textColor, primaryColor)
                        FeedbackButton("🙂", "Just right", selectedFeedback == "Just right", { selectedFeedback = "Just right" }, textColor, primaryColor)
                        FeedbackButton("😎", "Too easy", selectedFeedback == "Too easy", { selectedFeedback = "Too easy" }, textColor, primaryColor)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWeightSheet = true },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Weight", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f KG", currentWeightKg),
                            color = primaryColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Text("Edit", color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            BmiSummaryCard(
                heightCm = userProfile?.height ?: 170f,
                weightKg = currentWeightKg,
                cardColor = cardColor,
                textColor = textColor,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(28.dp),
            ) {
                Text(
                    text = "BACK TO DASHBOARD",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    if (showWeightSheet) {
        SessionWeightPickerSheet(
            initialWeight = currentWeightKg,
            onDismiss = { showWeightSheet = false },
            onSave = { newWeight, date ->
                currentWeightKg = newWeight
                onSaveWeight(newWeight, date)
                showWeightSheet = false
            },
        )
    }
}
