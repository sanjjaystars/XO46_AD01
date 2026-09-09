package com.example.shouldersurfingprotection.ui.navigation

import androidx.navigation3.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class MainNavKey : NavKey {
    @Serializable
    data object Home : MainNavKey()
    
    @Serializable
    data object Settings : MainNavKey()
}
