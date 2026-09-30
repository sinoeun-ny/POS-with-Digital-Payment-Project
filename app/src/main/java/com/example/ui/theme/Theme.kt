package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val FoodEatsDarkColorScheme = darkColorScheme(
    primary = Forest400,
    onPrimary = Ink950,
    primaryContainer = Forest700,
    onPrimaryContainer = Forest100,
    secondary = Mint400,
    onSecondary = Ink950,
    background = Ink950,
    onBackground = Cream50,
    surface = Ink900,
    onSurface = Cream50,
    surfaceVariant = Ink800,
    onSurfaceVariant = Ink200,
    outline = Forest700
)

private val FoodEatsLightColorScheme = lightColorScheme(
    primary = Forest500,
    onPrimary = Color.White,
    primaryContainer = Forest100,
    onPrimaryContainer = Forest800,
    secondary = Mint500,
    onSecondary = Color.White,
    secondaryContainer = Mint100,
    onSecondaryContainer = Ink950,
    background = Cream50,
    onBackground = Ink950,
    surface = Color.White,
    onSurface = Ink900,
    surfaceVariant = Cream100,
    onSurfaceVariant = Ink700,
    outline = Sage200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default false to preserve FoodEats signature emerald & cream colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FoodEatsDarkColorScheme
        else -> FoodEatsLightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
