package com.nichefinder.scanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nichefinder.scanner.ConnectionState
import com.nichefinder.scanner.DtcUi
import com.nichefinder.scanner.UiState

/** Check-engine codes: read, understand in plain language, clear (with confirmation). */
@Composable
fun DtcScreen(
    state: UiState,
    modifier: Modifier = Modifier,
    onRead: () -> Unit,
    onClear: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }
    val connected = state.connection is ConnectionState.Connected

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onRead, enabled = connected) { Text("Read codes") }
                OutlinedButton(
                    onClick = { confirmClear = true },
                    enabled = connected && state.storedCodes.isNotEmpty(),
                ) { Text("Clear codes") }
            }
        }

        if (!connected) {
            item { Text("Connect to a vehicle first (or try demo mode).") }
        } else if (state.codesReadOnce && state.storedCodes.isEmpty() && state.pendingCodes.isEmpty()) {
            item { Text("No trouble codes — engine reports clean. ✅") }
        }

        if (state.storedCodes.isNotEmpty()) {
            item { Text("Stored codes (check-engine light)", style = MaterialTheme.typography.titleMedium) }
            items(state.storedCodes) { DtcCard(it) }
        }
        if (state.pendingCodes.isNotEmpty()) {
            item { Text("Pending codes (intermittent, not yet confirmed)", style = MaterialTheme.typography.titleMedium) }
            items(state.pendingCodes) { DtcCard(it) }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear trouble codes?") },
            text = {
                Text(
                    "This turns off the check-engine light and erases stored codes and " +
                        "readiness monitors. If the underlying problem remains, the light " +
                        "will come back. Emissions tests may fail until monitors re-run."
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; onClear() }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun DtcCard(dtc: DtcUi) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(dtc.code, style = MaterialTheme.typography.titleMedium)
                Text(dtc.system, style = MaterialTheme.typography.bodySmall)
            }
            Text(dtc.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
