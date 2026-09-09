package com.example.shouldersurfingprotection.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shouldersurfingprotection.data.ProtectionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed class ProtectionStatus {
    data object Protected : ProtectionStatus()
    data object Monitoring : ProtectionStatus()
    data object Alert : ProtectionStatus()
}

class MainViewModel(
    private val repository: ProtectionRepository
) : ViewModel() {

    val isProtectionEnabled = repository.isProtectionEnabled
    val isSurferDetected = repository.isSurferDetected

    val status: StateFlow<ProtectionStatus> = combine(
        isProtectionEnabled,
        isSurferDetected
    ) { enabled, detected ->
        when {
            detected -> ProtectionStatus.Alert
            enabled -> ProtectionStatus.Protected
            else -> ProtectionStatus.Monitoring
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProtectionStatus.Monitoring
    )

    fun toggleProtection(enabled: Boolean) {
        repository.setProtectionEnabled(enabled)
    }
}
