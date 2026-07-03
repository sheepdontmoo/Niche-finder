package com.nichefinder.scanner.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nichefinder.obd2.transport.ObdTransport
import com.nichefinder.scanner.ConnectionState
import com.nichefinder.scanner.UiState
import com.nichefinder.scanner.transport.BleTransport
import com.nichefinder.scanner.transport.BluetoothClassicTransport
import com.nichefinder.scanner.transport.WifiTransport
import kotlinx.coroutines.delay

/**
 * Entry point: pick how to reach the adapter. Bluetooth Classic lists paired
 * devices (clones pair in system settings, PIN 1234/0000); BLE runs a short
 * scan; WiFi uses the standard 192.168.0.10:35000; Demo needs no hardware.
 */
@SuppressLint("MissingPermission") // gated by runtime permission request in MainActivity
@Composable
fun ConnectScreen(
    state: UiState,
    modifier: Modifier = Modifier,
    onDemo: () -> Unit,
    onConnect: (ObdTransport) -> Unit,
    onDisconnect: () -> Unit,
) {
    val context = LocalContext.current
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var bleDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var scanning by remember { mutableStateOf(false) }
    var wifiHost by remember { mutableStateOf("192.168.0.10") }
    var wifiPort by remember { mutableStateOf("35000") }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            when (val c = state.connection) {
                is ConnectionState.Connected -> Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            if (c.demo) "Connected — demo vehicle" else "Connected",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(c.protocol, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = onDisconnect) { Text("Disconnect") }
                    }
                }
                is ConnectionState.Connecting -> Row {
                    CircularProgressIndicator()
                    Text("  Connecting…", style = MaterialTheme.typography.titleMedium)
                }
                is ConnectionState.Error -> Card {
                    Text(
                        c.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                ConnectionState.Idle -> Text(
                    "Plug the adapter into the OBD port (under the dashboard), " +
                        "turn the ignition on, then connect below.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item { HorizontalDivider() }

        item {
            Button(onClick = onDemo, modifier = Modifier.fillMaxWidth()) {
                Text("Try demo mode (no adapter needed)")
            }
        }

        item { Text("Bluetooth (paired adapters)", style = MaterialTheme.typography.titleMedium) }
        item {
            OutlinedButton(onClick = {
                val bt = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                pairedDevices = bt.adapter?.bondedDevices?.toList().orEmpty()
            }) { Text("Show paired devices") }
        }
        items(pairedDevices) { device ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp)) {
                    Column(Modifier.fillMaxWidth(0.6f)) {
                        Text(device.name ?: "(no name)")
                        Text(device.address, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { onConnect(BluetoothClassicTransport(device)) }) {
                        Text("Connect")
                    }
                }
            }
        }

        item { Text("Bluetooth LE adapters", style = MaterialTheme.typography.titleMedium) }
        item {
            OutlinedButton(enabled = !scanning, onClick = { scanning = true }) {
                Text(if (scanning) "Scanning…" else "Scan for BLE adapters")
            }
            if (scanning) {
                BleScanEffect(
                    context = context,
                    onFound = { d -> if (bleDevices.none { it.address == d.address }) bleDevices = bleDevices + d },
                    onDone = { scanning = false },
                )
            }
        }
        items(bleDevices) { device ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp)) {
                    Column(Modifier.fillMaxWidth(0.6f)) {
                        Text(device.name ?: "(no name)")
                        Text(device.address, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { onConnect(BleTransport(context, device)) }) {
                        Text("Connect")
                    }
                }
            }
        }

        item { Text("WiFi adapter", style = MaterialTheme.typography.titleMedium) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = wifiHost, onValueChange = { wifiHost = it }, label = { Text("Host") })
                OutlinedTextField(value = wifiPort, onValueChange = { wifiPort = it }, label = { Text("Port") })
                Button(onClick = {
                    onConnect(WifiTransport(wifiHost.trim(), wifiPort.trim().toIntOrNull() ?: 35000))
                }) { Text("Connect over WiFi") }
            }
        }
    }
}

/** Runs one bounded BLE scan while composed; filters for likely OBD adapters. */
@SuppressLint("MissingPermission")
@Composable
private fun BleScanEffect(
    context: Context,
    onFound: (BluetoothDevice) -> Unit,
    onDone: () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val bt = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanner = bt.adapter?.bluetoothLeScanner
        if (scanner == null) { onDone(); return@LaunchedEffect }
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = result.device?.name ?: return
                val n = name.uppercase()
                if (listOf("OBD", "ELM", "VGATE", "VLINK", "VEEPEAK", "IOS-VLINK", "SCAN").any { it in n }) {
                    onFound(result.device)
                }
            }
        }
        scanner.startScan(callback)
        delay(8_000)
        runCatching { scanner.stopScan(callback) }
        onDone()
    }
}
