package com.nichefinder.scanner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nichefinder.obd2.pid.Pid
import com.nichefinder.obd2.pid.PidValue
import com.nichefinder.scanner.ConnectionState
import com.nichefinder.scanner.UiState

/** Live gauges. Each supported PID renders as an arc gauge card. */
@Composable
fun DashboardScreen(state: UiState, modifier: Modifier = Modifier) {
    if (state.connection !is ConnectionState.Connected) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Connect to a vehicle first (or try demo mode)", textAlign = TextAlign.Center)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(state.gauges.entries.toList(), key = { it.key.code }) { (pid, value) ->
            GaugeCard(pid, value, state.imperialUnits)
        }
    }
}

@Composable
private fun GaugeCard(pid: Pid, value: PidValue, imperial: Boolean) {
    val (display, unit) = displayFor(pid, value, imperial)
    Card {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().aspectRatio(1.6f), contentAlignment = Alignment.Center) {
                ArcGauge(fraction = fractionFor(pid, value.value))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(display, style = MaterialTheme.typography.headlineMedium)
                    Text(unit, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(pid.label, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ArcGauge(fraction: Float) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val fill = when {
        fraction < 0.75f -> MaterialTheme.colorScheme.primary
        fraction < 0.9f -> Color(0xFFE6A817)
        else -> MaterialTheme.colorScheme.error
    }
    Canvas(Modifier.fillMaxSize().padding(8.dp)) {
        val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
        val arcSize = Size(size.width - stroke.width, size.width - stroke.width)
        val topLeft = Offset(stroke.width / 2, stroke.width / 2)
        drawArc(track, 135f, 270f, false, topLeft, arcSize, style = stroke)
        drawArc(fill, 135f, 270f * fraction.coerceIn(0f, 1f), false, topLeft, arcSize, style = stroke)
    }
}

/** Sensible full-scale per gauge so the arc means something. */
private fun fractionFor(pid: Pid, value: Double): Float = when (pid) {
    Pid.RPM -> (value / 8000.0).toFloat()
    Pid.SPEED -> (value / 240.0).toFloat()
    Pid.COOLANT_TEMP -> ((value + 40) / 180.0).toFloat()
    Pid.INTAKE_TEMP, Pid.AMBIENT_TEMP, Pid.OIL_TEMP -> ((value + 40) / 190.0).toFloat()
    Pid.CONTROL_MODULE_VOLTAGE -> ((value - 8.0) / 8.0).toFloat()
    else -> (value / 100.0).toFloat()   // percent-style PIDs
}

private fun displayFor(pid: Pid, v: PidValue, imperial: Boolean): Pair<String, String> {
    if (imperial) {
        when (pid) {
            Pid.SPEED -> return "%.0f".format(v.value * 0.621371) to "mph"
            Pid.COOLANT_TEMP, Pid.INTAKE_TEMP, Pid.AMBIENT_TEMP, Pid.OIL_TEMP ->
                return "%.0f".format(v.value * 9 / 5 + 32) to "°F"
            else -> {}
        }
    }
    return pid.format(v.value) to pid.unit
}
