package com.example.trueline_listener.onboarding

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(
    private val onFaceDetected: (
        isCentered: Boolean,
        headEulerY: Float,
        leftEyeOpenProb: Float?,
        rightEyeOpenProb: Float?
    ) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0]
                        val isCentered = isFaceCentered(face, imageProxy.width, imageProxy.height)
                        onFaceDetected(
                            isCentered,
                            face.headEulerAngleY,
                            face.leftEyeOpenProbability,
                            face.rightEyeOpenProbability
                        )
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun isFaceCentered(face: com.google.mlkit.vision.face.Face, width: Int, height: Int): Boolean {
        val bounds = face.boundingBox
        val faceCenterX = bounds.centerX()
        val faceCenterY = bounds.centerY()
        val screenCenterX = width / 2
        val screenCenterY = height / 2
        
        val toleranceX = width * 0.15f
        val toleranceY = height * 0.15f
        
        return Math.abs(faceCenterX - screenCenterX) < toleranceX &&
               Math.abs(faceCenterY - screenCenterY) < toleranceY
    }
}
