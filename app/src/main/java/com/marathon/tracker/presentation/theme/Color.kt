package com.marathon.tracker.presentation.theme

import androidx.compose.ui.graphics.Color
import com.marathon.tracker.domain.model.TrainingPhase

// Strava-inspired palette
// Primary: Strava orange #FC4C02
// Secondary: warm coral #E8390E
// Tertiary: amber for race highlights

val md_theme_light_primary = Color(0xFFFC4C02)       // Strava orange
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFFFDBCF)
val md_theme_light_onPrimaryContainer = Color(0xFF3A0A00)

val md_theme_light_secondary = Color(0xFF9C4300)      // Deep burnt orange
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFFFDBCF)
val md_theme_light_onSecondaryContainer = Color(0xFF360E00)

val md_theme_light_tertiary = Color(0xFF755A00)       // Amber/gold for race days
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFDF92)
val md_theme_light_onTertiaryContainer = Color(0xFF241A00)

val md_theme_light_background = Color(0xFFFFFBFF)
val md_theme_light_surface = Color(0xFFFFFBFF)
val md_theme_light_surfaceVariant = Color(0xFFF4DDD6)  // Warm tinted surface
val md_theme_light_onSurfaceVariant = Color(0xFF52443F)
val md_theme_light_outline = Color(0xFF85736D)

// Dark theme: Strava's dark app feel — charcoal base, vivid orange accents
val md_theme_dark_primary = Color(0xFFFFB59C)         // Soft orange on dark
val md_theme_dark_onPrimary = Color(0xFF5C1500)
val md_theme_dark_primaryContainer = Color(0xFFBE3500)  // Rich burnt orange container
val md_theme_dark_onPrimaryContainer = Color(0xFFFFDBCF)

val md_theme_dark_secondary = Color(0xFFFFB59C)
val md_theme_dark_onSecondary = Color(0xFF541E00)
val md_theme_dark_secondaryContainer = Color(0xFF773200)
val md_theme_dark_onSecondaryContainer = Color(0xFFFFDBCF)

val md_theme_dark_tertiary = Color(0xFFF5C010)        // Bright gold on dark
val md_theme_dark_onTertiary = Color(0xFF3D2E00)
val md_theme_dark_tertiaryContainer = Color(0xFF584400)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFDF92)

val md_theme_dark_background = Color(0xFF201A19)       // Strava dark charcoal
val md_theme_dark_surface = Color(0xFF201A19)
val md_theme_dark_surfaceVariant = Color(0xFF52443F)
val md_theme_dark_onSurfaceVariant = Color(0xFFD8C2BB)
val md_theme_dark_outline = Color(0xFFA08D87)

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
