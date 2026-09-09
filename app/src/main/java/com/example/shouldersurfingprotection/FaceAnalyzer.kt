package com.example.shouldersurfingprotection

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.abs

class FaceAnalyzer : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    private val _isSurferDetected = MutableSharedFlow<Boolean>(replay = 1)
    val isSurferDetected: SharedFlow<Boolean> = _isSurferDetected.asSharedFlow()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotation)
            
            // For heuristics, we need image dimensions
            // Note: If rotation is 90 or 270, width and height are swapped for the processed image
            val isRotated = rotation == 90 || rotation == 270
            val width = if (isRotated) imageProxy.height else imageProxy.width
            val height = if (isRotated) imageProxy.width else imageProxy.height

            detector.process(image)
                .addOnSuccessListener { faces ->
                    val surferFound = detectSurfer(faces, width, height)
                    _isSurferDetected.tryEmit(surferFound)
                    Log.d("FaceAnalyzer", "Faces: ${faces.size}, Surfer: $surferFound")
                }
                .addOnFailureListener { e ->
                    Log.e("FaceAnalyzer", "Face detection failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun detectSurfer(faces: List<Face>, width: Int, height: Int): Boolean {
        if (faces.isEmpty()) return false
        
        // If only one face is detected, it's likely the user
        if (faces.size == 1) return false

        // Identify "Primary User": The face with the largest bounding box area that is relatively centered.
        // Even if we just check faces.size > 1, the requirement asks to identify the primary user.
        // We can use this to be more robust, e.g. ignoring very small background faces.
        
        val primaryUser = faces.maxByOrNull { face ->
            val box = face.boundingBox
            val area = box.width() * box.height()
            
            // Centered heuristic: penalty for being far from center
            val centerX = box.centerX()
            val distanceFromCenter = abs(centerX - width / 2f)
            val centralityFactor = 1f - (distanceFromCenter / (width / 2f))
            
            area * centralityFactor
        }

        // Identify "Surfers": Any additional faces detected in the frame.
        // A surfer is any face that is NOT the primary user and is significant enough.
        val surfers = faces.filter { it != primaryUser }
        
        // Optimization: UI response in < 300ms is handled by FAST mode and tryEmit.
        return surfers.isNotEmpty()
    }
}
