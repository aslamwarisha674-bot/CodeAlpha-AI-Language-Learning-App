package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDark,
    secondary = PurpleSecondaryDark,
    tertiary = PurpleTertiaryDark,
    background = PurpleBackgroundDark,
    surface = PurpleSurfaceDark,
    surfaceVariant = PurpleSurfaceVariantDark,
    outline = PurpleOutlineDark,
    onPrimary = PurpleBackgroundDark,
    onSecondary = PurpleBackgroundDark,
    onTertiary = PurpleBackgroundDark,
    onBackground = PurpleOnBackgroundDark,
    onSurface = PurpleOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimaryLight,
    secondary = PurpleSecondaryLight,
    tertiary = PurpleTertiaryLight,
    background = PurpleBackgroundLight,
    surface = PurpleSurfaceLight,
    surfaceVariant = PurpleSurfaceVariantLight,
    outline = PurpleOutlineLight,
    onPrimary = PurpleSurfaceLight,
    onSecondary = PurpleSurfaceLight,
    onTertiary = PurpleSurfaceLight,
    onBackground = PurpleOnBackgroundLight,
    onSurface = PurpleOnSurfaceLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Set false to prioritize our signature Teal-Sky-Amber palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
