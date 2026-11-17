package io.github.vnquynh810.fitflow.feature.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.AiCoachRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.model.WorkoutAudioSettings
import io.github.vnquynh810.fitflow.core.platform.WorkoutAudioController
import io.github.vnquynh810.fitflow.core.platform.WorkoutTrack
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class WorkoutSettingsUiState(
    val audioSettings: WorkoutAudioSettings = WorkoutAudioSettings(),
    val tracks: List<WorkoutTrack> = emptyList(),
    val currentSongIndex: Int = 0,
    val isPlaying: Boolean = false,
    val playbackProgress: Int = 0,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = true,
    val hasConfiguredApiKey: Boolean = false,
    val customGeminiApiKey: String = "",
)

@HiltViewModel
class WorkoutSettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val audioController: WorkoutAudioController,
    aiCoachRepository: AiCoachRepository,
) : ViewModel() {

    private data class PlaybackSnapshot(
        val currentSongIndex: Int,
        val isPlaying: Boolean,
        val playbackProgress: Int,
        val isShuffle: Boolean,
        val isRepeat: Boolean,
    )

    private val playbackFlow = combine(
        audioController.currentSongIndex,
        audioController.isPlaying,
        audioController.playbackProgress,
        audioController.isShuffle,
        audioController.isRepeat,
    ) { index, playing, progress, shuffle, repeat ->
        PlaybackSnapshot(
            currentSongIndex = index,
            isPlaying = playing,
            playbackProgress = progress,
            isShuffle = shuffle,
            isRepeat = repeat,
        )
    }

    val uiState: StateFlow<WorkoutSettingsUiState> = combine(
        preferencesRepository.audioSettingsFlow,
        playbackFlow,
        aiCoachRepository.hasConfiguredApiKeyFlow,
        preferencesRepository.customGeminiApiKeyFlow,
    ) { settings, playback, hasKey, customKey ->
        WorkoutSettingsUiState(
            audioSettings = settings,
            tracks = audioController.tracks,
            currentSongIndex = playback.currentSongIndex,
            isPlaying = playback.isPlaying,
            playbackProgress = playback.playbackProgress,
            isShuffle = playback.isShuffle,
            isRepeat = playback.isRepeat,
            hasConfiguredApiKey = hasKey,
            customGeminiApiKey = customKey,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutSettingsUiState(tracks = audioController.tracks),
    )

    fun syncWithCurrentPlayback() {
        audioController.syncPlaybackProgress()
    }

    fun togglePlayPause() {
        audioController.togglePlayPause()
    }

    fun nextSong() {
        audioController.nextSong()
    }

    fun prevSong() {
        audioController.prevSong()
    }

    fun toggleShuffle() {
        audioController.toggleShuffle()
    }

    fun toggleRepeat() {
        audioController.toggleRepeat()
    }

    fun setBgMusicEnabled(enabled: Boolean, isInWorkoutSession: Boolean = false) {
        viewModelScope.launch {
            preferencesRepository.updateAudioSettings { it.copy(isBgMusicEnabled = enabled) }
            if (enabled) {
                if (isInWorkoutSession) {
                    audioController.startMusicIfEnabled()
                }
            } else {
                audioController.stopMusic()
            }
        }
    }

    fun setBgMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        audioController.applyVolume(clamped)
        viewModelScope.launch {
            preferencesRepository.updateAudioSettings { it.copy(bgMusicVolume = clamped) }
        }
    }

    fun setVoiceGuideEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAudioSettings { it.copy(isVoiceGuideEnabled = enabled) }
        }
    }

    fun setCoachName(name: String) {
        viewModelScope.launch {
            preferencesRepository.updateAudioSettings { it.copy(coachName = name) }
        }
    }

    fun setCoachVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        viewModelScope.launch {
            preferencesRepository.updateAudioSettings { it.copy(coachVolume = clamped) }
        }
    }

    fun setSoundEffectEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAudioSettings { it.copy(isSoundEffectEnabled = enabled) }
        }
    }

    fun setAutoCounting(option: String) {
        viewModelScope.launch {
            preferencesRepository.updateAudioSettings { it.copy(autoCounting = option) }
        }
    }

    fun setRestTimer(option: String) {
        viewModelScope.launch {
            preferencesRepository.updateAudioSettings { it.copy(restTimer = option) }
        }
    }

    fun setCountdown(option: String) {
        viewModelScope.launch {
            preferencesRepository.updateAudioSettings { it.copy(countdown = option) }
        }
    }

    fun saveCustomGeminiApiKey(apiKey: String) {
        viewModelScope.launch {
            preferencesRepository.setCustomGeminiApiKey(apiKey.trim())
        }
    }

    fun clearCustomGeminiApiKey() {
        viewModelScope.launch {
            preferencesRepository.setCustomGeminiApiKey("")
        }
    }
}
