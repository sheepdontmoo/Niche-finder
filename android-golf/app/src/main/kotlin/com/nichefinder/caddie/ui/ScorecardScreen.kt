package com.nichefinder.caddie.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nichefinder.caddie.GolfUiState
import com.nichefinder.caddie.ui.theme.Anton
import com.nichefinder.caddie.ui.theme.Caddie

/**
 * Scorecard with the notation golfers already read fluently:
 * red circle = under par, no mark = par, blue square = over par.
 */
@Composable
fun ScorecardScreen(
    state: GolfUiState,
    modifier: Modifier = Modifier,
    onStrokes: (hole: Int, strokes: Int) -> Unit,
    labelFor: (Int) -> String,
) {
    val course = state.course
    if (course == null) {
        EmptyState(modifier, "No card yet", "Start a round and your scorecard fills in here.")
        return
    }

    Column(modifier.fillMaxSize().background(Caddie.pineDeep).padding(20.dp)) {
        // Totals band
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Caddie.pine)
                .border(1.dp, Caddie.pineEdge, RoundedCornerShape(18.dp))
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Total("${state.totalStrokes}", "STROKES", Caddie.cream)
            val tp = state.toPar
            Total(
                if (tp > 0) "+$tp" else "$tp", "TO PAR",
                when { tp < 0 -> Caddie.under; tp > 0 -> Caddie.over; else -> Caddie.cream },
            )
            Total("${state.stableford}", "POINTS", Caddie.gold)
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(course.holes) { hole ->
                val strokes = state.strokesByHole[hole.number] ?: 0
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Caddie.pine)
                        .border(1.dp, Caddie.pineEdge, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${hole.number}",
                        fontFamily = Anton,
                        style = MaterialTheme.typography.displaySmall,
                        color = Caddie.creamDim,
                        modifier = Modifier.width(40.dp),
                    )
                    Column(Modifier.weight(1f)) {
                        Text("Par ${hole.par}", style = MaterialTheme.typography.titleMedium, color = Caddie.cream)
                        val label = labelFor(hole.number)
                        if (label.isNotEmpty()) {
                            Text(label, style = MaterialTheme.typography.bodySmall,
                                color = if (strokes < hole.par) Caddie.under else Caddie.creamDim)
                        }
                    }
                    Stepper("−") { if (strokes > 0) onStrokes(hole.number, strokes - 1) }
                    ScoreMark(strokes, hole.par)
                    Stepper("+") { onStrokes(hole.number, strokes + 1) }
                }
            }
        }
    }
}

@Composable
private fun Total(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = Anton, style = MaterialTheme.typography.displayMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Caddie.creamDim)
    }
}

/** Traditional golf marks: circle under par, square over par. */
@Composable
private fun ScoreMark(strokes: Int, par: Int) {
    val shape = if (strokes in 1 until par) CircleShape else RoundedCornerShape(6.dp)
    val borderColor = when {
        strokes == 0 -> Color.Transparent
        strokes < par -> Caddie.under
        strokes == par -> Color.Transparent
        else -> Caddie.over
    }
    Box(
        Modifier
            .padding(horizontal = 10.dp)
            .size(44.dp)
            .clip(shape)
            .border(2.dp, borderColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (strokes == 0) "–" else "$strokes",
            fontFamily = Anton,
            style = MaterialTheme.typography.headlineMedium,
            color = if (strokes == 0) Caddie.creamDim else Caddie.cream,
        )
    }
}

@Composable
private fun Stepper(glyph: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Caddie.pineDeep)
            .border(1.dp, Caddie.pineEdge, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, style = MaterialTheme.typography.titleLarge, color = Caddie.fairway)
    }
}
