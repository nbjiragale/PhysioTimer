package com.niranjan.physiotimer.ui

import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object WellnessColors {
    val Sage50 = Color(0xFFF4F8F1)
    val Sage75 = Color(0xFFEEF5EA)
    val Sage100 = Color(0xFFE4EEDD)
    val Sage200 = Color(0xFFC9DCC1)
    val Sage300 = Color(0xFFA8C09C)
    val Sage500 = Color(0xFF6F8F64)
    val Sage600 = Color(0xFF58754F)
    val Sage700 = Color(0xFF3F5C39)

    val Beige50 = Color(0xFFFFFBF5)
    val Beige100 = Color(0xFFF8EFE2)
    val Beige200 = Color(0xFFEBDCC7)
    val Beige300 = Color(0xFFD9C5AA)

    val Lavender50 = Color(0xFFF8F4FA)
    val Lavender100 = Color(0xFFEDE3F1)
    val Lavender200 = Color(0xFFD9C7E1)
    val Lavender400 = Color(0xFF9C7EAD)
    val Lavender600 = Color(0xFF725684)

    val Clay50 = Color(0xFFFFF4EF)
    val Clay100 = Color(0xFFF7D9CB)
    val Clay400 = Color(0xFFD98B6E)
    val Clay600 = Color(0xFFB95F45)

    val SkyMist50 = Color(0xFFF1F7FA)
    val SkyMist200 = Color(0xFFC7DDE7)
    val SkyMist500 = Color(0xFF6F9CAD)

    val Ink900 = Color(0xFF1F2A24)
    val Ink700 = Color(0xFF435047)
    val Ink500 = Color(0xFF6E7A71)
    val Ink300 = Color(0xFFA5AEA7)

    val White = Color(0xFFFFFFFF)
    val Scrim = Color(0x660F1F18)
}

internal val WellnessLightColorScheme = lightColorScheme(
    primary = WellnessColors.Sage600,
    onPrimary = WellnessColors.White,
    primaryContainer = WellnessColors.Sage100,
    onPrimaryContainer = WellnessColors.Sage700,

    secondary = WellnessColors.Lavender600,
    onSecondary = WellnessColors.White,
    secondaryContainer = WellnessColors.Lavender100,
    onSecondaryContainer = WellnessColors.Lavender600,

    tertiary = WellnessColors.Clay600,
    onTertiary = WellnessColors.White,
    tertiaryContainer = WellnessColors.Clay100,
    onTertiaryContainer = WellnessColors.Clay600,

    background = WellnessColors.Beige50,
    onBackground = WellnessColors.Ink900,
    surface = WellnessColors.Beige50,
    onSurface = WellnessColors.Ink900,
    surfaceVariant = WellnessColors.Sage75,
    onSurfaceVariant = WellnessColors.Ink700,

    outline = Color(0xFFD7D0C3),
    outlineVariant = Color(0xFFE8DFD2),

    error = Color(0xFFBA4E42),
    onError = WellnessColors.White,
    errorContainer = Color(0xFFFFE2DC),
    onErrorContainer = Color(0xFF7B241C)
)

private val WellnessTimerFont = FontFamily.Monospace
private val WellnessEditorialFont = FontFamily.Serif
private val WellnessBodyFont = FontFamily.SansSerif

internal val WellnessTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = WellnessTimerFont,
        fontSize = 80.sp,
        lineHeight = 84.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-1.2).sp
    ),
    displayMedium = TextStyle(
        fontFamily = WellnessTimerFont,
        fontSize = 58.sp,
        lineHeight = 62.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.8).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = WellnessEditorialFont,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = TextStyle(
        fontFamily = WellnessEditorialFont,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = TextStyle(
        fontFamily = WellnessEditorialFont,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontFamily = WellnessEditorialFont,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontFamily = WellnessBodyFont,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontFamily = WellnessBodyFont,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontFamily = WellnessBodyFont,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = WellnessTimerFont,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = WellnessBodyFont,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        fontWeight = FontWeight.Medium
    )
)

object WellnessRadius {
    val Tiny = 8.dp
    val Small = 12.dp
    val Medium = 18.dp
    val Large = 24.dp
    val XLarge = 32.dp
    val Pill = 999.dp
}

object WellnessSpacing {
    val Xxs = 4.dp
    val Xs = 8.dp
    val Sm = 12.dp
    val Md = 16.dp
    val Lg = 20.dp
    val Xl = 24.dp
    val Xxl = 32.dp
    val Xxxl = 40.dp
}

object WellnessSurfaces {
    val Screen = WellnessColors.Beige50
    val LayerSoft = WellnessColors.Sage50
    val LayerWarm = WellnessColors.Beige100
    val LayerLavender = WellnessColors.Lavender50
    val Card = Color(0xFFFFFEFB)
    val CardMuted = Color(0xFFF7F1E8)
}

internal val WellnessShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(WellnessRadius.Tiny),
    small = androidx.compose.foundation.shape.RoundedCornerShape(WellnessRadius.Small),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(WellnessRadius.Medium),
    large = androidx.compose.foundation.shape.RoundedCornerShape(WellnessRadius.Large),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(WellnessRadius.XLarge)
)

data class StepAccent(
    val name: String,
    val primary: Color,
    val surface: Color
)

val StepAccentPalette = listOf(
    StepAccent("Sage", Color(0xFF6F8F64), Color(0xFFEAF2E5)),
    StepAccent("Lavender", Color(0xFF8E72A0), Color(0xFFF1EAF5)),
    StepAccent("Clay", Color(0xFFC47A5B), Color(0xFFFFEEE7)),
    StepAccent("Sky Mist", Color(0xFF6F9CAD), Color(0xFFEAF4F8)),
    StepAccent("Olive", Color(0xFF8A8F55), Color(0xFFF1F2E2)),
    StepAccent("Warm Sand", Color(0xFFA8875F), Color(0xFFF6EDDF))
)

internal val SageLavenderGradient = Brush.linearGradient(
    colors = listOf(WellnessColors.Sage600, WellnessColors.Lavender600)
)

internal val ClayGradient = Brush.linearGradient(
    colors = listOf(WellnessColors.Clay400, WellnessColors.Clay600)
)

internal val ProgressGradient = Brush.sweepGradient(
    listOf(WellnessColors.Sage500, WellnessColors.Lavender400, WellnessColors.Sage500)
)

internal val BgDeep = WellnessSurfaces.Screen
internal val BgCard = WellnessSurfaces.Card
internal val BgElevated = WellnessColors.Sage75
internal val BgMid = WellnessColors.Sage200

internal val TextPrimary = WellnessColors.Ink900
internal val TextSecondary = WellnessColors.Ink700
internal val TextDim = WellnessColors.Ink500

internal val Teal = WellnessColors.Sage600
internal val TealDark = WellnessColors.Sage700
internal val Coral = WellnessColors.Clay600
internal val Amber = WellnessColors.Clay400
internal val Mint = WellnessColors.Sage500
internal val Sky = WellnessColors.SkyMist500

internal val BorderLine = WellnessLightColorScheme.outlineVariant
internal val BorderFocus = WellnessLightColorScheme.primary

internal val TealGradient = SageLavenderGradient
internal val CoralGradient = ClayGradient
internal val CardGradient = Brush.linearGradient(
    listOf(WellnessSurfaces.Card, WellnessSurfaces.LayerWarm)
)
