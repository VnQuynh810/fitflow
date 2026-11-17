package io.github.vnquynh810.fitflow.feature.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.vnquynh810.fitflow.core.designsystem.theme.CardBackground
import io.github.vnquynh810.fitflow.core.designsystem.theme.OrangePrimary
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun EditExerciseRowItem(
    index: Int,
    exercise: WorkoutExercise,
    gifUrl: String?,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    dragOffsetPx: Float,
    onAdjustQty: (Int) -> Unit,
    onReplace: () -> Unit,
) {
    val context = LocalContext.current
    val accentColor = OrangePrimary
    val qtyLabel = if (exercise.durationSec > 0) {
        val m = exercise.durationSec / 60
        val s = exercise.durationSec % 60
        String.format(Locale.US, "%02d:%02d", m, s)
    } else {
        "x ${exercise.reps}"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(x = 0, y = dragOffsetPx.roundToInt()) }
            .zIndex(if (isDragging) 1f else 0f)
            .border(
                width = if (isDragging) 1.dp else 0.dp,
                color = if (isDragging) OrangePrimary else Color.Transparent,
                shape = RoundedCornerShape(16.dp),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDragging) OrangePrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                    .pointerInput(index) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragEnd,
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drag to reorder",
                    tint = OrangePrimary,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (!gifUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(gifUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = exercise.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                    )
                } else {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.category.uppercase(),
                    color = accentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exercise.name.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 18.sp,
                    maxLines = 2,
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuantityStepButton(icon = Icons.Default.Remove, onClick = { onAdjustQty(-1) })
                    Text(
                        text = qtyLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = accentColor,
                        fontSize = 15.sp,
                        modifier = Modifier.widthIn(min = 40.dp),
                    )
                    QuantityStepButton(icon = Icons.Default.Add, onClick = { onAdjustQty(1) })
                }
            }

            IconButton(
                onClick = onReplace,
                modifier = Modifier
                    .size(36.dp)
                    .background(OrangePrimary.copy(alpha = 0.15f), CircleShape),
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Replace",
                    tint = OrangePrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun QuantityStepButton(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}
