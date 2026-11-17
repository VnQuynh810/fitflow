package io.github.vnquynh810.fitflow.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.vnquynh810.fitflow.core.model.Exercise
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise
import io.github.vnquynh810.fitflow.core.platform.GifUrlProvider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseInstructionSheet(
    title: String,
    gifFileName: String?,
    instructions: List<String>,
    targetMuscles: List<String>,
    reps: Int? = null,
    durationSec: Int? = null,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val resolvedGifUrl = remember(gifFileName) {
        gifFileName?.takeIf { it.isNotBlank() }?.let {
            GifUrlProvider.resolveWithContext(context, it)
        }
    }

    val cleanMuscles = remember(targetMuscles) {
        targetMuscles.filter { !it.equals("main", ignoreCase = true) }
            .distinct()
            .ifEmpty { listOf("Full Body") }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!resolvedGifUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(resolvedGifUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                        )
                    } else {
                        Text(
                            text = "No animation available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(32.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (reps != null || durationSec != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if ((reps ?: 0) > 0) {
                            Text(
                                text = "REPS",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = reps.toString(),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        } else if ((durationSec ?: 0) > 0) {
                            Text(
                                text = "DURATION",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = "${durationSec}s",
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = "INSTRUCTIONS",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (instructions.isEmpty()) {
                    Text(
                        text = "Perform controlled repetitions maintaining proper form and steady breathing.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                    )
                } else {
                    instructions.forEach { line ->
                        val cleanedLine = line.replaceFirst(Regex("^[\\d\\.\\)\\s]+"), "")
                        Text(
                            text = cleanedLine,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                            lineHeight = 24.sp,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 14.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TARGET MUSCLES",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    cleanMuscles.forEach { muscle ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = muscle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(28.dp),
            ) {
                Text(
                    text = "CLOSE",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 18.sp,
                )
            }
        }
    }
}

@Composable
fun ExerciseInstructionSheetForLibrary(
    exercise: Exercise,
    onClose: () -> Unit,
) {
    ExerciseInstructionSheet(
        title = exercise.name,
        gifFileName = exercise.localGifs.firstOrNull(),
        instructions = exercise.instructions,
        targetMuscles = exercise.targetMuscles,
        onClose = onClose,
    )
}

@Composable
fun ExerciseInstructionSheetForWorkout(
    workoutExercise: WorkoutExercise,
    catalogExercise: Exercise?,
    onClose: () -> Unit,
) {
    val instructions = catalogExercise?.instructions
        ?.takeIf { it.isNotEmpty() }
        ?: workoutExercise.description.takeIf { it.isNotBlank() }?.let { listOf(it) }.orEmpty()
    val targetMuscles = catalogExercise?.targetMuscles
        ?.takeIf { it.isNotEmpty() }
        ?: listOf(workoutExercise.category)

    ExerciseInstructionSheet(
        title = workoutExercise.name,
        gifFileName = catalogExercise?.localGifs?.firstOrNull()
            ?: workoutExercise.gifFileName.takeIf { it.isNotBlank() },
        instructions = instructions,
        targetMuscles = targetMuscles,
        reps = workoutExercise.reps,
        durationSec = workoutExercise.durationSec,
        onClose = onClose,
    )
}
