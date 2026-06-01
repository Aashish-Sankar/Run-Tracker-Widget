package com.marathon.tracker.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.marathon.tracker.domain.model.ThemeMode

private val MarathonLightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    secondary = md_theme_light_secondary,
    tertiary = md_theme_light_tertiary,
)

private val MarathonDarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    secondary = md_theme_dark_secondary,
    tertiary = md_theme_dark_tertiary,
)

@Composable
fun MarathonTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MarathonDarkColorScheme
        else -> MarathonLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
