package com.parth.aquasense.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.parth.aquasense.R

// Set of Material typography styles to start with

// Define the Outfit Font Family
val OutfitFontFamily = FontFamily(
    Font(R.font.outfit)
)

private val defaultTypography = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = OutfitFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = OutfitFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = OutfitFontFamily),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = OutfitFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = OutfitFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = OutfitFontFamily),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = OutfitFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = OutfitFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = OutfitFontFamily),

    bodyLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = OutfitFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = OutfitFontFamily),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = OutfitFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = OutfitFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = OutfitFontFamily)
)