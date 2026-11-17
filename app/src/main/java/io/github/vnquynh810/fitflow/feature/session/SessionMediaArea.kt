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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
internal fun ExerciseMediaArea(
    gifUrl: String?,
    prepareOverlay: Boolean,
    prepareCountdown: Int,
    isBgMusicEnabled: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleMusic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bgColor = MaterialTheme.colorScheme.background
    val buttonBgColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (!gifUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(gifUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Exercise GIF",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Placeholder",
                        modifier = Modifier.size(64.dp),
                        tint = iconColor,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "GIF OR IMG SPACE",
                        color = iconColor,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            if (prepareOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$prepareCountdown",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 160.sp,
                            lineHeight = 160.sp,
                            color = primaryColor,
                        ),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(buttonBgColor),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(buttonBgColor),
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = textColor)
                }
                IconButton(
                    onClick = onToggleMusic,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(buttonBgColor),
                ) {
                    Icon(
                        imageVector = if (isBgMusicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                        contentDescription = "Toggle Music",
                        tint = textColor,
                    )
                }
            }
        }
    }
}
