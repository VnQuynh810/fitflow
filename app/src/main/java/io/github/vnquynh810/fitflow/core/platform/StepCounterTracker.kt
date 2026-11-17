package io.github.vnquynh810.fitflow.core.platform

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.vnquynh810.fitflow.core.domain.repository.HealthRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.usecase.BmiCalculator
import io.github.vnquynh810.fitflow.core.model.StepSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class StepCounterTracker @Inject constructor(
    @ApplicationContext context: Context,
    private val healthRepository: HealthRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : SensorEventListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _isTrackingActive = MutableStateFlow(false)
    val isTrackingActive: StateFlow<Boolean> = _isTrackingActive.asStateFlow()

    fun setPermissionGranted(granted: Boolean) {
        _isPermissionGranted.value = granted
        if (!granted) {
            stopTracking()
            scope.launch { preferencesRepository.setStepSensorEnabled(false) }
        }
    }

    fun startTracking() {
        if (!_isPermissionGranted.value) {
            _isTrackingActive.value = false
            scope.launch { preferencesRepository.setStepSensorEnabled(false) }
            return
        }
        if (_isTrackingActive.value) return

        val registered = when {
            stepCounterSensor != null -> sensorManager.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_UI,
            )
            stepDetectorSensor != null -> sensorManager.registerListener(
                this,
                stepDetectorSensor,
                SensorManager.SENSOR_DELAY_UI,
            )
            else -> false
        }

        _isTrackingActive.value = registered
        scope.launch { preferencesRepository.setStepSensorEnabled(registered) }
    }

    fun stopTracking() {
        if (_isTrackingActive.value) {
            sensorManager.unregisterListener(this)
            _isTrackingActive.value = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorType = event?.sensor?.type ?: return
        scope.launch {
            val weight = preferencesRepository.userProfileFlow.first()?.weight ?: 57f
            val defaultWater = BmiCalculator.recommendedWaterGoalMl(weight)
            when (sensorType) {
                Sensor.TYPE_STEP_COUNTER -> {
                    val rawCounter = event.values.firstOrNull()?.toInt() ?: return@launch
                    healthRepository.updateSensorSteps(rawCounter, defaultWater)
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    healthRepository.incrementSteps(1, defaultWater, StepSource.SENSOR)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
