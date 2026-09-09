package com.example.shouldersurfing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.shouldersurfing.ProtectionUiState
import com.example.shouldersurfing.ui.theme.AlertRed
import com.example.shouldersurfing.ui.theme.SafeGreen

@Composable
fun StatusBar(
    state: ProtectionUiState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when {
        !state.isProtectionEnabled -> Color.Gray
        state.isThreatDetected -> AlertRed
        else -> SafeGreen
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(indicatorColor.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(state.statusText, style = MaterialTheme.typography.bodyLarge, color = indicatorColor)
            Text(
                "Faces in view: ${state.faceCount}",
                style = MaterialTheme.typography.labelSmall
            )
        }
        Switch(checked = state.isProtectionEnabled, onCheckedChange = { onToggle() })
    }
}
