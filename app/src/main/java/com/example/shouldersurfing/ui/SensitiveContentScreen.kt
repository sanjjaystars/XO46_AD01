package com.example.shouldersurfing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shouldersurfing.ui.theme.SafeGreen

/**
 * A mock "sensitive" screen (bank-style balance + transactions + a password field) used to
 * demonstrate the shoulder-surfing protection working live. Nothing here is real financial data.
 */
@Composable
fun SensitiveContentScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = SafeGreen)
            Spacer(Modifier.width(8.dp))
            Text("My Wallet", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SafeGreen.copy(alpha = 0.12f))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Available balance", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(6.dp))
                Text(
                    "\u20B9 4,82,930.75",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text("A/C **** 5521", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Recent transactions", style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(8.dp))

        val transactions = listOf(
            Triple("Amazon", "-\u20B9 1,249.00", "Today"),
            Triple("Salary credit", "+\u20B9 78,000.00", "Yesterday"),
            Triple("Zomato", "-\u20B9 540.00", "2 days ago"),
            Triple("Rent transfer", "-\u20B9 15,000.00", "3 days ago")
        )

        transactions.forEach { (name, amount, date) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(name, style = MaterialTheme.typography.bodyLarge)
                    Text(date, style = MaterialTheme.typography.labelSmall)
                }
                Text(
                    amount,
                    fontWeight = FontWeight.SemiBold,
                    color = if (amount.startsWith("+")) SafeGreen else Color(0xFFE0473C)
                )
            }
            Divider()
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
            onValueChange = {},
            readOnly = true,
            label = { Text("Login PIN") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
