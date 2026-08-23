package com.example.trueline_listener.onboarding

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Base64
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.trueline_listener.ui.theme.Primary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun CameraPreview(
    modifier: Modifier,
    onPhotoCaptured: (String) -> Unit
) {
    // Legacy implementation
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun FaceVerificationCamera(
    modifier: Modifier,
    viewModel: OnboardingViewModel,
    onPhotoCaptured: (String) -> Unit
) {
    val permissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (permissionState.status.isGranted) {
        CameraPreviewContent(modifier, viewModel, onPhotoCaptured)
    } else {
        Box(
            modifier = modifier
                .background(Color(0xFF0F1B22))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Videocam,
                        contentDescription = "Camera Permission",
                        tint = Primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Camera Access Required",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Enable camera to verify your face and liveness check",
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { permissionState.launchPermissionRequest() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .wrapContentHeight()
                        .defaultMinSize(minHeight = 40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Grant Permission",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
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
                setAnalyzer(analysisExecutor, FaceAnalyzer { data ->
                    viewModel.onFaceInspection(
                        faceCount = data.faceCount,
                        isCentered = data.isFaceCentered,
                        eulerX = data.headEulerX,
                        eulerY = data.headEulerY,
                        eulerZ = data.headEulerZ,
                        leftEyeProb = data.leftEyeOpenProb,
                        rightEyeProb = data.rightEyeOpenProb,
                        luminance = data.averageLuminance,
                        lightingGood = data.isLightingGood,
                        lookingStraight = data.isLookingStraight,
                        qualityPass = data.qualityPass
                    )
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

    fun fileToDataUri(file: File, fallbackInitial: String = "P"): String {
        return try {
            if (file.exists() && file.length() > 0) {
                val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (originalBitmap != null) {
                    val maxDimension = 640
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    val scale = if (width > maxDimension || height > maxDimension) {
                        maxDimension.toFloat() / max(width, height)
                    } else 1.0f

                    val scaledBitmap = if (scale < 1.0f) {
                        Bitmap.createScaledBitmap(
                            originalBitmap,
                            (width * scale).toInt(),
                            (height * scale).toInt(),
                            true
                        )
                    } else {
                        originalBitmap
                    }

                    val outStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                    val base64 = Base64.encodeToString(outStream.toByteArray(), Base64.NO_WRAP)
                    "data:image/jpeg;base64,$base64"
                } else {
                    val bytes = file.readBytes()
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    "data:image/jpeg;base64,$base64"
                }
            } else {
                generateDemoSelfieDataUri(fallbackInitial)
            }
        } catch (e: Exception) {
            Log.e("CameraPreview", "Failed to encode photo file: ${e.message}")
            generateDemoSelfieDataUri(fallbackInitial)
        }
    }

    // Manual capture when user taps Take Selfie
    LaunchedEffect(viewModel.triggerCaptureToken) {
        if (viewModel.triggerCaptureToken > 0) {
            val file = File(context.cacheDir, "selfie_photo_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val path = fileToDataUri(file, viewModel.fullName.take(1).ifBlank { "P" })
                        viewModel.onSelfieCaptured(path)
                        onPhotoCaptured(path)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraPreview", "Manual capture failed, using synthesized portrait", exception)
                        val path = generateDemoSelfieDataUri(viewModel.fullName.take(1).ifBlank { "P" })
                        viewModel.onSelfieCaptured(path)
                        onPhotoCaptured(path)
                    }
                }
            )
        }
    }

    // Automated capture when verification is successful
    LaunchedEffect(viewModel.faceVerificationStatus) {
        if (viewModel.faceVerificationStatus == OnboardingViewModel.FaceVerificationStatus.SUCCESS) {
            val file = File(context.cacheDir, "selfie_verified_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val path = fileToDataUri(file, viewModel.fullName.take(1).ifBlank { "P" })
                        viewModel.onSelfieCaptured(path)
                        onPhotoCaptured(path)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraPreview", "Auto photo capture failed, retaining manual selfie", exception)
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

private fun generateDemoSelfieDataUri(initial: String = "P"): String {
    val bitmap = Bitmap.createBitmap(360, 480, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Background Dark Teal
    paint.color = android.graphics.Color.parseColor("#122A30")
    canvas.drawRect(0f, 0f, 360f, 480f, paint)

    // Head circle
    paint.color = android.graphics.Color.parseColor("#6DA2C2")
    canvas.drawCircle(180f, 190f, 90f, paint)

    // Shoulders
    paint.color = android.graphics.Color.parseColor("#2D6A6B")
    val rectF = RectF(50f, 310f, 310f, 530f)
    canvas.drawOval(rectF, paint)

    // Initial Letter
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 80f
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(initial.take(1).uppercase(), 180f, 218f, paint)

    val outStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
    return "data:image/jpeg;base64," + Base64.encodeToString(outStream.toByteArray(), Base64.NO_WRAP)
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, ContextCompat.getMainExecutor(this))
    }
}
