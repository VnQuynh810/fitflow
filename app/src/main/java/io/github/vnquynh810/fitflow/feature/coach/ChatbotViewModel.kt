package io.github.vnquynh810.fitflow.feature.coach

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.vnquynh810.fitflow.core.domain.repository.AiCoachRepository
import io.github.vnquynh810.fitflow.core.model.ChatMessage
import io.github.vnquynh810.fitflow.core.network.MissingApiKeyException
import io.github.vnquynh810.fitflow.core.platform.NetworkMonitor
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class ChatbotUiState(
    val messages: List<ChatMessage> = listOf(WELCOME_MESSAGE),
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val hasApiKey: Boolean = false,
    val customApiKey: String = "",
    val showApiKeyDialog: Boolean = false,
) {
    companion object {
        val WELCOME_MESSAGE = ChatMessage(
            id = "welcome",
            text = "Hey! I'm your AI fitness coach 💪 Ask me anything about your workout, nutrition, or recovery.",
            isFromUser = false,
        )
    }
}

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val aiCoachRepository: AiCoachRepository,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private data class PendingRequest(
        val userText: String,
        val history: List<ChatMessage>,
        val imageUri: Uri?,
    )

    private data class LocalChatState(
        val messages: List<ChatMessage> = listOf(ChatbotUiState.WELCOME_MESSAGE),
        val isLoading: Boolean = false,
        val showApiKeyDialog: Boolean = false,
    )

    private val localState = MutableStateFlow(LocalChatState())
    private var pendingRequest: PendingRequest? = null

    val uiState: StateFlow<ChatbotUiState> = combine(
        localState,
        networkMonitor.isOnlineFlow,
        aiCoachRepository.hasConfiguredApiKeyFlow,
        aiCoachRepository.customApiKeyFlow,
    ) { local, online, hasKey, customKey ->
        ChatbotUiState(
            messages = local.messages,
            isLoading = local.isLoading,
            isOnline = online,
            hasApiKey = hasKey,
            customApiKey = customKey,
            showApiKeyDialog = local.showApiKeyDialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatbotUiState(
            isOnline = networkMonitor.currentSnapshot().isConnected,
        ),
    )

    fun openApiKeyDialog() {
        localState.update { it.copy(showApiKeyDialog = true) }
    }

    fun dismissApiKeyDialog() {
        val hadPending = pendingRequest != null
        pendingRequest = null
        localState.update { state ->
            val updatedMessages = if (hadPending) {
                state.messages + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Please add your **Gemini API Key** (tap the **KEY** badge above or visit Workout Settings) whenever you're ready to chat!",
                    isFromUser = false,
                )
            } else {
                state.messages
            }
            state.copy(showApiKeyDialog = false, messages = updatedMessages)
        }
    }

    fun saveApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        val retryTarget = pendingRequest
        pendingRequest = null
        localState.update { it.copy(showApiKeyDialog = false) }

        viewModelScope.launch {
            aiCoachRepository.setCustomApiKey(trimmed)
            if (trimmed.isNotEmpty() && retryTarget != null) {
                executeCoachRequest(
                    userText = retryTarget.userText,
                    history = retryTarget.history,
                    imageUri = retryTarget.imageUri,
                )
            }
        }
    }

    fun clearApiKey() {
        pendingRequest = null
        localState.update { it.copy(showApiKeyDialog = false) }
        viewModelScope.launch {
            aiCoachRepository.setCustomApiKey("")
        }
    }

    fun sendMessage(inputText: String, imageUri: Uri?) {
        val trimmedText = inputText.trim()
        if ((trimmedText.isEmpty() && imageUri == null) || localState.value.isLoading) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = trimmedText,
            isFromUser = true,
            imageUriString = imageUri?.toString(),
        )

        if (!networkMonitor.currentSnapshot().isConnected) {
            val offlineReply = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Hiện tại bạn đang **Offline**. Không có kết nối mạng, vui lòng kiểm tra lại Wifi/4G để tôi có thể kết nối với server nhé!",
                isFromUser = false,
            )
            localState.update {
                it.copy(messages = it.messages + userMessage + offlineReply)
            }
            return
        }

        val historySnapshot = localState.value.messages
        localState.update {
            it.copy(messages = it.messages + userMessage, isLoading = true)
        }

        viewModelScope.launch {
            executeCoachRequest(
                userText = trimmedText,
                history = historySnapshot,
                imageUri = imageUri,
            )
        }
    }

    private suspend fun executeCoachRequest(
        userText: String,
        history: List<ChatMessage>,
        imageUri: Uri?,
    ) {
        localState.update { it.copy(isLoading = true) }

        val encodedImage = if (imageUri != null) {
            readAndCompressImage(imageUri)
        } else {
            null
        }

        val result = aiCoachRepository.sendMessage(
            userMessage = userText,
            history = history,
            imageBytes = encodedImage?.first,
            imageMimeType = encodedImage?.second,
        )

        result.fold(
            onSuccess = { reply ->
                localState.update { state ->
                    state.copy(
                        isLoading = false,
                        messages = state.messages + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = reply,
                            isFromUser = false,
                        ),
                    )
                }
            },
            onFailure = { error ->
                if (error is MissingApiKeyException) {
                    pendingRequest = PendingRequest(
                        userText = userText,
                        history = history,
                        imageUri = imageUri,
                    )
                    localState.update { state ->
                        state.copy(
                            isLoading = false,
                            showApiKeyDialog = true,
                        )
                    }
                } else {
                    localState.update { state ->
                        state.copy(
                            isLoading = false,
                            messages = state.messages + ChatMessage(
                                id = UUID.randomUUID().toString(),
                                text = "Sorry, something went wrong. Please check your API key or connection and try again.",
                                isFromUser = false,
                            ),
                        )
                    }
                }
            },
        )
    }

    private suspend fun readAndCompressImage(uri: Uri): Pair<ByteArray, String>? =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream) ?: return@use null
                    val maxDim = 1024
                    val scale = minOf(
                        maxDim.toFloat() / bitmap.width.coerceAtLeast(1),
                        maxDim.toFloat() / bitmap.height.coerceAtLeast(1),
                    )
                    val scaledBitmap = if (scale < 1f) {
                        Bitmap.createScaledBitmap(
                            bitmap,
                            (bitmap.width * scale).toInt().coerceAtLeast(1),
                            (bitmap.height * scale).toInt().coerceAtLeast(1),
                            true,
                        )
                    } else {
                        bitmap
                    }
                    val outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    outputStream.toByteArray() to "image/jpeg"
                }
            }.getOrNull()
        }
}
