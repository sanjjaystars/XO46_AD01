package com.example.shouldersurfing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * UI state for the protection screen.
 *
 * isProtectionEnabled : user-controlled master toggle.
 * faceCount           : latest number of faces seen in the front camera frame.
 * isThreatDetected    : true when the FaceAnalyzer heuristic believes a second person is
 *                        close enough to be reading the screen.
 * isContentHidden     : the actual, derived flag the UI uses to decide whether to show the
 *                        blur/hide overlay (protection must be enabled AND a threat detected).
 */
data class ProtectionUiState(
    val isProtectionEnabled: Boolean = true,
    val faceCount: Int = 0,
    val isThreatDetected: Boolean = false
) {
    val isContentHidden: Boolean get() = isProtectionEnabled && isThreatDetected

    val statusText: String
        get() = when {
            !isProtectionEnabled -> "Protection off"
            isThreatDetected -> "Alert: additional viewer detected"
            faceCount == 1 -> "Protected \u2022 monitoring"
            faceCount == 0 -> "Protected \u2022 no face in view"
            else -> "Protected \u2022 monitoring"
        }
}

class ProtectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProtectionUiState())
    val uiState: StateFlow<ProtectionUiState> = _uiState

    private val analyzer = FaceAnalyzer { result ->
        _uiState.update { current ->
            current.copy(
                faceCount = result.totalFaces,
                isThreatDetected = result.threatDetected
            )
        }
    }

    fun getAnalyzer(): FaceAnalyzer = analyzer

    fun toggleProtection() {
        _uiState.update { it.copy(isProtectionEnabled = !it.isProtectionEnabled) }
    }

    override fun onCleared() {
        super.onCleared()
        analyzer.shutdown()
    }
}
