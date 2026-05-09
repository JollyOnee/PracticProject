package org.infa252.project

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val lightColorScheme = lightColorScheme(
    primary = Color(0xFF006E1C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF41CB4F),
    onPrimaryContainer = Color(0xFF005012),
    secondary = Color(0xFF306A31),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB2F3AA),
    onSecondaryContainer = Color(0xFF367136),
    tertiary = Color(0xFF006685),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF00C0F7),
    onTertiaryContainer = Color(0xFF004A62),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF4FCED),
    onBackground = Color(0xFF161D15),
    surface = Color(0xFFF4FCED),
    onSurface = Color(0xFF161D15),
    surfaceVariant = Color(0xFFDDE5D7),
    onSurfaceVariant = Color(0xFF3E4A3B),
    outline = Color(0xFF6D7B69),
    outlineVariant = Color(0xFFBCCBB6),
    inverseSurface = Color(0xFF2B3229),
    inverseOnSurface = Color(0xFFEBF3E5),
    inversePrimary = Color(0xFF59E161),
    surfaceTint = Color(0xFF006E1C),
)

private val typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    )
)

private val shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
