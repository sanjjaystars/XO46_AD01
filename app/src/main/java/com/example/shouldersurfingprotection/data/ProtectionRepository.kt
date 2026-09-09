package com.example.shouldersurfingprotection.data

import com.example.shouldersurfingprotection.FaceAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProtectionRepository(
    val faceAnalyzer: FaceAnalyzer
) {
    private val _isProtectionEnabled = MutableStateFlow(true)
    val isProtectionEnabled: StateFlow<Boolean> = _isProtectionEnabled.asStateFlow()

    fun setProtectionEnabled(enabled: Boolean) {
        _isProtectionEnabled.value = enabled
    }

    val isSurferDetected = faceAnalyzer.isSurferDetected
}
