package com.example.shouldersurfingprotection.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import com.example.shouldersurfingprotection.ui.navigation.MainNavKey

@Composable
fun AdaptiveScaffold(
    backstack: MutableList<MainNavKey>,
    content: @Composable () -> Unit
) {
    val currentKey = backstack.lastOrNull() ?: MainNavKey.Home
    
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = currentKey == MainNavKey.Home,
                onClick = { 
                    backstack.clear()
                    backstack.add(MainNavKey.Home)
                },
                icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                label = { Text("Home") }
            )
            item(
                selected = currentKey == MainNavKey.Settings,
                onClick = { 
                    if (currentKey != MainNavKey.Settings) {
                        backstack.clear()
                        backstack.add(MainNavKey.Home)
                        backstack.add(MainNavKey.Settings)
                    }
                },
                icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                label = { Text("Settings") }
            )
        }
    ) {
        content()
    }
}
