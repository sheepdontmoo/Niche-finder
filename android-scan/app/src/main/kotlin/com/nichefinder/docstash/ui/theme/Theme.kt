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
    // Explicit container roles matter: FloatingActionButton, NavigationBarItem's selected
    // indicator, and other M3 components default to *Container roles, which otherwise fall
    // back to Material3's baseline purple instead of a tint of our own brand colors.
    primaryContainer = Color(0xFFF3D9CD),
    onPrimaryContainer = Color(0xFF6B2113),
    secondary = DocStash.unlocked,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD3E9DE),
    onSecondaryContainer = Color(0xFF1F4531),
    background = DocStash.paper,
    onBackground = DocStash.ink,
    surface = DocStash.paperCard,
    onSurface = DocStash.ink,
    surfaceVariant = DocStash.paperCard,
    onSurfaceVariant = DocStash.inkDim,
    outline = DocStash.paperEdge,
    error = DocStash.warn,
    // Left unset, this defaults to Material3's baseline purple too: tonal elevation (the subtle
    // tint elevated surfaces used to get) blends `surface` with `surfaceTint`.
    surfaceTint = DocStash.stamp,
    // As of Material3 1.3.0 (bundled in compose-bom 2024.09.00+), Card and friends no longer use
    // `surface` + tonal elevation at all -- they default straight to the surfaceContainer* roles,
    // which (like every role above) fall back to baseline purple if left unset. Every populated
    // list of Cards was quietly purple-tinted until this was set; only visible by actually
    // running the app with real data, never from reading the color scheme in isolation.
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = DocStash.paperCard,
    surfaceContainer = DocStash.paperCard,
    surfaceContainerHigh = DocStash.paperCard,
    surfaceContainerHighest = DocStash.paperCard,
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
