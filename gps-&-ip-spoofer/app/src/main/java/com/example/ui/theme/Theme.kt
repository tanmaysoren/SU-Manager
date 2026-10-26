package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

private val DarkColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = CyberOnPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    surface = CyberSurface,
    surfaceVariant = CyberSurfaceVariant,
    onBackground = CyberOnSurface,
    onSurface = CyberOnSurface,
    onSurfaceVariant = CyberOnSurfaceVariant,
    outline = CyberOutline,
    error = CyberError
)

private val LightColorScheme = lightColorScheme(
    primary = CyberPrimary,
    onPrimary = CyberOnPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    surface = CyberSurface,
    surfaceVariant = CyberSurfaceVariant,
    onBackground = CyberOnSurface,
    onSurface = CyberOnSurface,
    onSurfaceVariant = CyberOnSurfaceVariant,
    outline = CyberOutline,
    error = CyberError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val responsiveScale = rememberResponsiveScale()
    val scaledTypography = remember(responsiveScale) {
        getResponsiveTypography(responsiveScale)
    }

    val colorScheme = DarkColorScheme

    CompositionLocalProvider(
        LocalResponsiveScale provides responsiveScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            content = content
        )
    }
}


