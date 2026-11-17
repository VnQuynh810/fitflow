package io.github.vnquynh810.fitflow.core.domain.repository

import io.github.vnquynh810.fitflow.core.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    suspend fun ensureSeeded()
    fun observeAll(): Flow<List<Exercise>>
    suspend fun getGifFileName(exerciseName: String): String?
    suspend fun findBestMatchByName(exerciseName: String): Exercise?
}
