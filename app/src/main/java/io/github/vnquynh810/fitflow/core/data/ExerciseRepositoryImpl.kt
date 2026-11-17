package io.github.vnquynh810.fitflow.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.vnquynh810.fitflow.core.database.ExerciseDao
import io.github.vnquynh810.fitflow.core.database.ExerciseEntity
import io.github.vnquynh810.fitflow.core.domain.repository.ExerciseRepository
import io.github.vnquynh810.fitflow.core.model.Exercise
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val seedMutex = Mutex()
    private val matchCache = ConcurrentHashMap<String, Exercise>()
    private val misses = ConcurrentHashMap.newKeySet<String>()

    @Volatile
    private var cachedExercises: List<Exercise>? = null

    @Serializable
    private data class SeedExerciseDto(
        val name: String,
        val difficulty: String = "intermediate",
        @SerialName("exercise_type") val exerciseType: String = "Strength",
        @SerialName("log_type") val logType: String = "reps",
        @SerialName("target_muscles") val targetMuscles: List<String> = emptyList(),
        val equipment: List<String> = emptyList(),
        val instructions: List<String> = emptyList(),
        @SerialName("local_gifs") val localGifs: List<String> = emptyList(),
    )

    override suspend fun ensureSeeded() {
        if (exerciseDao.count() > 0) return
        seedMutex.withLock {
            if (exerciseDao.count() > 0) return
            withContext(Dispatchers.IO) {
                val rawJson = context.assets.open("exercises.json")
                    .bufferedReader()
                    .use { it.readText() }
                val dtos = json.decodeFromString<List<SeedExerciseDto>>(rawJson)
                val entities = dtos.map { dto ->
                    ExerciseEntity(
                        name = dto.name,
                        difficulty = dto.difficulty,
                        exerciseType = dto.exerciseType,
                        logType = dto.logType,
                        targetMuscles = dto.targetMuscles,
                        equipment = dto.equipment,
                        instructions = dto.instructions,
                        localGifs = dto.localGifs,
                    )
                }
                exerciseDao.insertAll(entities)
                cachedExercises = entities.map { it.toDomain() }
            }
        }
    }

    override fun observeAll(): Flow<List<Exercise>> =
        exerciseDao.observeAll().map { entities ->
            entities.map { it.toDomain() }.also { cachedExercises = it }
        }

    override suspend fun getGifFileName(exerciseName: String): String? =
        findBestMatchByName(exerciseName)?.localGifs?.firstOrNull()

    override suspend fun findBestMatchByName(exerciseName: String): Exercise? {
        if (exerciseName.isBlank()) return null
        matchCache[exerciseName]?.let { return it }
        if (exerciseName in misses) return null

        ensureSeeded()

        val direct = exerciseDao.getByNameIgnoreCase(exerciseName)?.toDomain()
        if (direct != null) {
            matchCache[exerciseName] = direct
            return direct
        }

        val allExercises = cachedExercises ?: withContext(Dispatchers.IO) {
            exerciseDao.getAllSnapshot().map { it.toDomain() }.also { cachedExercises = it }
        }

        val normalizedTarget = normalizeName(exerciseName)
        var resolved = allExercises.firstOrNull { normalizeName(it.name) == normalizedTarget }

        if (resolved == null) {
            val targetWords = exerciseName.lowercase()
                .split(Regex("[^a-z0-9]+"))
                .filter { it.length > 2 }
                .toSet()

            if (targetWords.isNotEmpty()) {
                var bestMatch: Exercise? = null
                var maxScore = 0
                for (candidate in allExercises) {
                    val exWords = candidate.name.lowercase()
                        .split(Regex("[^a-z0-9]+"))
                        .filter { it.length > 2 }
                        .toSet()
                    val overlap = targetWords.intersect(exWords).size
                    if (overlap > maxScore ||
                        (overlap > 0 && overlap == maxScore && bestMatch != null && candidate.name.length < bestMatch.name.length)
                    ) {
                        maxScore = overlap
                        bestMatch = candidate
                    }
                }
                if (maxScore > 0) {
                    resolved = bestMatch
                }
            }
        }

        if (resolved == null) {
            resolved = allExercises.firstOrNull {
                val normEx = normalizeName(it.name)
                normEx.isNotEmpty() && (normEx.contains(normalizedTarget) || normalizedTarget.contains(normEx))
            }
        }

        if (resolved != null) {
            matchCache[exerciseName] = resolved
        } else {
            misses.add(exerciseName)
        }
        return resolved
    }

    private fun normalizeName(value: String): String =
        value.lowercase().replace("&", "and").replace(Regex("[^a-z0-9]+"), "")
}
