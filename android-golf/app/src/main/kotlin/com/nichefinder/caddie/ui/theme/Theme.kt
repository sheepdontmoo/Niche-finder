package com.nichefinder.caddie.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
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
 * "Clubhouse at dusk" — a deliberate single look. Deep pine greens, cream
 * text, one gold accent. Anton (OFL) carries every big number; it stays
 * readable at arm's length in sunlight, which is the whole job.
 */
object Caddie {
    val pineDeep = Color(0xFF07130C)     // page background
    val pine = Color(0xFF0D2115)         // cards
    val pineEdge = Color(0xFF1B3A28)     // card borders / dividers
    val fairway = Color(0xFF35D07F)      // the green — primary accent
    val fairwaySoft = Color(0xFF9BE7BF)
    val cream = Color(0xFFF4F1E6)        // primary text
    val creamDim = Color(0xFFA9B3A6)     // secondary text
    val gold = Color(0xFFE8C468)         // score/accent moments
    val under = Color(0xFFFF6B6B)        // golf tradition: red = under par
    val over = Color(0xFF7EA8FF)

    val heroBrush = Brush.verticalGradient(listOf(Color(0xFF0F2B1B), pineDeep))
    val greenGlow = Brush.radialGradient(listOf(Color(0x3335D07F), Color(0x0035D07F)))
}

val Anton = FontFamily(Font(R.font.anton))

private val colors = darkColorScheme(
    primary = Caddie.fairway,
    onPrimary = Color(0xFF03130A),
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
