package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.primitives.LGColorsDark
import com.example.ui.primitives.LGColorsLight
import com.example.ui.primitives.LocalLGColors
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════════
// My Legal Guardian — Material 3 Theme Configuration
// ═══════════════════════════════════════════════════════════════════════════════
// Wires the premium color palette and typography into M3 color schemes.
// The custom LGColors system is provided via CompositionLocal for components
// that need direct access to design tokens outside of MaterialTheme.

private val DarkColorScheme = darkColorScheme(
    primary = NavyPrimary,
    onPrimary = SoftWhiteText,
    primaryContainer = NavyContainerDark,
    onPrimaryContainer = NavyOnContainerDark,
    secondary = EmeraldSafe,
    onSecondary = SoftWhiteText,
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = EmeraldOnContainerDark,
    tertiary = CrimsonRisk,
    onTertiary = SoftWhiteText,
    error = CrimsonRisk,
    onError = SoftWhiteText,
    errorContainer = CrimsonContainerDark,
    onErrorContainer = CrimsonOnContainerDark,
    background = CharcoalCanvas,
    onBackground = SoftWhiteText,
    surface = ElevatedCardDark,
    onSurface = SoftWhiteText,
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = SubtextDark,
    surfaceTint = Color.Transparent,
    outline = SlateBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyContainerLight,
    onPrimaryContainer = NavyOnContainerLight,
    secondary = EmeraldSafe,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = EmeraldOnContainerLight,
    tertiary = CrimsonRisk,
    onTertiary = Color.White,
    error = CrimsonRisk,
    onError = Color.White,
    errorContainer = CrimsonContainerLight,
    onErrorContainer = CrimsonOnContainerLight,
    background = OffWhiteBackground,
    onBackground = DeepSlateText,
    surface = WhiteCard,
    onSurface = DeepSlateText,
    surfaceVariant = Color(0xFFEEF0F4),
    onSurfaceVariant = SubtextLight,
    outline = SlateBorderLight
)

@Composable
fun MyLegalGuardianTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colors = if (darkTheme) LGColorsDark else LGColorsLight

    CompositionLocalProvider(LocalLGColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
