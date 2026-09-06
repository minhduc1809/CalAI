package com.calai.app.presentation.theme

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

/**
 * 1. Theme Dark Luxury Canvas (Mục 9.3 - Chuẩn chính của app)
 */
val CalAIDarkColorScheme = darkColorScheme(
    primary = VividOrange,
    onPrimary = TextWhite,
    primaryContainer = VividOrangeDark,
    onPrimaryContainer = TextWhite,
    secondary = LavenderGradientStart,
    onSecondary = TextDeepInk,
    secondaryContainer = CharcoalCard,
    onSecondaryContainer = TextLightGrey,
    tertiary = ProteinGradientStart,
    onTertiary = TextDeepInk,
    background = ObsidianBackground,
    onBackground = TextWhite,
    surface = CharcoalSurface,
    onSurface = TextWhite,
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = TextMuted,
    surfaceContainerHighest = CharcoalCardElevated,
    outline = CharcoalBorder,
    outlineVariant = CharcoalBorder,
    error = CrimsonError,
    onError = TextWhite
)

/**
 * 2. Theme Ivory Luxury Canvas (Mục 9.4 - Chế độ sáng sang trọng)
 */
val CalAILightColorScheme = lightColorScheme(
    primary = VividOrange,
    onPrimary = TextWhite,
    primaryContainer = VividOrangeLight,
    onPrimaryContainer = TextWhite,
    secondary = PastelLavenderLight,
    onSecondary = TextInkPrimary,
    secondaryContainer = PearlCard,
    onSecondaryContainer = TextInkSecondary,
    tertiary = PastelProteinLight,
    onTertiary = TextInkPrimary,
    background = IvoryBackground,
    onBackground = TextInkPrimary,
    surface = PearlSurface,
    onSurface = TextInkPrimary,
    surfaceVariant = PearlCard,
    onSurfaceVariant = TextInkMuted,
    surfaceContainerHighest = PearlCardElevated,
    outline = PearlBorder,
    outlineVariant = PearlBorder,
    error = CrimsonError,
    onError = TextWhite
)

@Composable
fun CalAITheme(
    darkTheme: Boolean = true, // Mặc định luôn là Dark Luxury theo Spec 9.3
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CalAIDarkColorScheme else CalAILightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bgArgb = if (darkTheme) ObsidianBackground.toArgb() else IvoryBackground.toArgb()
            window.statusBarColor = bgArgb
            window.navigationBarColor = bgArgb
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

