package io.github.vnquynh810.fitflow.core.data

import io.github.vnquynh810.fitflow.core.domain.repository.AiCoachRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.model.ChatMessage
import io.github.vnquynh810.fitflow.core.network.GeminiCoachClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class AiCoachRepositoryImpl @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val geminiCoachClient: GeminiCoachClient,
) : AiCoachRepository {

    override val customApiKeyFlow: Flow<String> = preferencesRepository.customGeminiApiKeyFlow

    override val hasConfiguredApiKeyFlow: Flow<Boolean> = customApiKeyFlow.map { customKey ->
        geminiCoachClient.resolveApiKey(customKey).isNotBlank()
    }

    override suspend fun setCustomApiKey(apiKey: String) {
        preferencesRepository.setCustomGeminiApiKey(apiKey)
    }

    override suspend fun sendMessage(
        userMessage: String,
        history: List<ChatMessage>,
        imageBytes: ByteArray?,
        imageMimeType: String?,
    ): Result<String> {
        val customKey = customApiKeyFlow.first()
        return geminiCoachClient.generateReply(
            apiKey = customKey,
            userMessage = userMessage,
            history = history,
            imageBytes = imageBytes,
            imageMimeType = imageMimeType,
        )
    }
}
