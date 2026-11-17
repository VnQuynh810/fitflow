package io.github.vnquynh810.fitflow.feature.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise
import java.util.Locale

@Composable
internal fun RestPhaseScreen(
    restSeconds: Int,
    nextExercise: WorkoutExercise?,
    nextGifUrl: String?,
    nextIndexOneBased: Int,
    totalExercises: Int,
    onAddTime: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVar = MaterialTheme.colorScheme.surfaceVariant
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    val timeStr = String.format(Locale.US, "%02d:%02d", restSeconds / 60, restSeconds % 60)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(surfaceVar),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (nextExercise != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "UP NEXT $nextIndexOneBased/$totalExercises",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = primaryColor,
                                letterSpacing = 0.08.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nextExercise.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                    if (nextExercise.reps > 0) {
                        Text(
                            text = "x ${nextExercise.reps}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    } else if (nextExercise.durationSec > 0) {
                        Text(
                            text = "${nextExercise.durationSec}s",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!nextGifUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(nextGifUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Next Exercise GIF",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(surface),
                        ) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = iconColor,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No image available",
                                color = iconColor,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(surface),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = primaryColor,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Almost done!",
                    color = textColor,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "REST",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = primaryColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timeStr,
                style = MaterialTheme.typography.displayLarge.copy(
                    color = textColor,
                    fontSize = 80.sp,
                    lineHeight = 80.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = onAddTime,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = surfaceVar,
                    contentColor = textColor,
                ),
                shape = RoundedCornerShape(32.dp),
            ) {
                Text("+20s", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Button(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(32.dp),
            ) {
                Text("Skip", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
