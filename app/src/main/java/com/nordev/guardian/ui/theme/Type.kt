package com.nordev.guardian.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tabular figures for durations and times per docs/design.md.
const val TNUM = "tnum"

fun guardianTypography(base: Typography = Typography()): Typography = Typography(
    displaySmall = base.displaySmall.copy(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Normal,
        fontFeatureSettings = TNUM,
    ),
    headlineLarge = base.headlineLarge.copy(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Normal,
    ),
    headlineMedium = base.headlineMedium.copy(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Normal,
    ),
    headlineSmall = base.headlineSmall.copy(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Normal,
    ),
    titleLarge = base.titleLarge.copy(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Medium,
    ),
    titleMedium = base.titleMedium.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = base.bodyLarge.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        fontFeatureSettings = TNUM,
    ),
    bodyMedium = base.bodyMedium.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = base.labelLarge.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelMedium = base.labelMedium.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFeatureSettings = TNUM,
    ),
)
