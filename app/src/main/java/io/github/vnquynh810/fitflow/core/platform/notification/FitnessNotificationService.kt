package io.github.vnquynh810.fitflow.core.platform.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.vnquynh810.fitflow.MainActivity
import io.github.vnquynh810.fitflow.R
import io.github.vnquynh810.fitflow.core.domain.repository.HealthRepository
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.BmiCalculator
import io.github.vnquynh810.fitflow.core.domain.usecase.GenerateWorkoutPlanUseCase
import io.github.vnquynh810.fitflow.core.model.FitnessGoal
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FitnessNotificationService : Service() {

    @Inject lateinit var healthRepository: HealthRepository
    @Inject lateinit var progressRepository: ProgressRepository
    @Inject lateinit var preferencesRepository: UserPreferencesRepository
    @Inject lateinit var generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private data class NotificationUiState(
        val waterCurrent: Int = 0,
        val waterGoal: Int = 2000,
        val inputMl: Int = 250,
        val waterExpanded: Boolean = false,
        val challengeName: String = "FitFlow Training",
        val currentDay: Int = 1,
        val totalDays: Int = 30,
        val status: String = "todo",
    )

    private var latestState = NotificationUiState()

    companion object {
        const val NOTIF_ID = 2
        const val CHANNEL_ID = "fitness_status_channel"

        const val ACTION_TOGGLE_WATER = "io.github.vnquynh810.fitflow.ACTION_TOGGLE_WATER"
        const val ACTION_WATER_MINUS = "io.github.vnquynh810.fitflow.ACTION_WATER_MINUS"
        const val ACTION_WATER_PLUS = "io.github.vnquynh810.fitflow.ACTION_WATER_PLUS"
        const val ACTION_ADD_WATER = "io.github.vnquynh810.fitflow.ACTION_ADD_WATER"

        private val waterExpandedFlow = MutableStateFlow(false)
        private val inputMlFlow = MutableStateFlow(250)

        fun toggleWaterExpanded() {
            waterExpandedFlow.value = !waterExpandedFlow.value
        }

        fun adjustInputMl(delta: Int) {
            inputMlFlow.value = (inputMlFlow.value + delta).coerceIn(50, 1000)
        }

        fun currentInputMl(): Int = inputMlFlow.value
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        ensureChannel()

        serviceScope.launch {
            val planAndProgressFlow = preferencesRepository.userProfileFlow.flatMapLatest { profile ->
                val goal = profile?.goal ?: FitnessGoal.WEIGHT_LOSS
                val defaultWater = BmiCalculator.recommendedWaterGoalMl(profile?.weight ?: 57f)
                combine(
                    healthRepository.observeTodayMetrics(defaultWater),
                    generateWorkoutPlanUseCase.observeMergedPlan(goal),
                    progressRepository.observeGoalProgress(goal),
                ) { health, plan, progress ->
                    val nextDay = plan.firstOrNull { it.dayNumber !in progress.completedDays }
                    val stateTag = when {
                        nextDay == null && plan.isNotEmpty() -> "done"
                        nextDay?.isRest == true -> "rest"
                        else -> "todo"
                    }
                    Triple(health, nextDay to plan.size.coerceAtLeast(1), stateTag)
                }
            }

            combine(
                planAndProgressFlow,
                waterExpandedFlow,
                inputMlFlow,
            ) { (health, dayPair, statusTag), expanded, inputMl ->
                val (nextDay, totalDays) = dayPair
                NotificationUiState(
                    waterCurrent = health.waterIntakeMl,
                    waterGoal = health.waterGoalMl,
                    inputMl = inputMl,
                    waterExpanded = expanded,
                    challengeName = nextDay?.title?.ifBlank { "Day ${nextDay.dayNumber}" } ?: "Plan Complete",
                    currentDay = nextDay?.dayNumber ?: totalDays,
                    totalDays = totalDays,
                    status = statusTag,
                )
            }.collect { state ->
                latestState = state
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIF_ID, buildNotification(state))
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification(latestState))
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(state: NotificationUiState): Notification {
        val views = RemoteViews(packageName, R.layout.notification_fitness)

        views.setTextViewText(R.id.tv_water, "${state.waterCurrent} / ${state.waterGoal} ml")
        views.setProgressBar(R.id.progress_water, state.waterGoal.coerceAtLeast(1), state.waterCurrent, false)
        views.setTextViewText(R.id.tv_ml_input, "${state.inputMl} ml")
        views.setViewVisibility(
            R.id.layout_water_expand,
            if (state.waterExpanded) View.VISIBLE else View.GONE,
        )

        fun actionPendingIntent(action: String): PendingIntent = PendingIntent.getBroadcast(
            this,
            action.hashCode(),
            Intent(this, FitnessActionReceiver::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        views.setOnClickPendingIntent(R.id.row_water, actionPendingIntent(ACTION_TOGGLE_WATER))
        views.setOnClickPendingIntent(R.id.btn_minus, actionPendingIntent(ACTION_WATER_MINUS))
        views.setOnClickPendingIntent(R.id.btn_plus, actionPendingIntent(ACTION_WATER_PLUS))
        views.setOnClickPendingIntent(R.id.btn_add_water, actionPendingIntent(ACTION_ADD_WATER))

        views.setTextViewText(R.id.tv_challenge_name, state.challengeName.uppercase())
        views.setTextViewText(R.id.tv_challenge_day, "Day ${state.currentDay}")
        val completedCount = (state.currentDay - 1).coerceAtLeast(0)
        views.setProgressBar(R.id.progress_challenge, state.totalDays, completedCount, false)
        views.setTextViewText(R.id.tv_challenge_progress, "$completedCount / ${state.totalDays} days")

        when (state.status) {
            "done" -> {
                views.setImageViewResource(R.id.ic_status, R.drawable.ic_trophy)
                views.setTextViewText(R.id.tv_status, "Completed")
                views.setTextColor(R.id.tv_status, Color.parseColor("#639922"))
                views.setInt(R.id.layout_status, "setBackgroundResource", R.drawable.badge_bg_green)
            }
            "rest" -> {
                views.setImageViewResource(R.id.ic_status, R.drawable.ic_moon)
                views.setTextViewText(R.id.tv_status, "Rest Day")
                views.setTextColor(R.id.tv_status, Color.parseColor("#888780"))
                views.setInt(R.id.layout_status, "setBackgroundResource", R.drawable.badge_bg_gray)
            }
            else -> {
                views.setImageViewResource(R.id.ic_status, R.drawable.ic_run)
                views.setTextViewText(R.id.tv_status, "Ready")
                views.setTextColor(R.id.tv_status, Color.parseColor("#F5651A"))
                views.setInt(R.id.layout_status, "setBackgroundResource", R.drawable.badge_bg_orange)
            }
        }

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .setCustomBigContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Fitness Status",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            setShowBadge(false)
            description = "Workout progress and daily hydration tracker"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
