package io.github.vnquynh810.fitflow.core.domain.usecase

import io.github.vnquynh810.fitflow.core.model.BmiCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class BmiCalculatorTest {

    @Test
    fun `calculate returns accurate BMI for standard height and weight`() {
        val bmi = BmiCalculator.calculate(heightCm = 175f, weightKg = 70f)
        assertEquals(22.86f, bmi, 0.05f)
        assertEquals(BmiCategory.NORMAL, BmiCalculator.categoryFor(bmi))
    }

    @Test
    fun `calculate safely returns zero on invalid non-positive inputs`() {
        assertEquals(0f, BmiCalculator.calculate(heightCm = 0f, weightKg = 70f), 0.001f)
        assertEquals(0f, BmiCalculator.calculate(heightCm = 170f, weightKg = -10f), 0.001f)
    }

    @Test
    fun `categoryFor classifies underweight normal and overweight boundaries`() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCalculator.categoryFor(18.4f))
        assertEquals(BmiCategory.NORMAL, BmiCalculator.categoryFor(18.5f))
        assertEquals(BmiCategory.NORMAL, BmiCalculator.categoryFor(24.9f))
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.categoryFor(25.0f))
    }

    @Test
    fun `recommendedWaterGoalMl scales with weight and clamps within healthy bounds`() {
        assertEquals(2100, BmiCalculator.recommendedWaterGoalMl(60f))
        assertEquals(1200, BmiCalculator.recommendedWaterGoalMl(20f))
        assertEquals(5000, BmiCalculator.recommendedWaterGoalMl(200f))
    }
}
