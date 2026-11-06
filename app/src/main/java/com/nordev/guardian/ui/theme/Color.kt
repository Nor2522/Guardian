package com.nordev.guardian.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Fixed schemes from docs/design.md. Dynamic color is off in version 1.
val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF9EF2E4),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A635E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE8E1),
    onSecondaryContainer = Color(0xFF06201B),
    tertiary = Color(0xFF5D5B7D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE3DFFF),
    onTertiaryContainer = Color(0xFF1A1738),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF4FBF8),
    onBackground = Color(0xFF161D1B),
    surface = Color(0xFFF4FBF8),
    onSurface = Color(0xFF161D1B),
    surfaceVariant = Color(0xFFDAE5E1),
    onSurfaceVariant = Color(0xFF3F4946),
    outline = Color(0xFF6F7976),
    outlineVariant = Color(0xFFBFC9C5),
    surfaceDim = Color(0xFFD4DCD8),
    surfaceBright = Color(0xFFF4FBF8),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEEF6F2),
    surfaceContainer = Color(0xFFE8F0EC),
    surfaceContainerHigh = Color(0xFFE2EAE6),
    surfaceContainerHighest = Color(0xFFDCE4E0),
    inverseSurface = Color(0xFF2B322F),
    inverseOnSurface = Color(0xFFECF2EF),
    inversePrimary = Color(0xFF82D5C8),
    scrim = Color(0xFF000000),
)

val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF82D5C8),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF9EF2E4),
    secondary = Color(0xFFB1CCC5),
    onSecondary = Color(0xFF1C3530),
    secondaryContainer = Color(0xFF334B46),
    onSecondaryContainer = Color(0xFFCDE8E1),
    tertiary = Color(0xFFC6C1EA),
    onTertiary = Color(0xFF2F2C4D),
    tertiaryContainer = Color(0xFF454263),
    onTertiaryContainer = Color(0xFFE3DFFF),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF0E1513),
    onBackground = Color(0xFFDDE4E1),
    surface = Color(0xFF0E1513),
    onSurface = Color(0xFFDDE4E1),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBEC9C5),
    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF3F4946),
    surfaceDim = Color(0xFF0E1513),
    surfaceBright = Color(0xFF343B39),
    surfaceContainerLowest = Color(0xFF090F0E),
    surfaceContainerLow = Color(0xFF1A211F),
    surfaceContainer = Color(0xFF1E2523),
    surfaceContainerHigh = Color(0xFF232A28),
    surfaceContainerHighest = Color(0xFF2D3432),
    inverseSurface = Color(0xFFDDE4E1),
    inverseOnSurface = Color(0xFF2B322F),
    inversePrimary = Color(0xFF006A60),
    scrim = Color(0xFF000000),
)

@Immutable
data class GuardianStatusColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

val LightStatusColors = GuardianStatusColors(
    success = Color(0xFF1E6B3A),
    successContainer = Color(0xFFA5F3B8),
    onSuccessContainer = Color(0xFF00210F),
    warning = Color(0xFF745B00),
    warningContainer = Color(0xFFFFE08A),
    onWarningContainer = Color(0xFF241A00),
)

val DarkStatusColors = GuardianStatusColors(
    success = Color(0xFF8ED6A0),
    successContainer = Color(0xFF005228),
    onSuccessContainer = Color(0xFFA5F3B8),
    warning = Color(0xFFE8C34A),
    warningContainer = Color(0xFF574400),
    onWarningContainer = Color(0xFFFFE08A),
)

val LocalGuardianStatusColors = staticCompositionLocalOf<GuardianStatusColors> {
    error("GuardianStatusColors not provided")
}
