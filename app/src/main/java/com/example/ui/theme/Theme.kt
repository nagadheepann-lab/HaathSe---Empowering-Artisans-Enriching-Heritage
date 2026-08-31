package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Scheme (Warm Indian Craftsman Aesthetic)
private val HaathSeLightColorScheme = lightColorScheme(
    primary = HaathSeTerracotta,
    onPrimary = Color.White,
    primaryContainer = HaathSeTerracottaLight,
    onPrimaryContainer = HaathSeTerracottaDark,

    secondary = HaathSeSaffron,
    onSecondary = Color.White,
    secondaryContainer = HaathSeSaffronLight,
    onSecondaryContainer = Color(0xFF662200),

    tertiary = HaathSePeacockTeal,
    onTertiary = Color.White,
    tertiaryContainer = HaathSePeacockLight,
    onTertiaryContainer = Color(0xFF003834),

    background = HaathSeCreamBg,
    onBackground = HaathSeTextPrimary,

    surface = HaathSeWhiteSurface,
    onSurface = HaathSeTextPrimary,
    surfaceVariant = HaathSeSand,
    onSurfaceVariant = HaathSeTextSecondary,

    outline = HaathSeCardBorder,
    outlineVariant = Color(0xFFE2D6C7)
)

// Dark Scheme (Charcoal Woodblock Night Aesthetic)
private val HaathSeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF27B5A),
    onPrimary = Color(0xFF4A1406),
    primaryContainer = Color(0xFF5A1C0E),
    onPrimaryContainer = Color(0xFFFFDBCF),

    secondary = HaathSeGoldBright,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF5F3B00),
    onSecondaryContainer = Color(0xFFFFE088),

    tertiary = Color(0xFF5EEAD4),
    onTertiary = Color(0xFF003735),
    tertiaryContainer = Color(0xFF133936),
    onTertiaryContainer = Color(0xFF70F3E0),

    background = HaathSeDarkBg,
    onBackground = HaathSeDarkTextPrimary,

    surface = HaathSeDarkCard,
    onSurface = HaathSeDarkTextPrimary,
    surfaceVariant = HaathSeDarkElevated,
    onSurfaceVariant = HaathSeDarkTextSecondary,

    outline = HaathSeDarkBorder,
    outlineVariant = Color(0xFF4D433C)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to vibrant warm craft light theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) HaathSeDarkColorScheme else HaathSeLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = HaathSeShapes,
        content = content
    )
}
