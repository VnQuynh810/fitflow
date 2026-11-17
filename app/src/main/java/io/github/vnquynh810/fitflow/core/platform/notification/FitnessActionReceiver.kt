package io.github.vnquynh810.fitflow.core.platform.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import io.github.vnquynh810.fitflow.core.domain.repository.HealthRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.BmiCalculator
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FitnessActionReceiver : BroadcastReceiver() {

    @Inject lateinit var healthRepository: HealthRepository
    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            FitnessNotificationService.ACTION_TOGGLE_WATER -> {
                FitnessNotificationService.toggleWaterExpanded()
            }
            FitnessNotificationService.ACTION_WATER_MINUS -> {
                FitnessNotificationService.adjustInputMl(-50)
            }
            FitnessNotificationService.ACTION_WATER_PLUS -> {
                FitnessNotificationService.adjustInputMl(50)
            }
            FitnessNotificationService.ACTION_ADD_WATER -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val weight = preferencesRepository.userProfileFlow.first()?.weight ?: 57f
                        val defaultGoal = BmiCalculator.recommendedWaterGoalMl(weight)
                        healthRepository.addWater(
                            amountMl = FitnessNotificationService.currentInputMl(),
                            defaultWaterGoalMl = defaultGoal,
                        )
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
