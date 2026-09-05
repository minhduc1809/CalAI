package com.calai.app.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CalAIDarkColorScheme = darkColorScheme(
    primary = VividOrange,
    onPrimary = TextWhite,
    primaryContainer = VividOrangeDark,
    onPrimaryContainer = TextWhite,
    secondary = PastelLavender,
    onSecondary = TextDeepInk,
    secondaryContainer = CharcoalCard,
    onSecondaryContainer = TextLightGrey,
    tertiary = PastelMint,
    onTertiary = TextDeepInk,
    background = ObsidianBackground,
    onBackground = TextWhite,
    surface = CharcoalSurface,
    onSurface = TextWhite,
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = TextMuted,
    outline = CharcoalBorder,
    error = CrimsonError,
    onError = TextWhite
)

@Composable
fun CalAITheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CalAIDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ObsidianBackground.toArgb()
            window.navigationBarColor = ObsidianBackground.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
