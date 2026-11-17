package io.github.vnquynh810.fitflow.core.domain.repository

import io.github.vnquynh810.fitflow.core.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface AiCoachRepository {
    val hasConfiguredApiKeyFlow: Flow<Boolean>
    val customApiKeyFlow: Flow<String>

    suspend fun setCustomApiKey(apiKey: String)
    suspend fun sendMessage(
        userMessage: String,
        history: List<ChatMessage>,
        imageBytes: ByteArray? = null,
        imageMimeType: String? = null,
    ): Result<String>
}
