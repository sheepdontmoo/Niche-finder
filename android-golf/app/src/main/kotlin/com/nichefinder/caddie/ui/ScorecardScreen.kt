package com.nichefinder.caddie.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nichefinder.caddie.GolfUiState

@Composable
fun ScorecardScreen(
    state: GolfUiState,
    modifier: Modifier = Modifier,
    onStrokes: (hole: Int, strokes: Int) -> Unit,
    labelFor: (Int) -> String,
) {
    val course = state.course
    if (course == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No round in progress", textAlign = TextAlign.Center)
        }
        return
    }

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.totalStrokes}", style = MaterialTheme.typography.headlineMedium)
                    Text("strokes", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val tp = state.toPar
                    Text(if (tp > 0) "+$tp" else "$tp", style = MaterialTheme.typography.headlineMedium)
                    Text("to par", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.stableford}", style = MaterialTheme.typography.headlineMedium)
                    Text("stableford", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        LazyColumn(
            Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(course.holes) { hole ->
                val strokes = state.strokesByHole[hole.number] ?: 0
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Hole ${hole.number} · Par ${hole.par}", style = MaterialTheme.typography.titleMedium)
                            val label = labelFor(hole.number)
                            if (label.isNotEmpty()) Text(label, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        OutlinedButton(onClick = { if (strokes > 0) onStrokes(hole.number, strokes - 1) }) { Text("−") }
                        Text(
                            if (strokes == 0) " — " else " $strokes ",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        OutlinedButton(onClick = { onStrokes(hole.number, strokes + 1) }) { Text("+") }
                    }
                }
            }
        }
    }
}
