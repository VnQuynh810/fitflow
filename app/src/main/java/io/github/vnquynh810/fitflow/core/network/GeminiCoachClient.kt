package io.github.vnquynh810.fitflow.core.network

import android.util.Base64
import io.github.vnquynh810.fitflow.BuildConfig
import io.github.vnquynh810.fitflow.core.model.ChatMessage
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class MissingApiKeyException : IllegalStateException(
    "Gemini API key is not configured. Add your key in AI Coach Settings or local.properties."
)

@Singleton
class GeminiCoachClient @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val MODEL_ID = "gemini-2.5-flash"
        private const val SYSTEM_PROMPT = """
You are FitFlow's AI fitness coach (FitBro AI).
You help users with workout advice, form cues, nutrition tips, recovery strategies, and motivation.
You can also estimate the calories and macronutrients of meals from food photos.
Keep responses concise, structured, and practical.
Always reply in the same language the user writes in.
"""
    }

    fun resolveApiKey(customKey: String): String =
        customKey.trim().ifBlank { BuildConfig.GEMINI_API_KEY.trim() }

    suspend fun generateReply(
        apiKey: String,
        userMessage: String,
        history: List<ChatMessage>,
        imageBytes: ByteArray? = null,
        imageMimeType: String? = null,
    ): Result<String> = withContext(Dispatchers.IO) {
        val activeKey = resolveApiKey(apiKey)
        if (activeKey.isBlank()) {
            return@withContext Result.failure(MissingApiKeyException())
        }

        runCatching {
            val contents = buildList {
                history.forEach { msg ->
                    if (msg.text.isNotBlank()) {
                        add(
                            GeminiContent(
                                role = if (msg.isFromUser) "user" else "model",
                                parts = listOf(GeminiPart(text = msg.text)),
                            )
                        )
                    }
                }

                val userParts = buildList {
                    val promptText = if (userMessage.isBlank() && imageBytes != null) {
                        "Analyze the estimated calories and macronutrients of the food in this image."
                    } else {
                        userMessage
                    }
                    add(GeminiPart(text = promptText))

                    if (imageBytes != null && !imageMimeType.isNullOrBlank()) {
                        val encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                        add(
                            GeminiPart(
                                inlineData = GeminiInlineData(
                                    mimeType = imageMimeType,
                                    data = encoded,
                                )
                            )
                        )
                    }
                }
                add(GeminiContent(role = "user", parts = userParts))
            }

            val payload = GeminiRequest(
                systemInstruction = GeminiSystemInstruction(
                    parts = listOf(GeminiPart(text = SYSTEM_PROMPT.trimIndent()))
                ),
                contents = contents,
            )

            val requestBody = json.encodeToString(payload)
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$MODEL_ID:generateContent")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-goog-api-key", activeKey)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException("Gemini API error (${response.code}): $rawBody")
                }
                val parsed = json.decodeFromString<GeminiResponse>(rawBody)
                parsed.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull { !it.text.isNullOrBlank() }
                    ?.text
                    ?: throw IllegalStateException("Empty response from AI coach.")
            }
        }
    }

    @Serializable
    private data class GeminiRequest(
        val systemInstruction: GeminiSystemInstruction,
        val contents: List<GeminiContent>,
    )

    @Serializable
    private data class GeminiSystemInstruction(
        val parts: List<GeminiPart>,
    )

    @Serializable
    private data class GeminiContent(
        val role: String? = null,
        val parts: List<GeminiPart> = emptyList(),
    )

    @Serializable
    private data class GeminiPart(
        val text: String? = null,
        @SerialName("inline_data") val inlineData: GeminiInlineData? = null,
    )

    @Serializable
    private data class GeminiInlineData(
        @SerialName("mime_type") val mimeType: String,
        val data: String,
    )

    @Serializable
    private data class GeminiResponse(
        val candidates: List<GeminiCandidate>? = null,
    )

    @Serializable
    private data class GeminiCandidate(
        val content: GeminiContent? = null,
    )
}
