package io.github.vnquynh810.fitflow

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import io.github.vnquynh810.fitflow.core.datastore.LegacyPreferencesMigrator
import io.github.vnquynh810.fitflow.core.domain.repository.ExerciseRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class FitFlowApp : Application(), ImageLoaderFactory {

    @Inject lateinit var coilImageLoader: ImageLoader
    @Inject lateinit var legacyMigrator: LegacyPreferencesMigrator
    @Inject lateinit var exerciseRepository: ExerciseRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            legacyMigrator.migrateIfNeeded()
            exerciseRepository.ensureSeeded()
        }
    }

    override fun newImageLoader(): ImageLoader = coilImageLoader
}
