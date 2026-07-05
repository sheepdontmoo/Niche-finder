package com.nichefinder.docstash.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    proUnlocked: Boolean,
    priceLabel: String?,
    modifier: Modifier = Modifier,
    onUnlockClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (proUnlocked) {
                    Text("Pro unlocked", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Thanks for supporting DocStash. Combined multi-page searchable PDF export " +
                            "is unlocked forever \u2014 no subscription, ever.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text("DocStash Pro \u2014 one-time purchase", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Unlock combined multi-page searchable PDF export. Pay once${priceLabel?.let { ", $it" } ?: ""}, " +
                            "keep it forever \u2014 we don't do subscriptions, ever.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = onUnlockClick, modifier = Modifier.fillMaxWidth()) {
                        Text(priceLabel?.let { "Unlock Pro \u2014 $it one-time" } ?: "Unlock Pro")
                    }
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Your privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Every document stays on this device by default. No account, no forced cloud " +
                        "upload, no ads, no watermarks, and no page or scan limits \u2014 on the free " +
                        "tier or Pro.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
