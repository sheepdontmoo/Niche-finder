package com.nichefinder.docstash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * "Ink on paper" — a warm paper-white palette with a single stamped-red accent, evoking a
 * physical document rather than a camera gadget. Deliberately distinct from this repo's other
 * two apps: golf's "scorecard white" greens and OBD2's dark gauge cluster.
 */
object DocStash {
    val paper = Color(0xFFFBF6EC)      // page background — warm off-white, like paper stock
    val paperCard = Color(0xFFFFFFFF)  // cards/surfaces
    val paperEdge = Color(0xFFE8E0CF)  // borders/dividers
    val ink = Color(0xFF2B2620)        // primary text — warm near-black
    val inkDim = Color(0xFF716A5C)     // secondary text
    val stamp = Color(0xFFC1442B)      // primary accent — like a rubber ink stamp
    val unlocked = Color(0xFF3C7A5C)   // Pro-unlocked / success state
    val warn = Color(0xFFB23B3B)       // errors
}

private val colors = lightColorScheme(
    primary = DocStash.stamp,
    onPrimary = Color(0xFFFFFFFF),
    secondary = DocStash.unlocked,
    onSecondary = Color(0xFFFFFFFF),
    background = DocStash.paper,
    onBackground = DocStash.ink,
    surface = DocStash.paperCard,
    onSurface = DocStash.ink,
    surfaceVariant = DocStash.paperCard,
    onSurfaceVariant = DocStash.inkDim,
    outline = DocStash.paperEdge,
    error = DocStash.warn,
)

private val type = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.6.sp),
)

@Composable
fun DocStashTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = type, content = content)
}
