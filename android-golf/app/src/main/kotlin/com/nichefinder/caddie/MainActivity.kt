package com.nichefinder.caddie

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nichefinder.caddie.ui.CoursesScreen
import com.nichefinder.caddie.ui.PlayScreen
import com.nichefinder.caddie.ui.ScorecardScreen
import com.nichefinder.caddie.ui.SettingsScreen
import com.nichefinder.caddie.ui.theme.Caddie
import com.nichefinder.caddie.ui.theme.CaddieTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A rangefinder that sleeps mid-hole is useless: keep the screen on.
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
        setContent {
            CaddieTheme {
                CaddieApp()
            }
        }
    }
}

private enum class Screen(val label: String) {
    Courses("Course"), Play("Play"), Card("Card"), Settings("Settings")
}

@Composable
private fun CaddieApp(vm: GolfViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var screen by remember { mutableStateOf(Screen.Courses) }

    Scaffold(
        containerColor = Caddie.pineDeep,
        bottomBar = {
            NavigationBar(containerColor = Caddie.pine) {
                Screen.entries.forEach { s ->
                    NavigationBarItem(
                        selected = screen == s,
                        onClick = { screen = s },
                        label = { Text(s.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Caddie.pineDeep,
                            indicatorColor = Caddie.fairway,
                            selectedTextColor = Caddie.cream,
                            unselectedIconColor = Caddie.creamDim,
                            unselectedTextColor = Caddie.creamDim,
                        ),
                        icon = {
                            Icon(
                                when (s) {
                                    Screen.Courses -> Icons.Default.GolfCourse
                                    Screen.Play -> Icons.Default.SportsGolf
                                    Screen.Card -> Icons.AutoMirrored.Filled.List
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
            Screen.Courses -> CoursesScreen(
                state = state,
                modifier = mod,
                onDemo = { vm.startDemoRound(); screen = Screen.Play },
                onGpsCourse = { vm.startGps(); vm.loadCourseNearMe() },
                onDismissMessage = vm::dismissMessage,
            )
            Screen.Play -> PlayScreen(
                state = state,
                modifier = mod,
                onSelectHole = vm::selectHole,
                onStrokes = vm::setStrokes,
            )
            Screen.Card -> ScorecardScreen(
                state = state,
                modifier = mod,
                onStrokes = vm::setStrokes,
                labelFor = vm::scoreLabel,
            )
            Screen.Settings -> SettingsScreen(
                state = state,
                modifier = mod,
                onUseMeters = vm::setUseMeters,
            )
        }
    }
}
