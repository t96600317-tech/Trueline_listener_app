package com.example.trueline_listener.onboarding

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.trueline_listener.ui.theme.Primary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun CameraPreview(
    modifier: Modifier,
    onPhotoCaptured: (String) -> Unit
) {
    // Legacy/Generic implementation - could be used elsewhere
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceVerificationCamera(
    modifier: Modifier,
    viewModel: OnboardingViewModel,
    onPhotoCaptured: (String) -> Unit
) {
    val permissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    
    if (permissionState.status.isGranted) {
        CameraPreviewContent(modifier, viewModel, onPhotoCaptured)
    } else {
        Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("Grant Camera Permission")
            }
        }
    }
}

@Composable
private fun CameraPreviewContent(
    modifier: Modifier,
    viewModel: OnboardingViewModel,
    onPhotoCaptured: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    val analysisExecutor = remember { ContextCompat.getMainExecutor(context) }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()
            .apply {
                setAnalyzer(analysisExecutor, FaceAnalyzer { isCentered, eulerY, leftEye, rightEye ->
                    viewModel.onFaceDetected(isCentered, eulerY, leftEye, rightEye)
                })
            }
    }

    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )
        } catch (e: Exception) {
            Log.e("CameraPreview", "Use case binding failed", e)
        }
    }

    // Automated capture when verification is successful
    LaunchedEffect(viewModel.faceVerificationStatus) {
        if (viewModel.faceVerificationStatus == OnboardingViewModel.FaceVerificationStatus.SUCCESS) {
            val outputOptions = ImageCapture.OutputFileOptions.Builder(
                File(context.cacheDir, "selfie_verified_${System.currentTimeMillis()}.jpg")
            ).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        onPhotoCaptured(outputFileResults.savedUri.toString())
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraPreview", "Auto photo capture failed", exception)
                    }
                }
            )
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, ContextCompat.getMainExecutor(this))
    }
}
