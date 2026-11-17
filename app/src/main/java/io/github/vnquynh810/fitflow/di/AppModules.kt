package io.github.vnquynh810.fitflow.di

import android.content.Context
import android.os.Build
import androidx.room.Room
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.vnquynh810.fitflow.core.data.AiCoachRepositoryImpl
import io.github.vnquynh810.fitflow.core.data.ExerciseRepositoryImpl
import io.github.vnquynh810.fitflow.core.data.HealthRepositoryImpl
import io.github.vnquynh810.fitflow.core.data.ProgressRepositoryImpl
import io.github.vnquynh810.fitflow.core.data.UserPreferencesRepositoryImpl
import io.github.vnquynh810.fitflow.core.data.WorkoutPlanRepositoryImpl
import io.github.vnquynh810.fitflow.core.database.AppDatabase
import io.github.vnquynh810.fitflow.core.database.ExerciseDao
import io.github.vnquynh810.fitflow.core.database.HealthDao
import io.github.vnquynh810.fitflow.core.database.ProgressDao
import io.github.vnquynh810.fitflow.core.domain.repository.AiCoachRepository
import io.github.vnquynh810.fitflow.core.domain.repository.ExerciseRepository
import io.github.vnquynh810.fitflow.core.domain.repository.HealthRepository
import io.github.vnquynh810.fitflow.core.domain.repository.ProgressRepository
import io.github.vnquynh810.fitflow.core.domain.repository.UserPreferencesRepository
import io.github.vnquynh810.fitflow.core.domain.repository.WorkoutPlanRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseAndMediaModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fitflow.db",
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun provideProgressDao(db: AppDatabase): ProgressDao = db.progressDao()

    @Provides
    fun provideHealthDao(db: AppDatabase): HealthDao = db.healthDao()

    @Provides
    @Singleton
    fun provideCoilImageLoader(@ApplicationContext context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("gif_cache"))
                    .maxSizeBytes(300L * 1024 * 1024)
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.15)
                    .build()
            }
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingsModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutPlanRepository(impl: WorkoutPlanRepositoryImpl): WorkoutPlanRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAiCoachRepository(impl: AiCoachRepositoryImpl): AiCoachRepository
}
