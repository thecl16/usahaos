package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Crisp, Modern Light Business SaaS Color Scheme
private val UsahaLightColorScheme = lightColorScheme(
    primary = UsahaBlue600,
    onPrimary = Color.White,
    primaryContainer = UsahaBlue50,
    onPrimaryContainer = UsahaBlue700,
    secondary = UsahaBlue700,
    onSecondary = Color.White,
    secondaryContainer = UsahaBlue100,
    onSecondaryContainer = UsahaBlue700,
    tertiary = UsahaEmerald600,
    onTertiary = Color.White,
    tertiaryContainer = UsahaEmerald100,
    onTertiaryContainer = UsahaEmerald700,
    background = UsahaSlate50,
    onBackground = UsahaNavy900,
    surface = Color.White,
    onSurface = UsahaNavy900,
    surfaceVariant = UsahaSlate100,
    onSurfaceVariant = UsahaSlate500,
    outline = UsahaSlate200,
    outlineVariant = UsahaSlate300,
    error = UsahaRed600,
    errorContainer = UsahaRed100,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Explicitly enforce light theme as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = UsahaLightColorScheme,
        typography = Typography,
        content = content
    )
}
