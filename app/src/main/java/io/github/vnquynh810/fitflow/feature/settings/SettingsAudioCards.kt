package io.github.vnquynh810.fitflow.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward
import io.github.vnquynh810.fitflow.core.designsystem.theme.CardBackground
import io.github.vnquynh810.fitflow.core.designsystem.theme.CardNested
import io.github.vnquynh810.fitflow.core.designsystem.theme.OrangePrimary
import io.github.vnquynh810.fitflow.core.platform.WorkoutTrack

@Composable
internal fun BackgroundMusicCard(
    isBgMusicEnabled: Boolean,
    bgMusicVolume: Float,
    isInWorkoutSession: Boolean,
    currentSong: WorkoutTrack,
    isPlaying: Boolean,
    playbackProgress: Int,
    formatTime: (Int) -> String,
    onBgMusicEnabledChange: (Boolean) -> Unit,
    onBgMusicVolumeChange: (Float) -> Unit,
    onPrevSong: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextSong: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Lucide.Music,
                    contentDescription = null,
                    tint = if (isBgMusicEnabled) OrangePrimary else Color.Gray,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Background Music",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
            Switch(
                checked = isBgMusicEnabled,
                onCheckedChange = onBgMusicEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = OrangePrimary,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray,
                ),
            )
        }

        AnimatedVisibility(
            visible = isBgMusicEnabled && isInWorkoutSession,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardNested)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(OrangePrimary, Color(0xFFCC3300))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Lucide.Music,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column {
                        Text(
                            text = currentSong.name.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Normal,
                        )
                        Text(
                            text = currentSong.artist,
                            color = Color.Gray,
                            fontSize = 12.sp,
                        )
                    }
                }
                Slider(
                    value = playbackProgress.toFloat().coerceAtMost(
                        currentSong.durationSec.toFloat().coerceAtLeast(1f)
                    ),
                    onValueChange = {},
                    valueRange = 0f..currentSong.durationSec.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Transparent,
                        activeTrackColor = OrangePrimary,
                        inactiveTrackColor = Color.DarkGray,
                    ),
                    modifier = Modifier.height(8.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatTime(playbackProgress),
                        color = OrangePrimary,
                        fontSize = 11.sp,
                    )
                    Text(
                        text = formatTime(currentSong.durationSec),
                        color = Color.Gray,
                        fontSize = 11.sp,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onPrevSong) {
                        Icon(
                            imageVector = Lucide.SkipBack,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary)
                            .clickable(onClick = onTogglePlayPause),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Lucide.Pause else Lucide.Play,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    IconButton(onClick = onNextSong) {
                        Icon(
                            imageVector = Lucide.SkipForward,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                VolumeSlider(
                    value = bgMusicVolume,
                    onValueChange = onBgMusicVolumeChange,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AnimatedVisibility(
            visible = isBgMusicEnabled && !isInWorkoutSession,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                VolumeSlider(
                    value = bgMusicVolume,
                    onValueChange = onBgMusicVolumeChange,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
