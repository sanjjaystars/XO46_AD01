package com.example.shouldersurfing

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shouldersurfing.ui.ProtectionOverlay
import com.example.shouldersurfing.ui.SensitiveContentScreen
import com.example.shouldersurfing.ui.StatusBar
import com.example.shouldersurfing.ui.theme.ShoulderSurfingTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private val viewModel: ProtectionViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            cameraPermissionGranted.value = granted
        }

    private val cameraPermissionGranted = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraPermissionGranted.value = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        setContent {
            ShoulderSurfingTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val granted by cameraPermissionGranted

                    if (granted) {
                        ProtectedAppScreen(
                            viewModel = viewModel,
                            cameraExecutor = cameraExecutor
                        )
                    } else {
                        CameraPermissionRationale(
                            onRequestPermission = {
                                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
private fun CameraPermissionRationale(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Camera access needed",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "This app uses your front camera to detect when someone else is looking at " +
                "your screen, so it can automatically hide sensitive content. Camera frames " +
                "are analyzed in memory only and are never saved or uploaded.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant camera permission")
        }
    }
}

/**
 * Binds CameraX (front camera + ImageAnalysis running the FaceAnalyzer) to this composable's
 * lifecycle, and renders the demo sensitive-content screen with the protection overlay on top.
 */
@Composable
private fun ProtectedAppScreen(
    viewModel: ProtectionViewModel,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Bind / unbind the camera use cases to this composable's lifecycle. No Preview use case is
    // bound because the front camera feed itself never needs to be shown to the user -- only
    // used internally for face analysis -- which also avoids ever rendering a "selfie" on screen.
    DisposableEffect(lifecycleOwner, uiState.isProtectionEnabled) {
        var cameraProvider: ProcessCameraProvider? = null

        if (uiState.isProtectionEnabled) {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                cameraProvider = providerFuture.get()

                val imageAnalysis = ImageAnalysis.Builder()
                    // Only the newest frame matters for a live "is someone watching right now"
                    // check -- older, unanalyzed frames are dropped instead of queued.
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(cameraExecutor, viewModel.getAnalyzer()) }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    // Camera not available (e.g. emulator without front camera) -- fail quietly,
                    // the app still functions, it simply won't receive face-analysis callbacks.
                }
            }, ContextCompat.getMainExecutor(context))
        }

        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatusBar(state = uiState, onToggle = { viewModel.toggleProtection() })
        }

        ProtectionOverlay(
            hidden = uiState.isContentHidden,
            modifier = Modifier.weight(1f)
        ) {
            SensitiveContentScreen()
        }
    }
}
