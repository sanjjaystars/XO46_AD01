package com.example.shouldersurfingprotection

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.shouldersurfingprotection.data.ProtectionRepository
import com.example.shouldersurfingprotection.ui.AdaptiveScaffold
import com.example.shouldersurfingprotection.ui.MainViewModel
import com.example.shouldersurfingprotection.ui.navigation.MainNavKey
import com.example.shouldersurfingprotection.ui.screens.MainScreen
import com.example.shouldersurfingprotection.ui.screens.SettingsScreen
import com.example.shouldersurfingprotection.ui.theme.ShoulderSurfingProtectionTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            ShoulderSurfingProtectionTheme {
                val faceAnalyzer = remember { FaceAnalyzer() }
                val repository = remember { ProtectionRepository(faceAnalyzer) }
                
                // Manual ViewModel creation for simplicity
                val viewModel: MainViewModel = viewModel { MainViewModel(repository) }
                
                val backstack = remember { mutableStateListOf<MainNavKey>(MainNavKey.Home) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraPermissionGateway(
                        onPermissionGranted = {
                            val isEnabled by viewModel.isProtectionEnabled.collectAsState()
                            val isDetected by viewModel.isSurferDetected.collectAsState(initial = false)

                            Box(modifier = Modifier.fillMaxSize()) {
                                // Background camera processing
                                Box(modifier = Modifier.size(1.dp)) {
                                    CameraScreen(cameraExecutor, faceAnalyzer)
                                }

                                AdaptiveScaffold(backstack = backstack) {
                                    NavDisplay(
                                        backStack = backstack,
                                        onBack = { if (backstack.size > 1) backstack.removeLast() },
                                        entryProvider = { key ->
                                            when (key) {
                                                is MainNavKey.Home -> NavEntry(key) { MainScreen(viewModel) }
                                                is MainNavKey.Settings -> NavEntry(key) { SettingsScreen() }
                                                else -> error("Unknown key: $key")
                                            }
                                        }
                                    )
                                }

                                // Top-level Privacy Overlay
                                PrivacyOverlay(isVisible = isEnabled && isDetected)
                            }
                        },
                        onPermissionDenied = { shouldShowRationale ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (shouldShowRationale)
                                        "Camera permission is required for face detection."
                                    else
                                        "Camera permission denied. Please enable it in settings."
                                )
                            }
                        }
                    )
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
fun CameraScreen(cameraExecutor: ExecutorService, faceAnalyzer: FaceAnalyzer) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    ) { view ->
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = view.surfaceProvider
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, faceAnalyzer)
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("MainActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
