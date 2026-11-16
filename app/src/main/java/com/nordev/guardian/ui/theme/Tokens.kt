package com.nordev.guardian.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Additive layer on top of docs/design.md. The original values are unchanged;
// these tokens give the screens a shared rhythm and a small set of brand
// gradients so spacing, shape, and color stay consistent as the app grows.
object GuardianSpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
    val gutter: Dp = 16.dp
    val cardPadding: Dp = 16.dp
    val sectionGap: Dp = 24.dp
    val hairline: Dp = 1.dp
    val maxContentWidth: Dp = 640.dp
}

object GuardianMotion {
    const val quickMillis: Int = 150
    const val standardMillis: Int = 200
    const val slowMillis: Int = 300
    val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}

object GuardianSizes {
    val minTouch: Dp = 48.dp
    val primaryButtonHeight: Dp = 52.dp
    val iconBadge: Dp = 44.dp
    val iconBadgeLarge: Dp = 64.dp
    val progressHeight: Dp = 6.dp
}

// Gradients are tonal, never glossy. They reuse the fixed schemes so the brand
// still looks the same on every phone and in both themes.
@Immutable
data class GuardianGradients(
    val heroSetup: Brush,
    val heroHealthy: Brush,
    val heroAttention: Brush,
    val heroOff: Brush,
    val primaryButton: Brush,
    val barStrong: Brush,
    val barMuted: Brush,
    val accentLine: Brush,
    val ambientTop: Color,
    val ambientBottom: Color,
    val cardBorder: Color,
)

private fun guardianGradients(
    colors: ColorScheme,
    status: GuardianStatusColors,
    dark: Boolean,
): GuardianGradients {
    val lightSheen = if (dark) colors.surfaceContainerHigh else colors.surfaceContainerLowest
    return GuardianGradients(
        heroSetup = Brush.linearGradient(
            listOf(
                colors.primaryContainer.copy(alpha = if (dark) 0.55f else 0.95f),
                lightSheen,
            ),
        ),
        heroHealthy = Brush.linearGradient(
            listOf(
                status.successContainer.copy(alpha = if (dark) 0.55f else 0.95f),
                lightSheen,
            ),
        ),
        heroAttention = Brush.linearGradient(
            listOf(
                status.warningContainer.copy(alpha = if (dark) 0.50f else 0.95f),
                lightSheen,
            ),
        ),
        heroOff = Brush.linearGradient(
            listOf(
                colors.surfaceContainerHigh,
                lightSheen,
            ),
        ),
        primaryButton = Brush.verticalGradient(
            listOf(
                lerp(colors.primary, Color.White, if (dark) 0f else 0.08f),
                lerp(colors.primary, colors.secondary, 0.45f),
            ),
        ),
        barStrong = Brush.verticalGradient(
            listOf(
                lerp(colors.primary, colors.primaryContainer, 0.55f),
                colors.primary,
            ),
        ),
        barMuted = Brush.verticalGradient(
            listOf(
                colors.primaryContainer.copy(alpha = 0.85f),
                colors.primaryContainer.copy(alpha = 0.55f),
            ),
        ),
        accentLine = Brush.horizontalGradient(
            listOf(colors.primary, colors.tertiary),
        ),
        ambientTop = colors.primary.copy(alpha = if (dark) 0.16f else 0.10f),
        ambientBottom = colors.tertiary.copy(alpha = if (dark) 0.10f else 0.05f),
        cardBorder = colors.outlineVariant.copy(alpha = if (dark) 0.28f else 0.38f),
    )
}

val LightGradients: GuardianGradients = guardianGradients(LightColors, LightStatusColors, dark = false)

val DarkGradients: GuardianGradients = guardianGradients(DarkColors, DarkStatusColors, dark = true)

val LocalGuardianGradients = staticCompositionLocalOf<GuardianGradients> {
    error("GuardianGradients not provided")
}
