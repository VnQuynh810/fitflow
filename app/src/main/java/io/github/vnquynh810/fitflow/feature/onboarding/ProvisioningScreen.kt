package io.github.vnquynh810.fitflow.feature.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vnquynh810.fitflow.core.designsystem.theme.FitflowTheme

@Composable
fun ProvisioningRoute(
    goalName: String,
    onPlanReady: () -> Unit,
) {
    val viewModel: ProvisioningViewModel = hiltViewModel()
    val provisioningState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(goalName) {
        viewModel.startProvisioning(goalName)
    }

    ProvisioningScreen(
        provisioningState = provisioningState,
        onPlanReady = onPlanReady,
        onConfirmMobileData = viewModel::confirmMobileData,
        onRetry = viewModel::retry,
    )
}

@Composable
fun ProvisioningScreen(
    provisioningState: PlanProvisioningState,
    onPlanReady: () -> Unit,
    onConfirmMobileData: () -> Unit,
    onRetry: () -> Unit,
) {
    LaunchedEffect(provisioningState.isCompleted) {
        if (provisioningState.isCompleted) {
            onPlanReady()
        }
    }

    val totalProgressRaw =
        (provisioningState.firstSegmentProgress + provisioningState.secondSegmentProgress) / 2f
    val animatedTotalProgress by animateFloatAsState(
        targetValue = totalProgressRaw,
        animationSpec = tween(durationMillis = 300),
        label = "total_progress",
    )
    val percentage = (animatedTotalProgress * 100).toInt()

    val statusText = when {
        provisioningState.requiresMobileDataConsent ->
            "Using mobile data may consume data to prepare workouts."
        provisioningState.isNoNetwork ->
            "Network is required for first-time workout media setup."
        provisioningState.hasError ->
            provisioningState.statusMessage.ifBlank { "Unable to complete setup." }
        else -> provisioningState.statusMessage
    }

    val actionMode = when {
        provisioningState.requiresMobileDataConsent -> "accept"
        provisioningState.isNoNetwork || provisioningState.hasError -> "retry"
        else -> "none"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "PICKING THE BEST",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
        )
        Row {
            Text(
                text = "EXERCISES ",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Normal,
            )
            Text(
                text = "FOR YOU",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Normal,
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "PREPARING",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
            Text(
                text = "$percentage%",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ProgressTrack(
            progress = animatedTotalProgress,
            modifier = Modifier.fillMaxWidth(),
            height = 12.dp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = statusText,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )

        if (actionMode != "none") {
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (actionMode == "accept") {
            Button(
                onClick = onConfirmMobileData,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "CONTINUE WITH MOBILE DATA",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                )
            }
        }

        if (actionMode == "retry") {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "RETRY",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

@Composable
private fun ProgressTrack(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProvisioningScreenPreview() {
    FitflowTheme {
        ProvisioningScreen(
            provisioningState = PlanProvisioningState(
                firstSegmentProgress = 1f,
                secondSegmentProgress = 0.4f,
                statusMessage = "Finalizing setup...",
                isInProgress = true,
            ),
            onPlanReady = {},
            onConfirmMobileData = {},
            onRetry = {},
        )
    }
}
