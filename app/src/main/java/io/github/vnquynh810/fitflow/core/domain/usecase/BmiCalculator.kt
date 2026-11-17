package io.github.vnquynh810.fitflow.core.domain.usecase

import io.github.vnquynh810.fitflow.core.model.BmiCategory

object BmiCalculator {
    fun calculate(heightCm: Float, weightKg: Float): Float {
        if (heightCm <= 0f || weightKg <= 0f) return 0f
        val heightMeters = heightCm / 100f
        return weightKg / (heightMeters * heightMeters)
    }

    fun categoryFor(bmi: Float): BmiCategory = when {
        bmi < 18.5f -> BmiCategory.UNDERWEIGHT
        bmi < 25.0f -> BmiCategory.NORMAL
        else -> BmiCategory.OVERWEIGHT
    }

    fun recommendedWaterGoalMl(weightKg: Float): Int {
        val safeWeight = if (weightKg > 0f) weightKg else 57f
        return (safeWeight * 35f).toInt().coerceIn(1200, 5000)
    }
}
