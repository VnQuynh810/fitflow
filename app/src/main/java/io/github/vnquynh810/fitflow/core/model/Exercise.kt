package io.github.vnquynh810.fitflow.core.model

data class Exercise(
    val name: String,
    val difficulty: String,
    val exerciseType: String,
    val logType: String,
    val targetMuscles: List<String>,
    val equipment: List<String>,
    val instructions: List<String>,
    val localGifs: List<String>,
)
