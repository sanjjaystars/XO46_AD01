package com.example.shouldersurfingprotection.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GppGood
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Devices
import com.example.shouldersurfingprotection.FaceAnalyzer
import com.example.shouldersurfingprotection.data.ProtectionRepository
import com.example.shouldersurfingprotection.ui.MainViewModel
import com.example.shouldersurfingprotection.ui.ProtectionStatus

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val status by viewModel.status.collectAsState()
    val isEnabled by viewModel.isProtectionEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StatusIndicator(status)

        ProtectionToggle(
            isEnabled = isEnabled,
            onToggle = { viewModel.toggleProtection(it) }
        )

        SensitiveContentCard()

        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "Keep your screen private in public spaces.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatusIndicator(status: ProtectionStatus) {
    val (color, icon, text) = when (status) {
        ProtectionStatus.Protected -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Rounded.GppGood,
            "Protected"
        )
        ProtectionStatus.Monitoring -> Triple(
            MaterialTheme.colorScheme.secondary,
            Icons.Rounded.RemoveRedEye,
            "Monitoring"
        )
        ProtectionStatus.Alert -> Triple(
            MaterialTheme.colorScheme.error,
            Icons.Rounded.Warning,
            "Alert: Surfer Detected"
        )
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProtectionToggle(isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Shield Protection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Automatically obscure screen when others look.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
fun SensitiveContentCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mock Private Data",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            repeat(3) { i ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (i == 0) "Direct Message" else if (i == 1) "Bank Transfer" else "Security Alert",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Confidential info...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (i == 1) "-$500.00" else "Just now",
                        fontWeight = FontWeight.Bold,
                        color = if (i == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (i < 2) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Composable
fun MainScreenPreview() {
    val faceAnalyzer = FaceAnalyzer()
    val repository = ProtectionRepository(faceAnalyzer)
    val viewModel = MainViewModel(repository)
    MaterialTheme {
        Surface {
            MainScreen(viewModel)
        }
    }
}
