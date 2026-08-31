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
    primary = Color(0xFF2E7D32),            // Green #2E7D32 primary accent
    onPrimary = Color.White,
    primaryContainer = NavyContainerDark,
    onPrimaryContainer = NavyOnContainerDark,
    secondary = EmeraldSafe,
    onSecondary = SoftWhiteText,
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = EmeraldOnContainerDark,
    tertiary = CrimsonRisk,
    onTertiary = SoftWhiteText,
    error = CrimsonRisk,
    onError = Color.White,
    errorContainer = CrimsonContainerDark,
    onErrorContainer = CrimsonOnContainerDark,
    background = CharcoalCanvas,            // #121212 dark background
    onBackground = SoftWhiteText,
    surface = ElevatedCardDark,             // #1E1E1E dark card surface
    onSurface = SoftWhiteText,
    surfaceVariant = Color(0xFF1E1E1E),     // #1E1E1E
    onSurfaceVariant = Color(0xFF9E9E9E),
    surfaceTint = Color.Transparent,
    outline = SlateBorderDark,             // #2C2C2C
    outlineVariant = Color(0xFF2C2C2C),
    surfaceContainerHighest = Color(0xFF252525)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,                  // Navy #0A192F
    onPrimary = Color.White,
    primaryContainer = NavyContainerLight,
    onPrimaryContainer = NavyOnContainerLight,
    secondary = EmeraldSafe,                // Green #2E7D32
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = EmeraldOnContainerLight,
    tertiary = CrimsonRisk,                 // Red #D32F2F
    onTertiary = Color.White,
    error = CrimsonRisk,
    onError = Color.White,
    errorContainer = CrimsonContainerLight,
    onErrorContainer = CrimsonOnContainerLight,
    background = OffWhiteBackground,        // Cream #F9F9F7
    onBackground = DeepSlateText,
    surface = WhiteCard,                    // White
    onSurface = DeepSlateText,
    surfaceVariant = Color(0xFFF2F2EE),
    onSurfaceVariant = SubtextLight,
    outline = SlateBorderLight,             // #E2E4E0
    outlineVariant = Color(0xFFE2E4E0),
    surfaceContainerHighest = Color(0xFFF2F2EE)
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
