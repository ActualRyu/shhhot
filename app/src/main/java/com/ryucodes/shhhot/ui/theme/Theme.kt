package com.ryucodes.shhhot.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Apple-inspired color schemes
private val AppleLightColorScheme = lightColorScheme(
    primary = AppleBlue,
    onPrimary = AppleLightGray,
    primaryContainer = AppleLightGray,
    onPrimaryContainer = AppleBlue,
    
    secondary = AppleMediumGray,
    onSecondary = AppleLightGray,
    secondaryContainer = AppleGray,
    onSecondaryContainer = AppleMediumGray,
    
    tertiary = AppleBlue,
    onTertiary = AppleLightGray,
    
    background = AppleLightGray,
    onBackground = AppleDarkGray,
    
    surface = AppleLightGray,
    onSurface = AppleDarkGray,
    surfaceVariant = AppleGray,
    onSurfaceVariant = AppleMediumGray,
    
    error = Color(0xFFD30000),
    onError = AppleLightGray
)

private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleBlueDark,
    onPrimary = AppleTextDark,
    primaryContainer = AppleSurfaceDark,
    onPrimaryContainer = AppleBlueDark,
    
    secondary = AppleGrayMediumDark,
    onSecondary = AppleTextDark,
    secondaryContainer = AppleSurfaceDark,
    onSecondaryContainer = AppleGrayMediumDark,
    
    tertiary = AppleBlueDark,
    onTertiary = AppleTextDark,
    
    background = AppleGrayDark,
    onBackground = AppleTextDark,
    
    surface = AppleGrayDark,
    onSurface = AppleTextDark,
    surfaceVariant = AppleSurfaceDark,
    onSurfaceVariant = AppleGrayMediumDark,
    
    error = Color(0xFFFF453A),
    onError = AppleTextDark
)

@Composable
fun ShhhotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled by default to use our custom Apple-like colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> AppleDarkColorScheme
        else -> AppleLightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set transparent status bar and let content draw under it
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // Make status bar icons dark in light mode, light in dark mode
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}