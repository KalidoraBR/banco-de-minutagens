package com.kalidorabr.bancodeminutagens.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun BancoDeMinutagensTheme(
    selectedTheme: AppThemeOpcao = AppThemeOpcao.VERMELHO,
    content: @Composable () -> Unit
) {
    val darkColorScheme = darkColorScheme(
        primary = selectedTheme.primaryColor,
        secondary = selectedTheme.primaryColor,
        tertiary = StatusGreen,
        background = DarkBackground,
        surface = SurfaceDark,
        onPrimary = TextPrimary,
        onSecondary = TextPrimary,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
        outline = BorderColor
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            if (activity != null) {
                try {
                    val insetsController = WindowCompat.getInsetsController(activity.window, view)
                    insetsController.isAppearanceLightStatusBars = false
                    insetsController.isAppearanceLightNavigationBars = false
                } catch (_: Exception) {
                    // Previne qualquer crash ao manipular a window
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme,
        typography = Typography,
        content = content
    )
}
