package io.github.vnquynh810.fitflow.core.platform

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutTrack(
    val name: String,
    val artist: String,
    val durationSec: Int,
    val assetPath: String,
)

@Singleton
class WorkoutAudioController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val tracks: List<WorkoutTrack> = listOf(
        WorkoutTrack("Hyper Pulse (128 BPM)", "FitFlow Synth Lab · CC0", 15, "music/hyper_pulse.wav"),
        WorkoutTrack("Cyber Sprint (138 BPM)", "FitFlow Synth Lab · CC0", 14, "music/cyber_sprint.wav"),
        WorkoutTrack("Iron Focus (118 BPM)", "FitFlow Synth Lab · CC0", 16, "music/iron_focus.wav"),
    )

    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0)
    val playbackProgress: StateFlow<Int> = _playbackProgress.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(true)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    fun startMusicIfEnabled() {
        scope.launch {
            val settings = preferencesRepository.audioSettingsFlow.first()
            if (settings.isBgMusicEnabled && mediaPlayer == null) {
                loadAndPlay(_currentSongIndex.value, settings.bgMusicVolume)
            }
        }
    }

    fun stopMusic() {
        progressJob?.cancel()
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _playbackProgress.value = 0
    }

    fun applyVolume(volume: Float) {
        val safe = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(safe, safe)
    }

    fun togglePlayPause() {
        scope.launch {
            val settings = preferencesRepository.audioSettingsFlow.first()
            if (!settings.isBgMusicEnabled) return@launch
            val mp = mediaPlayer
            if (mp == null) {
                loadAndPlay(_currentSongIndex.value, settings.bgMusicVolume)
                return@launch
            }
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
                progressJob?.cancel()
            } else {
                mp.start()
                _isPlaying.value = true
                startProgressTracking()
            }
        }
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        progressJob?.cancel()
    }

    fun resumeMusic() {
        scope.launch {
            val settings = preferencesRepository.audioSettingsFlow.first()
            if (settings.isBgMusicEnabled && mediaPlayer != null) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTracking()
            }
        }
    }

    fun nextSong() {
        scope.launch {
            val volume = preferencesRepository.audioSettingsFlow.first().bgMusicVolume
            val nextIdx = if (_isShuffle.value) {
                tracks.indices.filter { it != _currentSongIndex.value }.randomOrNull() ?: 0
            } else {
                (_currentSongIndex.value + 1) % tracks.size
            }
            loadAndPlay(nextIdx, volume)
        }
    }

    fun prevSong() {
        scope.launch {
            val currentSec = mediaPlayer?.currentPosition?.div(1000) ?: 0
            if (currentSec > 3) {
                mediaPlayer?.seekTo(0)
                _playbackProgress.value = 0
            } else {
                val volume = preferencesRepository.audioSettingsFlow.first().bgMusicVolume
                val prevIdx = (_currentSongIndex.value - 1 + tracks.size) % tracks.size
                loadAndPlay(prevIdx, volume)
            }
        }
    }

    fun toggleShuffle() {
        _isShuffle.update { !it }
    }

    fun toggleRepeat() {
        _isRepeat.update { !it }
        mediaPlayer?.isLooping = _isRepeat.value
    }

    fun syncPlaybackProgress() {
        if (mediaPlayer != null && _isPlaying.value) {
            startProgressTracking()
        }
        _playbackProgress.value = (mediaPlayer?.currentPosition ?: 0) / 1000
    }

    private fun loadAndPlay(index: Int, volume: Float) {
        progressJob?.cancel()
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null

        runCatching {
            val track = tracks[index.coerceIn(tracks.indices)]
            context.assets.openFd(track.assetPath).use { afd ->
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    val safeVol = volume.coerceIn(0f, 1f)
                    setVolume(safeVol, safeVol)
                    isLooping = _isRepeat.value
                    setOnCompletionListener {
                        if (!_isRepeat.value) nextSong()
                    }
                    prepare()
                    start()
                }
            }
            _currentSongIndex.value = index
            _isPlaying.value = true
            _playbackProgress.value = 0
            startProgressTracking()
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                delay(500L)
                _playbackProgress.value = (mediaPlayer?.currentPosition ?: 0) / 1000
            }
        }
    }
}
