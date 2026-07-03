package com.nichefinder.caddie.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nichefinder.caddie.R

/**
 * "Scorecard white" — a deliberate single light look, the way golf apps are
 * read on sunny fairways: near-white surfaces, near-black ink, fairway green
 * and one gold accent. Anton (OFL) carries every big number.
 *
 * Property names kept from the original dark palette so every screen retheme
 * is one file: pineDeep = page background, pine = cards, cream = primary ink,
 * creamDim = secondary ink.
 */
object Caddie {
    val pineDeep = Color(0xFFFAFAF6)     // page background — warm off-white
    val pine = Color(0xFFFFFFFF)         // cards
    val pineEdge = Color(0xFFE3E8E0)     // card borders / dividers
    val fairway = Color(0xFF17914F)      // the green — primary accent
    val fairwaySoft = Color(0xFF0E6B3C)  // caption green, darker for contrast on white
    val cream = Color(0xFF16211A)        // primary ink — near-black green
    val creamDim = Color(0xFF66736A)     // secondary ink
    val gold = Color(0xFFA87A1F)         // score/accent moments, darkened for white
    val under = Color(0xFFC93A3A)        // golf tradition: red = under par
    val over = Color(0xFF2F6BD8)

    val heroBrush = Brush.verticalGradient(listOf(Color(0xFFF1F6EF), pineDeep))
    val greenGlow = Brush.radialGradient(listOf(Color(0x2117914F), Color(0x0017914F)))
}

val Anton = FontFamily(Font(R.font.anton))

private val colors = lightColorScheme(
    primary = Caddie.fairway,
    onPrimary = Color(0xFFFFFFFF),
    secondary = Caddie.gold,
    background = Caddie.pineDeep,
    onBackground = Caddie.cream,
    surface = Caddie.pine,
    onSurface = Caddie.cream,
    surfaceVariant = Caddie.pine,
    onSurfaceVariant = Caddie.creamDim,
    outline = Caddie.pineEdge,
    error = Caddie.under,
)

private val type = Typography(
    displayLarge = TextStyle(fontFamily = Anton, fontSize = 128.sp, lineHeight = 120.sp, letterSpacing = 1.sp),
    displayMedium = TextStyle(fontFamily = Anton, fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = 0.5.sp),
    displaySmall = TextStyle(fontFamily = Anton, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = 0.5.sp),
    headlineMedium = TextStyle(fontFamily = Anton, fontSize = 24.sp, letterSpacing = 0.5.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.8.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.6.sp),
)

@Composable
fun CaddieTheme(content: @Composable () -> Unit) {
    isSystemInDarkTheme() // single committed look; hook kept for a future light variant
    MaterialTheme(colorScheme = colors, typography = type, content = content)
}
