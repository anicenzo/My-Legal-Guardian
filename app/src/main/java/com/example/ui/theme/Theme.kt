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
// My Legal Guardian — Material 3 Theme Configuration (Navy & White Rebrand)
// ═══════════════════════════════════════════════════════════════════════════════
// Wires the premium Navy & White palette into M3 color schemes.
// The custom LGColors system is provided via CompositionLocal for components
// that need direct access to design tokens outside of MaterialTheme.

private val DarkColorScheme = darkColorScheme(
    primary = ScannerBlue,                   // #0066FF primary accent
    onPrimary = Color.White,
    primaryContainer = NavyContainerDark,
    onPrimaryContainer = NavyOnContainerDark,
    secondary = EmeraldSafe,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = EmeraldOnContainerDark,
    tertiary = CrimsonRisk,
    onTertiary = Color.White,
    error = CrimsonRisk,
    onError = Color.White,
    errorContainer = CrimsonContainerDark,
    onErrorContainer = CrimsonOnContainerDark,
    background = NavyCanvas,                  // Deep Navy #0A142F
    onBackground = SoftWhiteText,
    surface = ElevatedCardDark,               // Elevated Navy #11224D
    onSurface = SoftWhiteText,
    surfaceVariant = ElevatedCardDark,
    onSurfaceVariant = SubtextDark,
    surfaceTint = Color.Transparent,
    outline = SlateBorderDark,
    outlineVariant = SlateBorderDark,
    surfaceContainerHighest = Color(0xFF162B50)
)

private val LightColorScheme = lightColorScheme(
    primary = ScannerBlue,                    // #0066FF primary accent
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
    background = PureWhiteBackground,         // Pure White #FFFFFF
    onBackground = DeepSlateText,
    surface = WhiteCard,                      // Pure White
    onSurface = DeepSlateText,
    surfaceVariant = Color(0xFFF7FAFC),
    onSurfaceVariant = SubtextLight,
    outline = SlateBorderLight,
    outlineVariant = SlateBorderLight,
    surfaceContainerHighest = Color(0xFFF7FAFC)
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
