package com.nichefinder.caddie.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nichefinder.caddie.ui.theme.Caddie
import com.nichefinder.golf.geo.Geo
import com.nichefinder.golf.geo.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the actual shape of the green from its mapped outline, rotated so the
 * player's approach line points straight up — the way a caddie would hold a
 * yardage book in front of you. Front of the green (nearest point) is marked.
 *
 * This is the feature: not an icon of "a green", but *this* green.
 */
@Composable
fun GreenShape(
    player: LatLng,
    green: List<LatLng>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        if (green.size < 3) return@Canvas
        val c = Geo.centroid(green)

        // Local flat projection (meters), centered on the green
        val cosLat = cos(Math.toRadians(c.lat))
        fun toXY(p: LatLng) = Offset(
            x = ((p.lon - c.lon) * 111_320.0 * cosLat).toFloat(),
            y = ((p.lat - c.lat) * 110_540.0).toFloat(),
        )

        // Rotate so approach direction (player -> green center) points up
        val pv = toXY(player)
        val approach = atan2(-pv.x, -pv.y)  // angle of centre-from-player vector
        val cosA = cos(approach.toDouble()).toFloat()
        val sinA = sin(approach.toDouble()).toFloat()
        fun rotate(o: Offset) = Offset(
            x = o.x * cosA - o.y * sinA,
            y = o.x * sinA + o.y * cosA,
        )

        val pts = green.map { rotate(toXY(it)) }
        val minX = pts.minOf { it.x }; val maxX = pts.maxOf { it.x }
        val minY = pts.minOf { it.y }; val maxY = pts.maxOf { it.y }
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val pad = 0.18f
        val scale = minOf(size.width * (1 - 2 * pad) / spanX, size.height * (1 - 2 * pad) / spanY)

        fun toScreen(o: Offset) = Offset(
            x = size.width / 2 + (o.x - (minX + maxX) / 2) * scale,
            y = size.height / 2 - (o.y - (minY + maxY) / 2) * scale,   // flip: north = up
        )

        val screen = pts.map { toScreen(it) }
        val path = Path().apply {
            moveTo(screen.first().x, screen.first().y)
            screen.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }

        // Glow, fill, outline
        drawCircle(brush = Caddie.greenGlow, radius = size.minDimension * 0.62f,
            center = Offset(size.width / 2, size.height / 2))
        drawPath(path, color = Caddie.fairway.copy(alpha = 0.16f))
        drawPath(path, color = Caddie.fairway, style = Stroke(width = 2.5.dp.toPx()))

        // Front of green: the outline point nearest the player
        val frontIdx = green.indices.minBy { Geo.distanceMeters(player, green[it]) }
        drawCircle(color = Caddie.gold, radius = 5.dp.toPx(), center = screen[frontIdx])

        // Approach line from bottom edge to the front marker
        drawLine(
            color = Caddie.creamDim.copy(alpha = 0.6f),
            start = Offset(size.width / 2, size.height),
            end = screen[frontIdx],
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
        )
    }
}
