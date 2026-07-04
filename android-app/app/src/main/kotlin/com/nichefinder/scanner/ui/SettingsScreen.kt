package com.nichefinder.scanner.ui

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
import com.nichefinder.scanner.UiState

@Composable
fun SettingsScreen(
    state: UiState,
    modifier: Modifier = Modifier,
    onImperialChange: (Boolean) -> Unit,
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
            Text("Imperial units (mph, °F)")
            Switch(checked = state.imperialUnits, onCheckedChange = onImperialChange)
        }

        HorizontalDivider()

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pro — one-time purchase", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Everything unlocked forever with a single purchase. No subscription, " +
                        "ever. Coming in the beta: unlimited gauges, data logging with CSV " +
                        "export, and Android Auto dashboards."
                )
                // Play Billing one-time product wiring is a Step-1 task (see AGENTS.md).
            }
        }

        Text(
            "Works with standard ELM327 adapters — Bluetooth, Bluetooth LE, and WiFi. " +
                "No proprietary hardware required.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
