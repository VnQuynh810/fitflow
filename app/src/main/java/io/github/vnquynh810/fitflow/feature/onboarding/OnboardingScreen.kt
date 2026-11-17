package io.github.vnquynh810.fitflow.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vnquynh810.fitflow.R
import io.github.vnquynh810.fitflow.core.designsystem.theme.FitflowTheme
import kotlinx.coroutines.launch

@Composable
fun OnboardingRoute(
    isRecalibrate: Boolean,
    onFinishedOnboarding: () -> Unit,
    onFinishedRecalibrate: () -> Unit,
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(isRecalibrate) {
        viewModel.initialize(isRecalibrate)
    }

    OnboardingScreen(
        uiState = uiState,
        isRecalibrate = isRecalibrate,
        onWeightChange = viewModel::updateWeight,
        onHeightChange = viewModel::updateHeight,
        onTargetWeightChange = viewModel::updateTargetWeight,
        onBirthYearChange = viewModel::updateBirthYear,
        onWorkoutTimeChange = viewModel::updateWorkoutTime,
        onComplete = {
            viewModel.completeOnboarding {
                if (isRecalibrate) {
                    onFinishedRecalibrate()
                } else {
                    onFinishedOnboarding()
                }
            }
        },
    )
}

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    isRecalibrate: Boolean = false,
    onWeightChange: (Float) -> Unit,
    onHeightChange: (Float) -> Unit,
    onTargetWeightChange: (Float) -> Unit,
    onBirthYearChange: (Int) -> Unit,
    onWorkoutTimeChange: (String) -> Unit,
    onComplete: () -> Unit,
) {
    val totalSteps = if (isRecalibrate) 5 else 6
    val pagerState = rememberPagerState(pageCount = { totalSteps })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 32.dp, bottom = 24.dp),
    ) {
        OnboardingHeader(
            currentStep = pagerState.currentPage,
            totalSteps = totalSteps,
        )

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
        ) { page ->
            when (page) {
                0 -> WeightStep(
                    weight = uiState.weight,
                    onWeightChange = onWeightChange,
                )
                1 -> HeightStep(
                    height = uiState.height,
                    onHeightChange = onHeightChange,
                )
                2 -> BmiInfoStep(
                    weight = uiState.weight,
                    height = uiState.height,
                )
                3 -> TargetWeightStep(
                    targetWeight = uiState.targetWeight,
                    currentWeight = uiState.weight,
                    onTargetChange = onTargetWeightChange,
                )
                4 -> BirthYearStep(
                    birthYear = uiState.birthYear,
                    onYearChange = onBirthYearChange,
                )
                5 -> WorkoutTimeStep(
                    workoutTime = uiState.workoutTime,
                    keySeed = uiState.isPreloaded,
                    onTimeSelected = onWorkoutTimeChange,
                )
            }
        }

        PaddingBox {
            Button(
                onClick = {
                    if (pagerState.currentPage < totalSteps - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onComplete()
                    }
                },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(
                    text = if (pagerState.currentPage == totalSteps - 1) {
                        stringResource(R.string.onboarding_action_activate_journey)
                    } else {
                        stringResource(R.string.onboarding_action_continue)
                    },
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun OnboardingHeader(currentStep: Int, totalSteps: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.onboarding_brand_fit),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                fontStyle = FontStyle.Normal,
            )
            Text(
                text = stringResource(R.string.onboarding_brand_flow),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                fontStyle = FontStyle.Normal,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index <= currentStep) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(0.1f)
                            }
                        ),
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun StepLayout(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.padding(horizontal = 24.dp)) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(40.dp))
        content()
    }
}

@Composable
fun PaddingBox(content: @Composable () -> Unit) {
    Box(Modifier.padding(horizontal = 24.dp)) { content() }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingHeaderPreview() {
    FitflowTheme {
        OnboardingHeader(currentStep = 1, totalSteps = 6)
    }
}
