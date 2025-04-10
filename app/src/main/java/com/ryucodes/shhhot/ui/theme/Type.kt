package com.ryucodes.shhhot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Creating a custom font family for a more Apple-like experience
// Using system sans-serif with specific weights to mimic SF Pro
val AppleInspiredFont = FontFamily.SansSerif

// Set of Material typography styles with Apple-inspired adjustments
val Typography = Typography(
    // Titles
    titleLarge = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.4).sp // Negative tracking like Apple fonts
    ),
    titleMedium = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.3).sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp
    ),
    
    // Body text
    bodyLarge = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.15).sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.1).sp
    ),
    
    // Labels
    labelLarge = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.05).sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.05).sp
    ),
    
    // Headings
    headlineLarge = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppleInspiredFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.4).sp
    )
)