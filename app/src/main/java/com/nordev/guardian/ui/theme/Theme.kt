package com.nordev.guardian.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun GuardianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // The in-app theme switch can disagree with the system, and edge-to-edge
    // only picks its icon tint once, from the system setting.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = guardianTypography(),
        shapes = guardianShapes(),
    ) {
        CompositionLocalProvider(
            LocalGuardianStatusColors provides if (darkTheme) DarkStatusColors else LightStatusColors,
            LocalGuardianGradients provides if (darkTheme) DarkGradients else LightGradients,
        ) {
            content()
        }
    }
}
