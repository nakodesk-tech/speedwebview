package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CleanMinimalLightColorScheme = lightColorScheme(
    primary = MinimalBlue,
    onPrimary = Color.White,
    primaryContainer = MinimalBlueContainer,
    onPrimaryContainer = MinimalOnBlueContainer,
    secondary = MinimalBlueDark,
    onSecondary = Color.White,
    background = MinimalBackground,
    onBackground = MinimalTextPrimary,
    surface = MinimalSurface,
    onSurface = MinimalTextPrimary,
    surfaceVariant = MinimalSurfaceVariant,
    onSurfaceVariant = MinimalTextSecondary,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerLow = MinimalSurfaceLow,
    outline = MinimalBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

private val CleanMinimalDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    background = Color(0xFF111418),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF2E3238),
    onSurfaceVariant = Color(0xFFC4C7D0),
    surfaceContainer = Color(0xFF1E2124),
    surfaceContainerHigh = Color(0xFF282B30),
    surfaceContainerLow = Color(0xFF16181B),
    outline = Color(0xFF42474E),
    outlineVariant = Color(0xFF44474E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) CleanMinimalDarkColorScheme else CleanMinimalLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
