package io.github.vnquynh810.fitflow.feature.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vnquynh810.fitflow.core.designsystem.component.ExerciseInstructionSheetForWorkout
import io.github.vnquynh810.fitflow.core.platform.TtsCoachController

@Composable
fun WorkoutSessionRoute(
    dayNumber: Int,
    startIndex: Int,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onSessionCompleted: (dayNumber: Int, activeSeconds: Int, isNewStreak: Boolean) -> Unit,
) {
    val viewModel: WorkoutSessionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(dayNumber, startIndex) {
        viewModel.loadDaySession(dayNumber = dayNumber, startIndex = startIndex)
    }

    LaunchedEffect(viewModel) {
        viewModel.completedEvents.collect { event ->
            if (event is SessionCompletedEvent.PlanDayCompleted) {
                onSessionCompleted(event.dayNumber, event.activeSeconds, event.isNewStreak)
            }
        }
    }

    WorkoutSessionScreen(
        uiState = uiState,
        onRequestExit = viewModel::requestExitDialog,
        onDismissExit = viewModel::dismissExitDialog,
        onConfirmQuit = { viewModel.confirmQuitSession(onExited = onBack) },
        onOpenSettings = onOpenSettings,
        onToggleMusic = viewModel::toggleBgMusic,
        onOpenInstructions = viewModel::openInstructionSheet,
        onCloseInstructions = viewModel::closeInstructionSheet,
        onSkipPreparing = viewModel::skipPreparing,
        onPreviousExercise = viewModel::goToPreviousExercise,
        onPrimaryAction = viewModel::onPrimaryExerciseAction,
        onNextExercise = viewModel::skipCurrentExercise,
        onAddRestTime = { viewModel.addRestTime(20) },
        onSkipRest = viewModel::skipRest,
        onResumeFromPause = viewModel::resumeFromPauseDialog,
        onRestartExerciseFromPause = viewModel::restartCurrentExerciseFromPause,
    )
}

@Composable
fun SupplementarySessionRoute(
    workoutId: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onSessionCompleted: (workoutId: String, activeSeconds: Int) -> Unit,
) {
    val viewModel: WorkoutSessionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(workoutId) {
        viewModel.loadSupplementarySession(workoutId = workoutId)
    }

    LaunchedEffect(viewModel) {
        viewModel.completedEvents.collect { event ->
            if (event is SessionCompletedEvent.SupplementaryCompleted) {
                onSessionCompleted(event.workoutId, event.activeSeconds)
            }
        }
    }

    WorkoutSessionScreen(
        uiState = uiState,
        onRequestExit = viewModel::requestExitDialog,
        onDismissExit = viewModel::dismissExitDialog,
        onConfirmQuit = { viewModel.confirmQuitSession(onExited = onBack) },
        onOpenSettings = onOpenSettings,
        onToggleMusic = viewModel::toggleBgMusic,
        onOpenInstructions = viewModel::openInstructionSheet,
        onCloseInstructions = viewModel::closeInstructionSheet,
        onSkipPreparing = viewModel::skipPreparing,
        onPreviousExercise = viewModel::goToPreviousExercise,
        onPrimaryAction = viewModel::onPrimaryExerciseAction,
        onNextExercise = viewModel::skipCurrentExercise,
        onAddRestTime = { viewModel.addRestTime(20) },
        onSkipRest = viewModel::skipRest,
        onResumeFromPause = viewModel::resumeFromPauseDialog,
        onRestartExerciseFromPause = viewModel::restartCurrentExerciseFromPause,
    )
}

@Composable
internal fun WorkoutSessionScreen(
    uiState: WorkoutSessionUiState,
    onRequestExit: () -> Unit,
    onDismissExit: () -> Unit,
    onConfirmQuit: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleMusic: () -> Unit,
    onOpenInstructions: () -> Unit,
    onCloseInstructions: () -> Unit,
    onSkipPreparing: () -> Unit,
    onPreviousExercise: () -> Unit,
    onPrimaryAction: () -> Unit,
    onNextExercise: () -> Unit,
    onAddRestTime: () -> Unit,
    onSkipRest: () -> Unit,
    onResumeFromPause: () -> Unit,
    onRestartExerciseFromPause: () -> Unit,
) {
    val context = LocalContext.current
    val ttsController = remember(context) { TtsCoachController(context) }

    DisposableEffect(ttsController) {
        onDispose {
            ttsController.shutdown()
        }
    }

    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }

    BackHandler(onBack = onRequestExit)

    if (uiState.isLoading || uiState.exercises.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LaunchedEffect(uiState.phase, uiState.countdownRemaining, uiState.isVoiceGuideEnabled) {
        if (!uiState.isVoiceGuideEnabled || uiState.phase != SessionPhase.PREPARING) return@LaunchedEffect
        when (uiState.countdownRemaining) {
            5 -> ttsController.speak("Ready to go!")
            3, 2, 1 -> ttsController.speak("${uiState.countdownRemaining}")
        }
    }

    LaunchedEffect(uiState.phase, uiState.currentIndex, uiState.isVoiceGuideEnabled) {
        if (!uiState.isVoiceGuideEnabled) return@LaunchedEffect
        when (uiState.phase) {
            SessionPhase.PREPARING -> Unit
            SessionPhase.EXERCISING -> {
                val exercise = uiState.currentExercise ?: return@LaunchedEffect
                val repsOrDuration = when {
                    exercise.reps > 0 -> "${exercise.reps}. Reps"
                    exercise.durationSec > 0 -> "${exercise.durationSec}. Seconds"
                    else -> ""
                }
                ttsController.speak("Go! Do your exercise! $repsOrDuration. ${exercise.name}.")
            }
            SessionPhase.RESTING -> {
                val nextExercise = uiState.nextExercise
                if (nextExercise != null && uiState.exercises.getOrNull(uiState.currentIndex + 2) != null) {
                    ttsController.speak("Take your rest. Up next. ${nextExercise.name}")
                } else {
                    ttsController.speak("Rest. Last exercise coming up!")
                }
            }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val buttonBgColor = MaterialTheme.colorScheme.surfaceVariant
    val bgColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(top = 16.dp),
    ) {
        val totalSteps = (uiState.exercises.size * 2 - 1).coerceAtLeast(0)
        val currentStepIndex = if (uiState.phase == SessionPhase.RESTING) {
            uiState.currentIndex * 2 + 1
        } else {
            uiState.currentIndex * 2
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (i in 0 until totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(if (i % 2 == 0) 2f else 1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                i < currentStepIndex -> primaryColor
                                i == currentStepIndex -> primaryColor.copy(alpha = 0.6f)
                                else -> buttonBgColor
                            }
                        ),
                )
            }
        }

        val current = uiState.currentExercise
        val next = uiState.nextExercise

        when (uiState.phase) {
            SessionPhase.PREPARING -> {
                PreparingPhaseContent(
                    currentExercise = current,
                    gifUrl = uiState.gifUrlsByName[current?.name],
                    countdownRemaining = uiState.countdownRemaining,
                    isBgMusicEnabled = uiState.isBgMusicEnabled,
                    onBack = onRequestExit,
                    onOpenSettings = onOpenSettings,
                    onToggleMusic = onToggleMusic,
                    onOpenInstructions = onOpenInstructions,
                    onSkip = onSkipPreparing,
                )
            }

            SessionPhase.EXERCISING -> {
                ExercisingPhaseContent(
                    currentExercise = current,
                    gifUrl = uiState.gifUrlsByName[current?.name],
                    remainingSeconds = uiState.exerciseRemaining,
                    isRunning = uiState.isExerciseRunning,
                    hasPrevious = uiState.currentIndex > 0,
                    hasNext = uiState.currentIndex < uiState.exercises.lastIndex,
                    isBgMusicEnabled = uiState.isBgMusicEnabled,
                    onBack = onRequestExit,
                    onOpenSettings = onOpenSettings,
                    onToggleMusic = onToggleMusic,
                    onOpenInstructions = onOpenInstructions,
                    onPrevious = onPreviousExercise,
                    onPrimaryAction = onPrimaryAction,
                    onNext = onNextExercise,
                )
            }

            SessionPhase.RESTING -> {
                RestPhaseScreen(
                    restSeconds = uiState.restRemaining,
                    nextExercise = next,
                    nextGifUrl = uiState.gifUrlsByName[next?.name],
                    nextIndexOneBased = uiState.currentIndex + 2,
                    totalExercises = uiState.exercises.size,
                    onAddTime = onAddRestTime,
                    onSkip = onSkipRest,
                    onBack = onRequestExit,
                )
            }
        }
    }

    if (uiState.showExitDialog) {
        SessionQuitDialog(
            completedExercisesCount = uiState.currentIndex,
            onConfirmQuit = {
                ttsController.stop()
                onConfirmQuit()
            },
            onKeepExercising = onDismissExit,
        )
    }

    if (uiState.showPauseDialog) {
        SessionPauseDialog(
            currentExercise = uiState.currentExercise,
            gifUrl = uiState.gifUrlsByName[uiState.currentExercise?.name],
            currentIndex = uiState.currentIndex,
            totalExercises = uiState.exercises.size,
            onResume = onResumeFromPause,
            onRestart = onRestartExerciseFromPause,
            onOpenInstructions = onOpenInstructions,
        )
    }

    uiState.selectedInstruction?.let { detail ->
        ExerciseInstructionSheetForWorkout(
            workoutExercise = detail.workoutExercise,
            catalogExercise = detail.catalogExercise,
            onClose = onCloseInstructions,
        )
    }
}
