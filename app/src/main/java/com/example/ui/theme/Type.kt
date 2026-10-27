package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun foodEatsText(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = height.sp,
    letterSpacing = 0.sp
)

val Typography = Typography(
    headlineLarge = foodEatsText(32, 40, FontWeight.Bold),
    headlineMedium = foodEatsText(28, 36, FontWeight.Bold),
    headlineSmall = foodEatsText(24, 32, FontWeight.Bold),
    titleLarge = foodEatsText(22, 28, FontWeight.Bold),
    titleMedium = foodEatsText(17, 24, FontWeight.SemiBold),
    titleSmall = foodEatsText(15, 22, FontWeight.SemiBold),
    bodyLarge = foodEatsText(16, 24),
    bodyMedium = foodEatsText(14, 22),
    bodySmall = foodEatsText(12, 18),
    labelLarge = foodEatsText(14, 20, FontWeight.SemiBold),
    labelMedium = foodEatsText(12, 18, FontWeight.SemiBold),
    labelSmall = foodEatsText(11, 16, FontWeight.Medium)
)
