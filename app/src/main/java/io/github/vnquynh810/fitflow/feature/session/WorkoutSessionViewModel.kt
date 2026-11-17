package io.github.vnquynh810.fitflow.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.ExerciseRepository
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.GenerateWorkoutPlanUseCase
import io.github.vnquynh810.fitflow.core.domain.usecase.GetSupplementaryWorkoutUseCase
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.WorkoutAudioSettings
import io.github.vnquynh810.fitflow.core.model.WorkoutExercise
import io.github.vnquynh810.fitflow.core.platform.GifUrlProvider
import io.github.vnquynh810.fitflow.core.platform.WorkoutAudioController
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val getSupplementaryWorkoutUseCase: GetSupplementaryWorkoutUseCase,
    private val exerciseRepository: ExerciseRepository,
    private val progressRepository: ProgressRepository,
    private val gifUrlProvider: GifUrlProvider,
    private val workoutAudioController: WorkoutAudioController,
) : ViewModel() {

    private var initializedKey: String? = null
    private var sessionMode: SessionMode? = null
    private var isCompleting = false
    private var activeClockJob: Job? = null
    private var phaseTimerJob: Job? = null

    private val internalState = MutableStateFlow(WorkoutSessionUiState())
    private val audioSettingsState = MutableStateFlow(WorkoutAudioSettings())

    private val completedChannel = Channel<SessionCompletedEvent>(Channel.BUFFERED)
    val completedEvents: Flow<SessionCompletedEvent> = completedChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.audioSettingsFlow.collect { audioSettingsState.value = it }
        }
    }

    val uiState: StateFlow<WorkoutSessionUiState> = combine(
        internalState,
        userPreferencesRepository.audioSettingsFlow,
    ) { state, audio ->
        state.copy(
            isBgMusicEnabled = audio.isBgMusicEnabled,
            isVoiceGuideEnabled = audio.isVoiceGuideEnabled,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutSessionUiState(),
    )

    fun loadDaySession(dayNumber: Int, startIndex: Int) {
        val key = "day:$dayNumber:$startIndex"
        if (initializedKey == key) return
        initializedKey = key
        viewModelScope.launch {
            val goal = userPreferencesRepository.userProfileFlow.first()?.goal ?: FitnessGoal.WEIGHT_LOSS
            audioSettingsState.value = userPreferencesRepository.audioSettingsFlow.first()
            val dayPlan = generateWorkoutPlanUseCase.observeMergedPlan(goal).first()
                .find { it.dayNumber == dayNumber }
            sessionMode = SessionMode.PlanDay(goal = goal, dayNumber = dayNumber)
            startLoadedSession(dayPlan?.workoutExercises.orEmpty(), startIndex)
        }
    }

    fun loadSupplementarySession(workoutId: String) {
        val key = "supp:$workoutId"
        if (initializedKey == key) return
        initializedKey = key
        viewModelScope.launch {
            audioSettingsState.value = userPreferencesRepository.audioSettingsFlow.first()
            val workout = getSupplementaryWorkoutUseCase(workoutId)
            sessionMode = SessionMode.Supplementary(workoutId, workout?.title ?: "Workout")
            startLoadedSession(workout?.exercises.orEmpty(), 0)
        }
    }

    private fun startLoadedSession(exercises: List<WorkoutExercise>, initialIndex: Int) {
        isCompleting = false
        val safeIndex = if (exercises.isEmpty()) 0 else initialIndex.coerceIn(0, exercises.lastIndex)
        val gifMap = exercises.associate { ex -> ex.name to gifUrlProvider.resolve(ex.gifFileName) }
        internalState.value = WorkoutSessionUiState(
            isLoading = false,
            exercises = exercises,
            gifUrlsByName = gifMap,
            currentIndex = safeIndex,
            phase = SessionPhase.PREPARING,
            countdownRemaining = audioSettingsState.value.countdownDurationSeconds,
            exerciseRemaining = exercises.getOrNull(safeIndex)?.durationSec ?: 0,
        )
        workoutAudioController.startMusicIfEnabled()
        activeClockJob?.cancel()
        activeClockJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000L)
                internalState.update { it.copy(totalActiveSeconds = it.totalActiveSeconds + 1) }
            }
        }
        enterPreparingPhase()
    }

    private fun enterPreparingPhase() {
        phaseTimerJob?.cancel()
        val countdownSec = audioSettingsState.value.countdownDurationSeconds
        if (countdownSec <= 0) {
            enterExercisingPhase()
            return
        }
        internalState.update {
            it.copy(phase = SessionPhase.PREPARING, countdownRemaining = countdownSec, isExerciseRunning = false)
        }
        phaseTimerJob = viewModelScope.launch {
            while (isActive && internalState.value.countdownRemaining > 0) {
                delay(1_000L)
                if (!internalState.value.showExitDialog) {
                    internalState.update { s -> s.copy(countdownRemaining = (s.countdownRemaining - 1).coerceAtLeast(0)) }
                }
            }
            if (internalState.value.phase == SessionPhase.PREPARING) enterExercisingPhase()
        }
    }

    private fun enterExercisingPhase() {
        phaseTimerJob?.cancel()
        val current = internalState.value.currentExercise
        val duration = current?.durationSec ?: 0
        val isTimed = (current?.reps ?: 0) == 0 && duration > 0
        internalState.update {
            it.copy(
                phase = SessionPhase.EXERCISING,
                countdownRemaining = 0,
                exerciseRemaining = duration,
                isExerciseRunning = isTimed,
            )
        }
        if (isTimed) startExerciseCountdownLoop()
    }

    private fun startExerciseCountdownLoop() {
        phaseTimerJob?.cancel()
        phaseTimerJob = viewModelScope.launch {
            while (isActive && internalState.value.exerciseRemaining > 0) {
                delay(1_000L)
                val s = internalState.value
                if (s.phase == SessionPhase.EXERCISING && s.isExerciseRunning && !s.showPauseDialog && !s.showExitDialog) {
                    internalState.update { it.copy(exerciseRemaining = (it.exerciseRemaining - 1).coerceAtLeast(0)) }
                }
            }
            val after = internalState.value
            if (after.phase == SessionPhase.EXERCISING && after.exerciseRemaining == 0 && after.isExerciseRunning) {
                internalState.update { it.copy(isExerciseRunning = false) }
                advanceAfterExerciseFinished()
            }
        }
    }

    private fun enterRestingPhase() {
        phaseTimerJob?.cancel()
        val restSec = audioSettingsState.value.restDurationSeconds
        if (restSec <= 0) {
            moveToNextExerciseOrFinish()
            return
        }
        internalState.update {
            it.copy(phase = SessionPhase.RESTING, restRemaining = restSec, isExerciseRunning = false)
        }
        phaseTimerJob = viewModelScope.launch {
            while (isActive && internalState.value.restRemaining > 0) {
                delay(1_000L)
                if (!internalState.value.showExitDialog) {
                    internalState.update { s -> s.copy(restRemaining = (s.restRemaining - 1).coerceAtLeast(0)) }
                }
            }
            if (internalState.value.phase == SessionPhase.RESTING) moveToNextExerciseOrFinish()
        }
    }

    private fun advanceAfterExerciseFinished() {
        val state = internalState.value
        if (state.currentIndex < state.exercises.lastIndex) enterRestingPhase() else finishWorkoutSession()
    }

    private fun moveToNextExerciseOrFinish() {
        val state = internalState.value
        if (state.currentIndex < state.exercises.lastIndex) {
            val nextIdx = state.currentIndex + 1
            internalState.update {
                it.copy(currentIndex = nextIdx, exerciseRemaining = it.exercises.getOrNull(nextIdx)?.durationSec ?: 0)
            }
            enterPreparingPhase()
        } else {
            finishWorkoutSession()
        }
    }

    fun skipPreparing() = enterExercisingPhase()

    fun goToPreviousExercise() {
        val state = internalState.value
        if (state.currentIndex > 0) {
            val prevIdx = state.currentIndex - 1
            internalState.update {
                it.copy(currentIndex = prevIdx, exerciseRemaining = it.exercises.getOrNull(prevIdx)?.durationSec ?: 0)
            }
            enterPreparingPhase()
        }
    }

    fun onPrimaryExerciseAction() {
        val state = internalState.value
        val current = state.currentExercise ?: return
        if (current.reps > 0 || state.exerciseRemaining == 0) {
            advanceAfterExerciseFinished()
        } else if (state.isExerciseRunning) {
            workoutAudioController.pauseMusic()
            internalState.update { it.copy(isExerciseRunning = false, showPauseDialog = true) }
        } else {
            workoutAudioController.resumeMusic()
            internalState.update { it.copy(isExerciseRunning = true, showPauseDialog = false) }
            startExerciseCountdownLoop()
        }
    }

    fun skipCurrentExercise() = advanceAfterExerciseFinished()
    fun addRestTime(seconds: Int = 20) = internalState.update { it.copy(restRemaining = it.restRemaining + seconds) }
    fun skipRest() = moveToNextExerciseOrFinish()

    fun resumeFromPauseDialog() {
        workoutAudioController.resumeMusic()
        internalState.update { it.copy(showPauseDialog = false, isExerciseRunning = true) }
        startExerciseCountdownLoop()
    }

    fun restartCurrentExerciseFromPause() {
        val duration = internalState.value.currentExercise?.durationSec ?: 0
        workoutAudioController.resumeMusic()
        internalState.update { it.copy(showPauseDialog = false, exerciseRemaining = duration, isExerciseRunning = true) }
        startExerciseCountdownLoop()
    }

    fun requestExitDialog() = internalState.update { it.copy(showExitDialog = true) }

    fun dismissExitDialog() {
        internalState.update { state ->
            val resumeTimed = state.phase == SessionPhase.EXERCISING &&
                (state.currentExercise?.reps ?: 0) == 0 && !state.showPauseDialog
            state.copy(showExitDialog = false, isExerciseRunning = if (resumeTimed) true else state.isExerciseRunning)
        }
    }

    fun confirmQuitSession(onExited: () -> Unit) {
        activeClockJob?.cancel()
        phaseTimerJob?.cancel()
        workoutAudioController.stopMusic()
        val state = internalState.value
        val mode = sessionMode
        internalState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch {
            if (mode is SessionMode.PlanDay) {
                progressRepository.savePartialProgress(mode.goal, mode.dayNumber, state.currentIndex)
            }
            onExited()
        }
    }

    fun toggleBgMusic() {
        viewModelScope.launch {
            val nextEnabled = !audioSettingsState.value.isBgMusicEnabled
            userPreferencesRepository.updateAudioSettings { it.copy(isBgMusicEnabled = nextEnabled) }
            if (nextEnabled) workoutAudioController.startMusicIfEnabled() else workoutAudioController.stopMusic()
        }
    }

    fun openInstructionSheet() {
        val current = internalState.value.currentExercise ?: return
        viewModelScope.launch {
            val catalog = exerciseRepository.findBestMatchByName(current.name)
            internalState.update { it.copy(selectedInstruction = SessionInstructionDetail(current, catalog)) }
        }
    }

    fun closeInstructionSheet() = internalState.update { it.copy(selectedInstruction = null) }

    private fun finishWorkoutSession() {
        if (isCompleting) return
        isCompleting = true
        activeClockJob?.cancel()
        phaseTimerJob?.cancel()
        workoutAudioController.stopMusic()

        val state = internalState.value
        val activeSec = state.totalActiveSeconds
        val totalKcal = state.exercises.sumOf { it.kcal }
        val mode = sessionMode ?: return

        viewModelScope.launch {
            val today = LocalDate.now()
            val now = System.currentTimeMillis()
            when (mode) {
                is SessionMode.PlanDay -> {
                    val isNew = progressRepository.recordWorkoutCompletion(
                        mode.goal, mode.dayNumber, today, now, activeSec, totalKcal, false
                    )
                    completedChannel.send(SessionCompletedEvent.PlanDayCompleted(mode.dayNumber, activeSec, isNew))
                }
                is SessionMode.Supplementary -> {
                    progressRepository.recordWorkoutCompletion(
                        null, -1, today, now, activeSec, totalKcal, false, mode.title
                    )
                    completedChannel.send(SessionCompletedEvent.SupplementaryCompleted(mode.workoutId, activeSec))
                }
            }
        }
    }

    override fun onCleared() {
        activeClockJob?.cancel()
        phaseTimerJob?.cancel()
        workoutAudioController.stopMusic()
        super.onCleared()
    }
}
