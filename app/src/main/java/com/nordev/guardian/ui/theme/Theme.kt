package com.nordev.guardian.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun GuardianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
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
