package com.citra.reverie.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ReverieLavender,
    onPrimary = ReverieNavyDeep,
    primaryContainer = ReverieCardBg,
    onPrimaryContainer = DarkTextPrimary,
    secondary = ReverieLightLav,
    onSecondary = ReverieNavyDeep,
    tertiary = ReveriePurpleHigh,
    onTertiary = ReverieNavyDeep,
    background = ReverieNavy,
    onBackground = DarkTextPrimary,
    surface = ReverieSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = ReverieCardBg,
    onSurfaceVariant = DarkTextSecondary,
    outline = ReverieBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccentPrimary,
    onPrimary = LightBgPrimary,
    primaryContainer = CardLightBg,
    onPrimaryContainer = LightTextPrimary,
    secondary = ReverieLavender,
    onSecondary = LightBgPrimary,
    tertiary = LightAccentPrimary,
    onTertiary = LightBgPrimary,
    background = LightBgPrimary,
    onBackground = LightTextPrimary,
    surface = LightBgSecondary,
    onSurface = LightTextPrimary,
    surfaceVariant = CardLightBg,
    onSurfaceVariant = LightTextSecondary,
    outline = LightSurfaceBorder
)

@Composable
fun ReverieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
