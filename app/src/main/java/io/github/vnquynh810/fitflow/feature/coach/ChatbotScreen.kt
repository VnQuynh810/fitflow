package io.github.vnquynh810.fitflow.feature.coach

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Bot
import com.composables.icons.lucide.Lucide
import io.github.vnquynh810.fitflow.core.designsystem.theme.FitflowTheme
import java.io.File

@Composable
fun ChatbotRoute(
    onBack: () -> Unit,
) {
    val viewModel: ChatbotViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatbotScreen(
        uiState = uiState,
        onBack = onBack,
        onSendMessage = viewModel::sendMessage,
        onOpenApiKeyDialog = viewModel::openApiKeyDialog,
        onSaveApiKey = viewModel::saveApiKey,
        onClearApiKey = viewModel::clearApiKey,
        onDismissApiKeyDialog = viewModel::dismissApiKeyDialog,
    )
}

@Composable
fun ChatbotScreen(
    uiState: ChatbotUiState,
    onBack: () -> Unit = {},
    onSendMessage: (String, Uri?) -> Unit,
    onOpenApiKeyDialog: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onClearApiKey: () -> Unit,
    onDismissApiKeyDialog: () -> Unit,
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            selectedImageUri = cameraImageUri
        }
    }

    fun createCameraUri(): Uri {
        val imageFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile,
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            val uri = createCameraUri()
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            val uri = createCameraUri()
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        val totalItems = uiState.messages.size + if (uiState.isLoading) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
        ChatbotTopBar(
            isOnline = uiState.isOnline,
            hasApiKey = uiState.hasApiKey,
            onBack = onBack,
            onOpenApiKeyDialog = onOpenApiKeyDialog,
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
            thickness = 1.dp,
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = uiState.messages,
                key = { it.id },
            ) { message ->
                ChatBubble(message = message)
            }
            if (uiState.isLoading) {
                item(key = "typing_indicator") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Lucide.Bot,
                                contentDescription = "Bot",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(
                                        topStart = 10.dp,
                                        topEnd = 10.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 10.dp,
                                    ),
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f),
                                    RoundedCornerShape(
                                        topStart = 10.dp,
                                        topEnd = 10.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 10.dp,
                                    ),
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            TypingIndicator()
                        }
                    }
                }
            }
        }

        QuickSuggestionsRow(
            onSuggestionClick = { inputText = it },
        )

        ChatbotComposerBar(
            inputText = inputText,
            selectedImageUri = selectedImageUri,
            isLoading = uiState.isLoading,
            onInputTextChange = { inputText = it },
            onClearImage = { selectedImageUri = null },
            onLaunchCamera = ::launchCamera,
            onLaunchGallery = { galleryLauncher.launch("image/*") },
            onSend = {
                val textToSend = inputText
                val uriToSend = selectedImageUri
                inputText = ""
                selectedImageUri = null
                onSendMessage(textToSend, uriToSend)
            },
        )
    }

    if (uiState.showApiKeyDialog) {
        CoachApiKeyDialog(
            currentCustomKey = uiState.customApiKey,
            hasConfiguredKey = uiState.hasApiKey,
            onSave = onSaveApiKey,
            onClear = onClearApiKey,
            onDismiss = onDismissApiKeyDialog,
        )
    }
}

@Composable
private fun QuickSuggestionsRow(
    onSuggestionClick: (String) -> Unit,
) {
    val suggestions = remember {
        listOf(
            "How to get shredded abs?",
            "Post-workout meal ideas?",
            "How many calories in a pizza?",
            "How to reduce muscle soreness?",
            "Fat-loss breakfast recipes",
            "Water intake during workout?",
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        suggestions.forEach { suggestion ->
            AssistChip(
                onClick = { onSuggestionClick(suggestion) },
                label = {
                    Text(
                        text = suggestion,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                ),
                border = AssistChipDefaults.assistChipBorder(
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    enabled = true,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatbotScreenPreview() {
    FitflowTheme {
        ChatbotScreen(
            uiState = ChatbotUiState(),
            onSendMessage = { _, _ -> },
            onOpenApiKeyDialog = {},
            onSaveApiKey = {},
            onClearApiKey = {},
            onDismissApiKeyDialog = {},
        )
    }
}
