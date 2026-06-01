package com.marathon.tracker.presentation.theme

import androidx.compose.ui.graphics.Color
import com.marathon.tracker.domain.model.TrainingPhase

val md_theme_light_primary = Color(0xFF1565C0)
val md_theme_light_secondary = Color(0xFF00695C)
val md_theme_light_tertiary = Color(0xFFE65100)

val md_theme_dark_primary = Color(0xFF82B1FF)
val md_theme_dark_secondary = Color(0xFF80CBC4)
val md_theme_dark_tertiary = Color(0xFFFFCC80)

fun TrainingPhase.toColor(): Color = when (this) {
    TrainingPhase.BASE_BUILDING -> Color(colorHex)
    TrainingPhase.AEROBIC_DEVELOPMENT -> Color(colorHex)
    TrainingPhase.TEMPO_INTRODUCTION -> Color(colorHex)
    TrainingPhase.RACE_PREP -> Color(colorHex)
    TrainingPhase.PEAK_TRAINING -> Color(colorHex)
    TrainingPhase.TAPER -> Color(colorHex)
    TrainingPhase.RACE_WEEK -> Color(colorHex)
    TrainingPhase.RECOVERY -> Color(colorHex)
}
