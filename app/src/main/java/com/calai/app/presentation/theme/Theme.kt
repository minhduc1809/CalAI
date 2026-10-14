package com.calai.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 1. Theme Dark Luxury Canvas (Mục 9.3 - Chuẩn chính của app)
 */
val CalAIDarkColorScheme = darkColorScheme(
    primary = VividOrange,
    onPrimary = OnPrimary,
    primaryContainer = PeachDarkTint,
    onPrimaryContainer = PeachOnDark,
    inversePrimary = VividOrangeDark,
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
    onSurfaceVariant = TextLightGrey,
    surfaceTint = Color.Transparent,
    surfaceDim = ObsidianBackground,
    surfaceBright = Color(0xFF38383A),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF141416),
    surfaceContainer = CharcoalSurface,
    surfaceContainerHigh = CharcoalCard,
    surfaceContainerHighest = CharcoalCardElevated,
    inverseSurface = Color(0xFFF5F0EE),
    inverseOnSurface = Color(0xFF302B2A),
    outline = CharcoalBorder,
    outlineVariant = Color(0xFF303033),
    scrim = Color(0xFF000000),
    error = CrimsonError,
    onError = TextWhite
)

/**
 * 2. Theme sáng "Bright Peach": nền kem hồng, thẻ trắng, nhấn cam đào.
 */
val CalAILightColorScheme = lightColorScheme(
    primary = VividOrange,
    onPrimary = OnPrimary,
    primaryContainer = PeachSoft,
    onPrimaryContainer = Color(0xFF5A2421),
    inversePrimary = PeachOnDark,
    secondary = PastelLavenderLight,
    onSecondary = Color.White,
    secondaryContainer = PearlCard,
    onSecondaryContainer = InkLightSecondary,
    tertiary = PastelProteinLight,
    onTertiary = Color.White,
    background = IvoryBackground,
    onBackground = InkLightPrimary,
    surface = PearlSurface,
    onSurface = InkLightPrimary,
    surfaceVariant = PearlCard,
    onSurfaceVariant = InkLightSecondary,
    surfaceTint = Color.Transparent,
    surfaceDim = Color(0xFFFBEFEA),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF8F5),
    surfaceContainer = Color(0xFFFFF5F1),
    surfaceContainerHigh = PearlCard,
    surfaceContainerHighest = PearlCardElevated,
    inverseSurface = Color(0xFF302B2A),
    inverseOnSurface = Color(0xFFF5F0EE),
    outline = PearlBorder,
    outlineVariant = LightDivider,
    scrim = Color(0xFF000000),
    error = ErrorTextLight,
    onError = Color.White
)

@Composable
fun CalAITheme(
    darkTheme: Boolean = true, // Mặc định luôn là Dark Luxury theo Spec 9.3
    content: @Composable () -> Unit
) {
    if (NwTheme.isDark != darkTheme) NwTheme.isDark = darkTheme
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

