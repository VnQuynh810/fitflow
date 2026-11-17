package io.github.vnquynh810.fitflow.core.platform

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

class TtsCoachController(context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isReady = true
                pendingText?.let { text ->
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fitflow_tts")
                }
                pendingText = null
            }
        }
    }

    fun getAvailableVoices(): List<Voice> = tts?.voices?.toList().orEmpty()

    fun setVoice(voice: Voice) {
        tts?.voice = voice
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        if (!isReady) {
            pendingText = text
            return
        }
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fitflow_tts")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
