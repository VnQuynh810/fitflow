package io.github.vnquynh810.fitflow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vnquynh810.fitflow.core.datastore.LegacyPreferencesMigrator
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.designsystem.theme.FitflowTheme
import io.github.vnquynh810.fitflow.core.platform.StepCounterTracker
import io.github.vnquynh810.fitflow.core.platform.notification.FitnessNotificationService
import io.github.vnquynh810.fitflow.navigation.AppRoute
import io.github.vnquynh810.fitflow.navigation.FitFlowNavHost
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MainStartupState {
    data object Loading : MainStartupState
    data class Ready(val startRoute: AppRoute) : MainStartupState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    legacyMigrator: LegacyPreferencesMigrator,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _startupState = MutableStateFlow<MainStartupState>(MainStartupState.Loading)
    val startupState: StateFlow<MainStartupState> = _startupState.asStateFlow()

    init {
        viewModelScope.launch {
            legacyMigrator.migrateIfNeeded()
            val onboarded = preferencesRepository.isOnboardedSnapshot()
            _startupState.value = MainStartupState.Ready(
                if (onboarded) AppRoute.Dashboard else AppRoute.Onboarding()
            )
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var stepCounterTracker: StepCounterTracker

    private val mainViewModel: MainActivityViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startFitnessStatusService()
    }

    private val activityRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        stepCounterTracker.setPermissionGranted(granted)
        if (granted) stepCounterTracker.startTracking()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationServiceIfPermitted()
        syncStepPermissionAndTracking()

        setContent {
            FitflowTheme {
                val startupState by mainViewModel.startupState.collectAsStateWithLifecycle()
                Scaffold { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(paddingValues),
                    ) {
                        when (val state = startupState) {
                            MainStartupState.Loading -> Unit
                            is MainStartupState.Ready -> {
                                FitFlowNavHost(
                                    startDestination = state.startRoute,
                                    onRequestStepPermission = ::requestActivityRecognitionPermission,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        syncStepPermissionAndTracking()
    }

    override fun onStop() {
        stepCounterTracker.stopTracking()
        super.onStop()
    }

    private fun requestNotificationServiceIfPermitted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                startFitnessStatusService()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startFitnessStatusService()
        }
    }

    private fun startFitnessStatusService() {
        runCatching {
            ContextCompat.startForegroundService(
                this,
                Intent(this, FitnessNotificationService::class.java),
            )
        }
    }

    private fun requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    private fun syncStepPermissionAndTracking() {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        stepCounterTracker.setPermissionGranted(granted)
        if (granted) stepCounterTracker.startTracking()
    }
}
