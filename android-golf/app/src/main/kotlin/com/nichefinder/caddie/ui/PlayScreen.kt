package com.nichefinder.caddie.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nichefinder.caddie.GolfUiState
import com.nichefinder.caddie.ui.theme.Anton
import com.nichefinder.caddie.ui.theme.Caddie
import com.nichefinder.golf.geo.Geo

/** On-course screen: the number, the green, nothing else fighting for attention. */
@Composable
fun PlayScreen(
    state: GolfUiState,
    modifier: Modifier = Modifier,
    onSelectHole: (Int) -> Unit,
    onStrokes: (hole: Int, strokes: Int) -> Unit,
    onToggleMeasure: () -> Unit = {},
) {
    val course = state.course
    if (course == null) {
        EmptyState(modifier, "No round yet", "Pick a course — or start a demo round and watch this screen come alive.")
        return
    }
    val hole = course.hole(state.currentHole) ?: return

    Column(
        modifier = modifier.fillMaxSize().background(Caddie.heroBrush).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))

        // Hole strip
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(course.holes) { h ->
                val selected = h.number == state.currentHole
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (selected) Caddie.fairway else Caddie.pine)
                        .border(1.dp, if (selected) Caddie.fairway else Caddie.pineEdge, CircleShape)
                        .clickable { onSelectHole(h.number) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${h.number}",
                        fontFamily = Anton,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) Caddie.pineDeep else Caddie.creamDim,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "HOLE ${hole.number}   ·   PAR ${hole.par}" + if (state.demoMode) "   ·   DEMO" else "",
            style = MaterialTheme.typography.labelSmall,
            color = Caddie.creamDim,
        )

        val d = state.distances
        if (d == null) {
            Spacer(Modifier.height(80.dp))
            Text(
                if (state.gpsActive) "Locking onto satellites…" else "No position yet",
                style = MaterialTheme.typography.titleMedium, color = Caddie.creamDim,
            )
        } else {
            fun show(yards: Double): Int =
                (if (state.useMeters) yards * Geo.METERS_PER_YARD else yards).toInt()
            val unit = if (state.useMeters) "M" else "YDS"

            // The green itself, drawn from its real outline, approach-up
            state.position?.let { pos ->
                Spacer(Modifier.height(6.dp))
                GreenShape(
                    player = pos,
                    green = hole.green,
                    modifier = Modifier.fillMaxWidth(0.72f).height(170.dp),
                )
            }

            // THE number — animated so demo mode visibly ticks down
            val animated by animateFloatAsState(
                targetValue = show(d.middleYards).toFloat(),
                animationSpec = tween(600), label = "middle",
            )
            Text(
                "${animated.toInt()}",
                style = MaterialTheme.typography.displayLarge,
                color = Caddie.cream,
                modifier = Modifier.graphicsLayer { translationY = -8f },
            )
            Text("MIDDLE · $unit", style = MaterialTheme.typography.labelSmall, color = Caddie.fairwaySoft)

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.SpaceEvenly) {
                FlankNumber("FRONT", show(d.frontYards), gold = true)
                FlankNumber("BACK", show(d.backYards))
            }
        }

        Spacer(Modifier.height(14.dp))

        // Shot measure: mark the ball, walk, learn your club distances
        val measuring = state.markPosition != null
        Box(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (measuring) Caddie.gold.copy(alpha = 0.16f) else Caddie.pine)
                .border(1.dp, if (measuring) Caddie.gold else Caddie.pineEdge, RoundedCornerShape(999.dp))
                .clickable { onToggleMeasure() }
                .padding(horizontal = 18.dp, vertical = 8.dp),
        ) {
            Text(
                if (measuring)
                    "SHOT: ${state.measuredYards?.let { m -> (if (state.useMeters) m * Geo.METERS_PER_YARD else m).toInt() } ?: 0} ${if (state.useMeters) "M" else "YDS"} · TAP TO CLEAR"
                else "MARK BALL · MEASURE SHOT",
                style = MaterialTheme.typography.labelSmall,
                color = if (measuring) Caddie.gold else Caddie.creamDim,
            )
        }

        Spacer(Modifier.weight(1f))

        // Score stepper
        val current = state.strokesByHole[hole.number] ?: 0
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            StepButton("−") { if (current > 0) onStrokes(hole.number, current - 1) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (current == 0) "–" else "$current",
                    style = MaterialTheme.typography.displayMedium,
                    color = if (current == 0) Caddie.creamDim else Caddie.cream,
                )
                Text("STROKES", style = MaterialTheme.typography.labelSmall, color = Caddie.creamDim)
            }
            StepButton("+") { onStrokes(hole.number, current + 1) }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun FlankNumber(label: String, value: Int, gold: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$value",
            style = MaterialTheme.typography.displayMedium,
            color = if (gold) Caddie.gold else Caddie.cream.copy(alpha = 0.9f),
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = Caddie.creamDim)
    }
}

@Composable
private fun StepButton(glyph: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Caddie.pine)
            .border(1.dp, Caddie.pineEdge, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, style = MaterialTheme.typography.displaySmall, color = Caddie.fairway)
    }
}

@Composable
internal fun EmptyState(modifier: Modifier, title: String, body: String) {
    Column(
        modifier.fillMaxSize().background(Caddie.heroBrush).padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.displaySmall, color = Caddie.cream, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = Caddie.creamDim, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp).alpha(0f))
    }
}
