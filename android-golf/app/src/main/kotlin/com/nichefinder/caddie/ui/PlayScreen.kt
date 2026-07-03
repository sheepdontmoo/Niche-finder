package com.nichefinder.caddie.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nichefinder.caddie.GolfUiState
import com.nichefinder.golf.geo.Geo

/**
 * The on-course screen: three big numbers. Front / MIDDLE / back,
 * exactly what a golfer glances at before pulling a club.
 */
@Composable
fun PlayScreen(
    state: GolfUiState,
    modifier: Modifier = Modifier,
    onSelectHole: (Int) -> Unit,
    onStrokes: (hole: Int, strokes: Int) -> Unit,
) {
    val course = state.course
    if (course == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pick a course first (or start a demo round)", textAlign = TextAlign.Center)
        }
        return
    }
    val hole = course.hole(state.currentHole) ?: return

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hole selector
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(course.holes) { h ->
                FilterChip(
                    selected = h.number == state.currentHole,
                    onClick = { onSelectHole(h.number) },
                    label = { Text("${h.number}") },
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Hole ${hole.number} · Par ${hole.par}", style = MaterialTheme.typography.titleLarge)
        if (state.demoMode) {
            Text("Demo round — you're walking down the fairway", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))

        val d = state.distances
        if (d == null) {
            Text(
                if (state.gpsActive) "Waiting for GPS…" else "No position yet",
                style = MaterialTheme.typography.titleMedium,
            )
        } else {
            fun fmt(yards: Double): String {
                val v = if (state.useMeters) yards * Geo.METERS_PER_YARD else yards
                return v.toInt().toString()
            }
            val unit = if (state.useMeters) "m" else "yds"

            // THE number — middle of the green, huge.
            Text(fmt(d.middleYards), fontSize = 112.sp, lineHeight = 112.sp,
                color = MaterialTheme.colorScheme.primary)
            Text("middle · $unit", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Card {
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(fmt(d.frontYards), style = MaterialTheme.typography.headlineMedium)
                        Text("front", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Card {
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(fmt(d.backYards), style = MaterialTheme.typography.headlineMedium)
                        Text("back", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Quick score entry for this hole
        val current = state.strokesByHole[hole.number] ?: 0
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { if (current > 0) onStrokes(hole.number, current - 1) }) { Text("−") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (current == 0) "—" else "$current", style = MaterialTheme.typography.headlineMedium)
                Text("strokes", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = { onStrokes(hole.number, current + 1) }) { Text("+") }
        }
        Spacer(Modifier.height(8.dp))
    }
}
