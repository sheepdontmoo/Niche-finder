package com.nichefinder.caddie.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nichefinder.caddie.GolfUiState
import com.nichefinder.caddie.ui.theme.Caddie

@Composable
fun CoursesScreen(
    state: GolfUiState,
    modifier: Modifier = Modifier,
    onDemo: () -> Unit,
    onGpsCourse: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(Caddie.heroBrush)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Text("FAIRWAY", style = MaterialTheme.typography.displayMedium, color = Caddie.cream)
        Text("CADDIE", style = MaterialTheme.typography.displayMedium, color = Caddie.fairway)
        Spacer(Modifier.height(10.dp))
        Text(
            "Front · middle · back to every green.\nPay once. No subscription. Ever.",
            style = MaterialTheme.typography.bodyMedium, color = Caddie.creamDim,
        )

        Spacer(Modifier.height(32.dp))

        ActionCard(
            title = "Demo round",
            body = "Three holes, no GPS needed. Watch the number tick down as you walk the first fairway.",
            cta = "TEE OFF",
            highlight = true,
            onClick = onDemo,
        )

        Spacer(Modifier.height(14.dp))

        ActionCard(
            title = "Play a real course",
            body = "Uses your GPS and OpenStreetMap course data. Best at mapped courses — coverage grows weekly.",
            cta = if (state.loading) "FINDING…" else "FIND MY COURSE",
            highlight = false,
            loading = state.loading,
            onClick = { if (!state.loading) onGpsCourse() },
        )

        state.course?.let {
            Spacer(Modifier.height(20.dp))
            Text(
                "On the card: ${it.name} — ${it.holes.size} holes, par ${it.totalPar}",
                style = MaterialTheme.typography.bodyMedium, color = Caddie.fairwaySoft,
            )
        }

        state.message?.let {
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Caddie.pine)
                    .border(1.dp, Caddie.under.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onDismissMessage)
                    .padding(16.dp),
            ) {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Caddie.cream)
                Spacer(Modifier.height(6.dp))
                Text("TAP TO DISMISS", style = MaterialTheme.typography.labelSmall, color = Caddie.creamDim)
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    body: String,
    cta: String,
    highlight: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Caddie.pine)
            .border(1.dp, if (highlight) Caddie.fairway.copy(alpha = 0.55f) else Caddie.pineEdge, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Caddie.cream)
        Spacer(Modifier.height(6.dp))
        Text(body, style = MaterialTheme.typography.bodySmall, color = Caddie.creamDim)
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (highlight) Caddie.fairway else Color.Transparent)
                .border(1.dp, if (highlight) Caddie.fairway else Caddie.fairway.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.height(16.dp).padding(end = 8.dp), color = Caddie.fairway, strokeWidth = 2.dp)
            }
            Text(
                cta,
                style = MaterialTheme.typography.labelLarge,
                color = if (highlight) Caddie.pineDeep else Caddie.fairway,
            )
        }
    }
}
