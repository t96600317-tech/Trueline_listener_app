package com.example.trueline_listener.onboarding

import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.abs

data class FaceInspectionData(
    val faceCount: Int,
    val isFaceCentered: Boolean,
    val faceAreaRatio: Float,
    val headEulerX: Float,
    val headEulerY: Float,
    val headEulerZ: Float,
    val leftEyeOpenProb: Float?,
    val rightEyeOpenProb: Float?,
    val smileProb: Float?,
    val averageLuminance: Float,
    val isLightingGood: Boolean,
    val isLookingStraight: Boolean,
    val qualityPass: Boolean
)

class FaceAnalyzer(
    private val onInspection: (FaceInspectionData) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val luminance = calculateAverageLuminance(mediaImage)
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { faces ->
                val count = faces.size
                if (count == 0) {
                    onInspection(
                        FaceInspectionData(
                            faceCount = 0,
                            isFaceCentered = false,
                            faceAreaRatio = 0f,
                            headEulerX = 0f,
                            headEulerY = 0f,
                            headEulerZ = 0f,
                            leftEyeOpenProb = null,
                            rightEyeOpenProb = null,
                            smileProb = null,
                            averageLuminance = luminance,
                            isLightingGood = luminance in 40f..225f,
                            isLookingStraight = false,
                            qualityPass = false
                        )
                    )
                } else {
                    val face = faces[0]
                    val bounds = face.boundingBox
                    val faceArea = (bounds.width().toLong() * bounds.height().toLong()).toFloat()
                    val totalArea = (imageProxy.width.toLong() * imageProxy.height.toLong()).toFloat()
                    val areaRatio = if (totalArea > 0) faceArea / totalArea else 0f

                    val faceCenterX = bounds.centerX()
                    val faceCenterY = bounds.centerY()
                    val screenCenterX = imageProxy.width / 2
                    val screenCenterY = imageProxy.height / 2
                    val toleranceX = imageProxy.width * 0.25f
                    val toleranceY = imageProxy.height * 0.25f

                    val isPositionCentered = abs(faceCenterX - screenCenterX) < toleranceX && abs(faceCenterY - screenCenterY) < toleranceY
                    val isSizeGood = areaRatio in 0.05f..0.70f
                    val isCentered = isPositionCentered && isSizeGood

                    val eulerX = face.headEulerAngleX
                    val eulerY = face.headEulerAngleY
                    val eulerZ = face.headEulerAngleZ

                    val isLightingGood = luminance in 35f..235f
                    val isLookingStraight = abs(eulerX) < 18f && abs(eulerY) < 18f && abs(eulerZ) < 16f
                    val qualityPass = (count == 1) && isCentered && isLightingGood && isLookingStraight

                    onInspection(
                        FaceInspectionData(
                            faceCount = count,
                            isFaceCentered = isCentered,
                            faceAreaRatio = areaRatio,
                            headEulerX = eulerX,
                            headEulerY = eulerY,
                            headEulerZ = eulerZ,
                            leftEyeOpenProb = face.leftEyeOpenProbability,
                            rightEyeOpenProb = face.rightEyeOpenProbability,
                            smileProb = face.smilingProbability,
                            averageLuminance = luminance,
                            isLightingGood = isLightingGood,
                            isLookingStraight = isLookingStraight,
                            qualityPass = qualityPass
                        )
                    )
                }
            }
            .addOnFailureListener {
                // Ignore transient frame analysis errors
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun calculateAverageLuminance(mediaImage: Image): Float {
        return try {
            val plane = mediaImage.planes[0]
            val buffer = plane.buffer
            val remaining = buffer.remaining()
            if (remaining <= 0) return 128f

            var sum = 0L
            var sampleCount = 0
            val step = 48
            var pos = 0
            while (pos < remaining) {
                val b = buffer.get(pos).toInt() and 0xFF
                sum += b
                sampleCount++
                pos += step
            }
            if (sampleCount > 0) sum.toFloat() / sampleCount else 128f
        } catch (e: Exception) {
            128f
        }
    }
}
