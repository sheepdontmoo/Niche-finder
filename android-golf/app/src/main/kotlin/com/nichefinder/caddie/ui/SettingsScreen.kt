package com.nichefinder.caddie.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.nichefinder.caddie.GolfUiState
import com.nichefinder.caddie.R
import com.nichefinder.caddie.ui.theme.Caddie

@Composable
fun SettingsScreen(
    state: GolfUiState,
    modifier: Modifier = Modifier,
    onUseMeters: (Boolean) -> Unit,
    onAutoAdvance: (Boolean) -> Unit = {},
) {
    Column(modifier.fillMaxSize().background(Caddie.pineDeep).padding(24.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("SETTINGS", style = MaterialTheme.typography.displaySmall, color = Caddie.cream)
        Spacer(Modifier.height(20.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Caddie.pine)
                .border(1.dp, Caddie.pineEdge, RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Meters", style = MaterialTheme.typography.titleMedium, color = Caddie.cream)
                Text("Show distances in meters instead of yards",
                    style = MaterialTheme.typography.bodySmall, color = Caddie.creamDim)
            }
            Switch(
                checked = state.useMeters,
                onCheckedChange = onUseMeters,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Caddie.fairway,
                    checkedThumbColor = Caddie.pineDeep,
                ),
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Caddie.pine)
                .border(1.dp, Caddie.pineEdge, RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Auto-advance holes", style = MaterialTheme.typography.titleMedium, color = Caddie.cream)
                Text("Switch to the next hole when you walk to its tee",
                    style = MaterialTheme.typography.bodySmall, color = Caddie.creamDim)
            }
            Switch(
                checked = state.autoAdvance,
                onCheckedChange = onAutoAdvance,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Caddie.fairway,
                    checkedThumbColor = Caddie.pineDeep,
                ),
            )
        }

        Spacer(Modifier.height(14.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Caddie.pine)
                .border(1.dp, Caddie.gold.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .padding(18.dp),
        ) {
            Text("CADDIE PLUS", style = MaterialTheme.typography.labelLarge, color = Caddie.gold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Everything, every round: offline courses, shot measuring, club distances and " +
                    "round history. Free trial in the beta, then a simple yearly or monthly plan.",
                style = MaterialTheme.typography.bodySmall, color = Caddie.creamDim,
            )
        }

        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.brand_mark),
                contentDescription = null,
                modifier = Modifier.height(30.dp),
            )
            Spacer(Modifier.height(0.dp))
            Text("  FAIRWAY CADDIE", style = MaterialTheme.typography.labelLarge, color = Caddie.creamDim)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Course data © OpenStreetMap contributors. Distances are measured to the mapped " +
                "green outline and its centre — always sanity-check locally.",
            style = MaterialTheme.typography.bodySmall, color = Caddie.creamDim,
        )
        Spacer(Modifier.height(12.dp))
    }
}
