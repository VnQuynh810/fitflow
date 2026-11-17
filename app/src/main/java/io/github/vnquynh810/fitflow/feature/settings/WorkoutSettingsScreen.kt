package io.github.vnquynh810.fitflow.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Bot
import com.composables.icons.lucide.Lucide
import io.github.vnquynh810.fitflow.core.designsystem.theme.CardBackground
import io.github.vnquynh810.fitflow.core.designsystem.theme.FitflowTheme
import io.github.vnquynh810.fitflow.core.designsystem.theme.OrangePrimary
import io.github.vnquynh810.fitflow.core.designsystem.theme.SuccessGreen
import io.github.vnquynh810.fitflow.core.platform.WorkoutTrack
import java.util.Locale

@Composable
fun WorkoutSettingsRoute(
    isInWorkoutSession: Boolean,
    onBack: () -> Unit,
) {
    val viewModel: WorkoutSettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.syncWithCurrentPlayback()
    }

    WorkoutSettingsScreen(
        uiState = uiState,
        isInWorkoutSession = isInWorkoutSession,
        onBack = onBack,
        onBgMusicEnabledChange = { viewModel.setBgMusicEnabled(it, isInWorkoutSession) },
        onBgMusicVolumeChange = viewModel::setBgMusicVolume,
        onTogglePlayPause = viewModel::togglePlayPause,
        onPrevSong = viewModel::prevSong,
        onNextSong = viewModel::nextSong,
        onVoiceGuideEnabledChange = viewModel::setVoiceGuideEnabled,
        onCoachVolumeChange = viewModel::setCoachVolume,
        onSoundEffectEnabledChange = viewModel::setSoundEffectEnabled,
        onRestTimerSelect = viewModel::setRestTimer,
        onSaveApiKey = viewModel::saveCustomGeminiApiKey,
        onClearApiKey = viewModel::clearCustomGeminiApiKey,
    )
}

@Composable
fun WorkoutSettingsScreen(
    uiState: WorkoutSettingsUiState,
    isInWorkoutSession: Boolean = false,
    onBack: () -> Unit,
    onBgMusicEnabledChange: (Boolean) -> Unit,
    onBgMusicVolumeChange: (Float) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrevSong: () -> Unit,
    onNextSong: () -> Unit,
    onVoiceGuideEnabledChange: (Boolean) -> Unit,
    onCoachVolumeChange: (Float) -> Unit,
    onSoundEffectEnabledChange: (Boolean) -> Unit,
    onRestTimerSelect: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onClearApiKey: () -> Unit,
) {
    val audioSettings = uiState.audioSettings
    var showRestTimerDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val currentSong = uiState.tracks.getOrNull(uiState.currentSongIndex)
        ?: WorkoutTrack("Unknown", "Unknown", 0, "")

    val formatTime: (Int) -> String = { sec ->
        val m = sec / 60
        val s = sec % 60
        String.format(Locale.US, "%02d:%02d", m, s)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WORKOUT SETTINGS",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    BackgroundMusicCard(
                        isBgMusicEnabled = audioSettings.isBgMusicEnabled,
                        bgMusicVolume = audioSettings.bgMusicVolume,
                        isInWorkoutSession = isInWorkoutSession,
                        currentSong = currentSong,
                        isPlaying = uiState.isPlaying,
                        playbackProgress = uiState.playbackProgress,
                        formatTime = formatTime,
                        onBgMusicEnabledChange = onBgMusicEnabledChange,
                        onBgMusicVolumeChange = onBgMusicVolumeChange,
                        onPrevSong = onPrevSong,
                        onTogglePlayPause = onTogglePlayPause,
                        onNextSong = onNextSong,
                    )
                }

                item {
                    VoiceAndSoundCard(
                        isVoiceGuideEnabled = audioSettings.isVoiceGuideEnabled,
                        coachVolume = audioSettings.coachVolume,
                        isSoundEffectEnabled = audioSettings.isSoundEffectEnabled,
                        onVoiceGuideEnabledChange = onVoiceGuideEnabledChange,
                        onCoachVolumeChange = onCoachVolumeChange,
                        onSoundEffectEnabledChange = onSoundEffectEnabledChange,
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBackground)
                            .padding(vertical = 8.dp),
                    ) {
                        GeneralSettingRow(
                            title = "Rest Timer",
                            subtitle = null,
                            valueText = audioSettings.restTimer,
                            onClick = { showRestTimerDialog = true },
                        )
                    }
                }

                item {
                    AiCoachApiKeyCard(
                        hasConfiguredKey = uiState.hasConfiguredApiKey,
                        hasCustomKey = uiState.customGeminiApiKey.isNotBlank(),
                        onClick = { showApiKeyDialog = true },
                    )
                }
            }
        }

        if (showRestTimerDialog) {
            RestTimerSelectionDialog(
                selectedValue = audioSettings.restTimer,
                onSelect = {
                    onRestTimerSelect(it)
                    showRestTimerDialog = false
                },
                onDismiss = { showRestTimerDialog = false },
            )
        }

        if (showApiKeyDialog) {
            GeminiApiKeyDialog(
                currentCustomKey = uiState.customGeminiApiKey,
                hasConfiguredKey = uiState.hasConfiguredApiKey,
                onSave = { key ->
                    onSaveApiKey(key)
                    showApiKeyDialog = false
                },
                onClear = {
                    onClearApiKey()
                    showApiKeyDialog = false
                },
                onDismiss = { showApiKeyDialog = false },
            )
        }
    }
}

@Composable
private fun AiCoachApiKeyCard(
    hasConfiguredKey: Boolean,
    hasCustomKey: Boolean,
    onClick: () -> Unit,
) {
    val badgeColor = if (hasConfiguredKey) SuccessGreen else Color.Gray
    val statusLabel = if (hasConfiguredKey) "Configured" else "Not Set"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Lucide.Bot,
                    contentDescription = "AI Coach",
                    tint = OrangePrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = "AI Coach (Gemini API Key)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                Text(
                    text = if (hasCustomKey) {
                        "Personal BYOK key active · Stored locally"
                    } else {
                        "Bring Your Own Key (BYOK) for Fitbro AI"
                    },
                    color = Color.Gray,
                    fontSize = 12.sp,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(badgeColor.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = statusLabel,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutSettingsScreenPreview() {
    FitflowTheme {
        WorkoutSettingsScreen(
            uiState = WorkoutSettingsUiState(),
            onBack = {},
            onBgMusicEnabledChange = {},
            onBgMusicVolumeChange = {},
            onTogglePlayPause = {},
            onPrevSong = {},
            onNextSong = {},
            onVoiceGuideEnabledChange = {},
            onCoachVolumeChange = {},
            onSoundEffectEnabledChange = {},
            onRestTimerSelect = {},
            onSaveApiKey = {},
            onClearApiKey = {},
        )
    }
}
