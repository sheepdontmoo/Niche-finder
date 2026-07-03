package com.nichefinder.scanner

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nichefinder.scanner.ui.ConnectScreen
import com.nichefinder.scanner.ui.DashboardScreen
import com.nichefinder.scanner.ui.DtcScreen
import com.nichefinder.scanner.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBtPermissions()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                ScannerApp()
            }
        }
    }

    private fun requestBtPermissions() {
        val needed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        permissionLauncher.launch(needed)
    }
}

private enum class Screen(val label: String) {
    Connect("Connect"), Dashboard("Gauges"), Codes("Codes"), Settings("Settings")
}

@Composable
private fun ScannerApp(vm: ObdViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var screen by remember { mutableStateOf(Screen.Connect) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { s ->
                    NavigationBarItem(
                        selected = screen == s,
                        onClick = { screen = s },
                        label = { Text(s.label) },
                        icon = {
                            Icon(
                                when (s) {
                                    Screen.Connect -> Icons.Default.Bluetooth
                                    Screen.Dashboard -> Icons.Default.Speed
                                    Screen.Codes -> Icons.Default.Warning
                                    Screen.Settings -> Icons.Default.Settings
                                },
                                contentDescription = s.label,
                            )
                        },
                    )
                }
            }
        }
    ) { padding ->
        val mod = Modifier.padding(padding)
        when (screen) {
            Screen.Connect -> ConnectScreen(
                state = state,
                modifier = mod,
                onDemo = { vm.connectDemo(); screen = Screen.Dashboard },
                onConnect = { t -> vm.connect(t); screen = Screen.Dashboard },
                onDisconnect = vm::disconnect,
            )
            Screen.Dashboard -> DashboardScreen(state = state, modifier = mod)
            Screen.Codes -> DtcScreen(
                state = state,
                modifier = mod,
                onRead = vm::readCodes,
                onClear = vm::clearCodes,
            )
            Screen.Settings -> SettingsScreen(
                state = state,
                modifier = mod,
                onImperialChange = vm::setImperial,
            )
        }
    }
}
