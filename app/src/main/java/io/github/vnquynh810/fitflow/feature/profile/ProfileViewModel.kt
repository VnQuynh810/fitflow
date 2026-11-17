package io.github.vnquynh810.fitflow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.domain.repository.HealthRepository
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.BmiCalculator
import io.github.vnquynh810.fitflow.core.domain.usecase.GenerateWorkoutPlanUseCase
import io.github.vnquynh810.fitflow.core.model.DailyHealthMetrics
import io.github.vnquynh810.fitflow.core.model.DayPlan
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import io.github.vnquynh810.fitflow.core.model.GoalProgressSummary
import io.github.vnquynh810.fitflow.core.model.UserProfile
import io.github.vnquynh810.fitflow.core.model.WeightRecord
import io.github.vnquynh810.fitflow.core.model.WorkoutLogEntry
import io.github.vnquynh810.fitflow.core.platform.StepCounterTracker
import io.github.vnquynh810.fitflow.core.platform.WorkoutReminderScheduler
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val completedDays: Set<Int> = emptySet(),
    val workoutPlan: List<DayPlan> = emptyList(),
    val startDate: LocalDate? = null,
    val completedDateMap: Map<LocalDate, Int> = emptyMap(),
    val weightHistory: List<WeightRecord> = emptyList(),
    val healthMetricsHistory: List<DailyHealthMetrics> = emptyList(),
    val todayHealthMetrics: DailyHealthMetrics? = null,
    val globalWorkoutLogs: List<WorkoutLogEntry> = emptyList(),
    val isActivityRecognitionGranted: Boolean = false,
    val isStepSensorEnabled: Boolean = false,
    val isStepTrackingActive: Boolean = false,
)

private data class ProfileCoreBundle(
    val profile: UserProfile?,
    val plan: List<DayPlan>,
    val progress: GoalProgressSummary,
    val startDate: LocalDate?,
    val logs: List<WorkoutLogEntry>,
)

private data class ProfileSensorBundle(
    val isPermissionGranted: Boolean,
    val isSensorEnabled: Boolean,
    val isTrackingActive: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val progressRepository: ProgressRepository,
    private val healthRepository: HealthRepository,
    private val stepCounterTracker: StepCounterTracker,
    private val workoutReminderScheduler: WorkoutReminderScheduler,
) : ViewModel() {

    private val planAndProgressFlow = userPreferencesRepository.userProfileFlow
        .flatMapLatest { profile ->
            val goal = profile?.goal ?: FitnessGoal.WEIGHT_LOSS
            combine(
                generateWorkoutPlanUseCase.observeMergedPlan(goal),
                progressRepository.observeGoalProgress(goal),
            ) { plan, progress ->
                Triple(profile, plan, progress)
            }
        }

    private val todayMetricsFlow = userPreferencesRepository.userProfileFlow
        .flatMapLatest { profile ->
            val defaultWater = BmiCalculator.recommendedWaterGoalMl(profile?.weight ?: 57f)
            healthRepository.observeTodayMetrics(defaultWater)
        }

    private val coreBundleFlow = combine(
        planAndProgressFlow,
        userPreferencesRepository.startDateFlow,
        progressRepository.observeWorkoutLogs(),
    ) { (profile, plan, progress), startDate, logs ->
        ProfileCoreBundle(
            profile = profile,
            plan = plan,
            progress = progress,
            startDate = startDate,
            logs = logs,
        )
    }

    private val sensorBundleFlow = combine(
        stepCounterTracker.isPermissionGranted,
        userPreferencesRepository.isStepSensorEnabledFlow,
        stepCounterTracker.isTrackingActive,
    ) { granted, enabled, active ->
        ProfileSensorBundle(
            isPermissionGranted = granted,
            isSensorEnabled = enabled,
            isTrackingActive = active,
        )
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        coreBundleFlow,
        todayMetricsFlow,
        healthRepository.observeWeightHistory(),
        healthRepository.observeHealthHistory(),
        sensorBundleFlow,
    ) { core, todayMetrics, weights, healthHistory, sensor ->
        ProfileUiState(
            userProfile = core.profile,
            completedDays = core.progress.completedDays,
            workoutPlan = core.plan,
            startDate = core.startDate,
            completedDateMap = core.progress.completedDateMap,
            weightHistory = weights,
            healthMetricsHistory = healthHistory,
            todayHealthMetrics = todayMetrics,
            globalWorkoutLogs = core.logs,
            isActivityRecognitionGranted = sensor.isPermissionGranted,
            isStepSensorEnabled = sensor.isSensorEnabled,
            isStepTrackingActive = sensor.isTrackingActive,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    fun recordWeight(weightKg: Float, date: LocalDate) {
        viewModelScope.launch {
            healthRepository.recordWeight(weightKg = weightKg, date = date)
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            healthRepository.addWater(amountMl, resolveDefaultWaterGoal())
        }
    }

    fun setWaterGoal(goalMl: Int) {
        viewModelScope.launch {
            healthRepository.setWaterGoal(goalMl, resolveDefaultWaterGoal())
        }
    }

    fun setStepGoal(goalSteps: Int) {
        viewModelScope.launch {
            healthRepository.setStepGoal(goalSteps, resolveDefaultWaterGoal())
        }
    }

    fun ensureStepTracking() {
        stepCounterTracker.startTracking()
    }

    fun triggerDemoNotification() {
        workoutReminderScheduler.scheduleDemoReminder()
    }

    private suspend fun resolveDefaultWaterGoal(): Int {
        val weight = uiState.value.userProfile?.weight
            ?: userPreferencesRepository.userProfileFlow.first()?.weight
            ?: 57f
        return BmiCalculator.recommendedWaterGoalMl(weight)
    }
}
