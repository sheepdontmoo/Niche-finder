package com.nichefinder.caddie.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nichefinder.caddie.GolfUiState

@Composable
fun CoursesScreen(
    state: GolfUiState,
    modifier: Modifier = Modifier,
    onDemo: () -> Unit,
    onGpsCourse: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Fairway Caddie", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Front · middle · back distances on any mapped course. One-time price, no subscription.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Play a demo round", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Three holes, no GPS needed — watch the distances tick down as you " +
                        "\"walk\" the fairway.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = onDemo, modifier = Modifier.fillMaxWidth()) { Text("Start demo round") }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Play a real course", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Uses your GPS position and OpenStreetMap course data. Works best at " +
                        "mapped courses; coverage varies (and is improving).",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(onClick = onGpsCourse, modifier = Modifier.fillMaxWidth(), enabled = !state.loading) {
                    if (state.loading) CircularProgressIndicator(Modifier.padding(end = 8.dp))
                    Text(if (state.loading) "Finding course…" else "Find course at my location")
                }
            }
        }

        state.course?.let {
            Text("Loaded: ${it.name} — ${it.holes.size} holes, par ${it.totalPar}",
                style = MaterialTheme.typography.bodyMedium)
        }

        state.message?.let {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onDismissMessage) { Text("OK") }
                }
            }
        }
    }
}
