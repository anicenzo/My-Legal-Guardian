package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.primitives.LGColorsDark
import com.example.ui.primitives.LocalLGColors

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Dark-Only Theme (no branching, no isSystemInDarkTheme)
// ═══════════════════════════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary              = CobaltAccent,
    onPrimary            = DarkOnBackground,
    primaryContainer     = CobaltContainer,
    onPrimaryContainer   = DarkOnBackground,
    secondary            = RiskLowColor,
    onSecondary          = DarkOnBackground,
    tertiary             = RiskMediumColor,
    onTertiary           = DarkOnBackground,
    error                = RiskHighColor,
    onError              = DarkOnBackground,
    errorContainer       = RiskHighContainer,
    onErrorContainer     = RiskHighColor,
    background           = DarkBackground,
    onBackground         = DarkOnBackground,
    surface              = DarkSurface,
    onSurface            = DarkOnSurface,
    surfaceVariant       = DarkSurfaceVariant,
    onSurfaceVariant     = DarkOnSurfaceVar,
    surfaceTint          = CobaltAccent,
    outline              = DarkOutline,
    outlineVariant       = DarkOutline
)

// Clean Modern Sans-Serif M3 typography to match LGType
private val LGMaterialTypography = Typography(
    displayLarge   = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,     fontSize = 30.sp, lineHeight = 36.sp),
    displayMedium  = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    displaySmall   = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    headlineLarge  = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 18.sp, lineHeight = 24.sp),
    headlineSmall  = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 22.sp),
    titleLarge     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleMedium    = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    titleSmall     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge      = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,   fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall      = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,   fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 13.sp, lineHeight = 18.sp),
    labelMedium    = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 15.sp)
)

// Dark-only. No darkTheme parameter. No isSystemInDarkTheme() check.
@Composable
fun MyLegalGuardianTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLGColors provides LGColorsDark) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography  = LGMaterialTypography,
            content     = content
        )
    }
}
