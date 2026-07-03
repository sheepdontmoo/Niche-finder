package com.nichefinder.caddie.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nichefinder.caddie.GolfUiState

@Composable
fun SettingsScreen(
    state: GolfUiState,
    modifier: Modifier = Modifier,
    onUseMeters: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Meters instead of yards")
            Switch(checked = state.useMeters, onCheckedChange = onUseMeters)
        }

        HorizontalDivider()

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pro — one-time purchase", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Pay once, keep forever. No subscription, ever. Coming in the beta: " +
                        "shot tracking, club distances, and round history."
                )
            }
        }

        Text(
            "Course data © OpenStreetMap contributors. Distances are to the green outline " +
                "and centre; always verify locally.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
