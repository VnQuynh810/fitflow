package io.github.vnquynh810.fitflow.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import io.github.vnquynh810.fitflow.R
import io.github.vnquynh810.fitflow.core.model.FitnessGoal

object WorkoutBannerMapper {

    fun styleForGoal(goal: FitnessGoal?): Pair<List<Color>, Int> = when (goal) {
        FitnessGoal.WEIGHT_LOSS ->
            listOf(Color(0xFF00A86B), Color(0xFF008A56)) to R.drawable.banner_weight_loss
        FitnessGoal.MUSCLE_GAIN ->
            listOf(Color(0xFFFF5F07), Color(0xFFE04C00)) to R.drawable.banner_muscle_gain
        FitnessGoal.ENDURANCE ->
            listOf(Color(0xFF4B5563), Color(0xFF374151)) to R.drawable.banner_endurance
        FitnessGoal.MAINTENANCE ->
            listOf(Color(0xFF6B4EFF), Color(0xFF533BCC)) to R.drawable.banner_maintenance
        null ->
            listOf(Color(0xFFFF6D00), Color(0xFFFF3D00)) to R.drawable.banner_maintenance
    }

    @DrawableRes
    fun drawableForBannerKey(bannerKey: String): Int = when (bannerKey) {
        "abs_workout" -> R.drawable.abs_workout
        "bench_press_form" -> R.drawable.bench_press_form
        "full_shot_woman_holding_dumbbell" -> R.drawable.full_shot_woman_holding_dumbbell
        "lat_pulldown" -> R.drawable.lat_pulldown
        "full_body_workout" -> R.drawable.full_body_workout
        else -> R.drawable.banner_maintenance
    }
}
