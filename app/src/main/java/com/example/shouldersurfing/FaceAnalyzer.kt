package com.example.shouldersurfing

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * Result of analyzing a single camera frame.
 *
 * - totalFaces: how many faces ML Kit found in the frame at all (including the primary user).
 * - threatDetected: true when, beyond the primary user, at least one other face is large/close
 *   enough to plausibly be someone reading the screen over the user's shoulder.
 */
data class FaceAnalysisResult(
    val totalFaces: Int,
    val threatDetected: Boolean
)

/**
 * CameraX analyzer that:
 *  1. Throttles processing to a fixed low rate (FRAME_SKIP) to save battery instead of running
 *     ML Kit on every single frame from the sensor.
 *  2. Runs on-device ML Kit face detection on the current frame only.
 *  3. Applies a simple, explainable "primary user vs. shoulder surfer" heuristic based on
 *     relative face size (bounding-box area), since the closest/largest face in a front-camera
 *     shot is overwhelmingly likely to be the person holding the phone.
 *  4. Immediately discards the frame (imageProxy.close()) after analysis -- no frame is ever
 *     copied, cached, or written to storage, satisfying the "no stored camera images" constraint.
 *
 * onResult is invoked with the latest FaceAnalysisResult. It may be called from a background
 * thread; StateFlow (used by the ViewModel) is safe to update from any thread.
 */
class FaceAnalyzer(
    private val onResult: (FaceAnalysisResult) -> Unit
) : ImageAnalysis.Analyzer {

    companion object {
        // Only analyze 1 out of every N frames delivered by CameraX. At a typical 30fps preview
        // this yields roughly 5-6 analyses per second -- fast enough to react well under the
        // ~300ms target, while keeping CPU/battery load low as required by the PS.
        private const val FRAME_SKIP = 5

        // A second face must have a bounding-box area at least this fraction of the primary
        // (largest) face's area to be treated as a genuine nearby viewer rather than a person
        // far in the background (e.g. someone walking past on a train).
        private const val SURFER_AREA_RATIO_THRESHOLD = 0.20f
    }

    private var frameCounter = 0

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            // FAST performance mode favors low latency over landmark/contour precision,
            // which is exactly what we need for a real-time privacy trigger.
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.10f)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        frameCounter++

        // Skip most frames entirely -- close immediately so CameraX can deliver the next one.
        if (frameCounter % FRAME_SKIP != 0) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { faces -> onResult(evaluate(faces)) }
            .addOnFailureListener {
                // Fail safe: on a detection error, report "no faces" rather than leaving stale
                // state, so the UI never gets stuck in a false "protected" or "threat" state.
                onResult(FaceAnalysisResult(totalFaces = 0, threatDetected = false))
            }
            .addOnCompleteListener {
                // Always close the frame -- nothing from it is ever retained.
                imageProxy.close()
            }
    }

    private fun evaluate(faces: List<Face>): FaceAnalysisResult {
        if (faces.isEmpty()) {
            return FaceAnalysisResult(totalFaces = 0, threatDetected = false)
        }
        if (faces.size == 1) {
            return FaceAnalysisResult(totalFaces = 1, threatDetected = false)
        }

        // Primary user = largest face in frame (closest to the front camera).
        val areas = faces.map { it.boundingBox.width().toLong() * it.boundingBox.height().toLong() }
        val primaryArea = areas.max()

        // Any other face big enough relative to the primary user counts as a likely
        // shoulder-surfer (someone genuinely close enough to read the screen), filtering out
        // faces that are merely present far in the background.
        val surferCount = areas.count { area ->
            area != primaryArea && area >= primaryArea * SURFER_AREA_RATIO_THRESHOLD
        }

        return FaceAnalysisResult(
            totalFaces = faces.size,
            threatDetected = surferCount > 0
        )
    }

    fun shutdown() {
        detector.close()
    }
}
