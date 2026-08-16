package com.example.trueline_listener.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.theme.*

@Composable
fun FaceVerificationScreen(
    viewModel: OnboardingViewModel
) {
    val status = viewModel.faceVerificationStatus
    val prompt = viewModel.livenessPrompt
    val retryCount = viewModel.faceRetryCount
    var capturedPhotoUri by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Light)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Face Verification",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Dark
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Confirm it's really you",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = TextMutedGrey
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Camera Container with Oval Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (capturedPhotoUri == null) {
                // Using the specific automated camera component
                FaceVerificationCameraWrapper(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onPhotoCaptured = { uri ->
                        capturedPhotoUri = uri
                    }
                )
            } else {
                // Preview captured photo
                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                    Text("✓ Verification Photo Captured", color = Color.White)
                }
            }

            // Oval Overlay Guide
            Box(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .border(
                        width = 2.dp,
                        color = when (status) {
                            OnboardingViewModel.FaceVerificationStatus.SUCCESS -> OnlineSuccess
                            OnboardingViewModel.FaceVerificationStatus.FAILED -> Color.Red
                            else -> Primary.copy(alpha = 0.7f)
                        },
                        shape = RoundedCornerShape(percent = 50)
                    )
            )
            
            if (status == OnboardingViewModel.FaceVerificationStatus.VERIFYING) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Instruction Pill
        Surface(
            color = when (status) {
                OnboardingViewModel.FaceVerificationStatus.SUCCESS -> OnlineSuccess.copy(alpha = 0.1f)
                OnboardingViewModel.FaceVerificationStatus.FAILED -> Color.Red.copy(alpha = 0.1f)
                else -> Primary.copy(alpha = 0.1f)
            },
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = prompt,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = when (status) {
                        OnboardingViewModel.FaceVerificationStatus.SUCCESS -> OnlineSuccess
                        OnboardingViewModel.FaceVerificationStatus.FAILED -> Color.Red
                        else -> Primary
                    }
                ),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        when (status) {
            OnboardingViewModel.FaceVerificationStatus.IDLE -> {
                Button(
                    onClick = { viewModel.startFaceVerification() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Start Check", fontWeight = FontWeight.Bold)
                }
            }
            OnboardingViewModel.FaceVerificationStatus.FAILED -> {
                Button(
                    onClick = { 
                        capturedPhotoUri = null
                        viewModel.retryFaceVerification() 
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Retry (${3 - retryCount} left)", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            OnboardingViewModel.FaceVerificationStatus.SUCCESS, OnboardingViewModel.FaceVerificationStatus.MANUAL_REVIEW -> {
                Button(
                    onClick = { viewModel.proceedFromFaceVerification() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            else -> {
                // Automated flow is running
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
expect fun FaceVerificationCameraWrapper(
    modifier: Modifier,
    viewModel: OnboardingViewModel,
    onPhotoCaptured: (String) -> Unit
)
