package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAmoledMode = compositionLocalOf { false }

private val TilawaDarkColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color.White,
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = GoldLight,
    background = DarkCanvas,
    onBackground = Color(0xFFE6EDE9),
    surface = DarkSurface,
    onSurface = Color(0xFFE6EDE9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF9EABA4),
    outline = DarkBorder,
    error = MistakeRed,
    onError = Color.White
)

private val TilawaAmoledColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color.White,
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = GoldLight,
    background = AmoledBlack,
    onBackground = Color(0xFFF0F5F2),
    surface = AmoledSurface,
    onSurface = Color(0xFFF0F5F2),
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = Color(0xFF8E9E97),
    outline = AmoledBorder,
    error = MistakeRed,
    onError = Color.White
)

private val TilawaLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = EmeraldDark,
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    background = LightCanvas,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = MistakeRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoledTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        amoledTheme -> TilawaAmoledColorScheme
        darkTheme -> TilawaDarkColorScheme
        else -> TilawaLightColorScheme
    }

    CompositionLocalProvider(LocalAmoledMode provides amoledTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
