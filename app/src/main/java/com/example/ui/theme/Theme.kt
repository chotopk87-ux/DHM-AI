package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisLightColorScheme = lightColorScheme(
    primary = JarvisViolet,
    onPrimary = Color.White,
    primaryContainer = JarvisVioletContainer,
    onPrimaryContainer = JarvisViolet,
    secondary = JarvisCoral,
    onSecondary = Color.White,
    secondaryContainer = JarvisCoralContainer,
    onSecondaryContainer = JarvisCoral,
    tertiary = JarvisEmerald,
    onTertiary = Color.White,
    tertiaryContainer = JarvisEmeraldContainer,
    onTertiaryContainer = JarvisEmerald,
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorderCyan,
    error = JarvisRedAlert
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Keep signature for template compatibility
    content: @Composable () -> Unit
) {
    // JARVIS AI is crafted with a vibrant White & Multi-Colour theme
    MaterialTheme(
        colorScheme = JarvisLightColorScheme,
        typography = Typography,
        content = content
    )
}
