package com.example.shouldersurfingprotection.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Text(
            text = "Protection Sensitivity",
            style = MaterialTheme.typography.titleMedium
        )
        Slider(value = 0.5f, onValueChange = {})
        
        ListItem(
            headlineContent = { Text("Detection Logging") },
            supportingContent = { Text("Keep a record of detections") },
            trailingContent = { Switch(checked = true, onCheckedChange = {}) }
        )
        
        ListItem(
            headlineContent = { Text("Decoy UI Style") },
            supportingContent = { Text("Currently: Banking App") },
            trailingContent = { TextButton(onClick = {}) { Text("Change") } }
        )
    }
}
