package com.example.trueline_listener.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.OnboardingProgressHeader
import com.example.trueline_listener.ui.theme.*

@Composable
fun FaceVerificationScreen(viewModel: OnboardingViewModel) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Light
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                OnboardingProgressHeader(
                    currentStep = 4,
                    totalSteps = 7,
                    titleNormal = when (viewModel.faceStepPhase) {
                        OnboardingViewModel.FaceStepPhase.CAPTURE_SELFIE -> "Capture your"
                        OnboardingViewModel.FaceStepPhase.REVIEW_SELFIE -> "Review your"
                        else -> "Live"
                    },
                    titleHighlight = when (viewModel.faceStepPhase) {
                        OnboardingViewModel.FaceStepPhase.CAPTURE_SELFIE,
                        OnboardingViewModel.FaceStepPhase.REVIEW_SELFIE -> "selfie photo"
                        else -> "face check"
                    },
                    subtitle = when (viewModel.faceStepPhase) {
                        OnboardingViewModel.FaceStepPhase.CAPTURE_SELFIE -> "Position your face clearly. Checked against your KYC ID."
                        OnboardingViewModel.FaceStepPhase.REVIEW_SELFIE -> "Ensure your face is clearly visible with good lighting."
                        else -> "Perform the real movement actions on screen."
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Live Camera Viewport
                Box(
                    modifier = Modifier
                        .width(250.dp)
                        .height(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF0F1B22))
                        .border(
                            width = 3.dp,
                            color = when {
                                viewModel.faceStepPhase == OnboardingViewModel.FaceStepPhase.REVIEW_SELFIE -> OnlineSuccess
                                viewModel.faceVerificationStatus == OnboardingViewModel.FaceVerificationStatus.SUCCESS -> OnlineSuccess
                                viewModel.liveQualityPass -> OnlineSuccess
                                else -> Primary
                            },
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    FaceVerificationCamera(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = viewModel,
                        onPhotoCaptured = { uri ->
                            viewModel.onSelfieCaptured(uri)
                        }
                    )

                    if (viewModel.faceStepPhase == OnboardingViewModel.FaceStepPhase.REVIEW_SELFIE) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xD90F1B22)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(OnlineSuccess.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✓", fontSize = 28.sp, color = OnlineSuccess, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Selfie Snapshot Ready",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Tap below to begin liveness test",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Phase-Specific Information & Live Feedback
                when (viewModel.faceStepPhase) {
                    OnboardingViewModel.FaceStepPhase.CAPTURE_SELFIE -> {
                        // Live Quality Indicators Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.2.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "REAL-TIME QUALITY DETECTION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 0.6.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Indicator 1: Face Presence
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                ) {
                                    val ok = viewModel.liveFaceCount == 1
                                    Text(if (ok) "✓" else "○", color = if (ok) OnlineSuccess else TextMuted, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (viewModel.liveFaceCount == 0) "No face detected in frame" else if (viewModel.liveFaceCount > 1) "Multiple faces detected" else "Single face in portrait frame",
                                        fontSize = 13.sp,
                                        color = if (ok) TextPrimary else TextSecondary,
                                        fontWeight = if (ok) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }

                                // Indicator 2: Lighting
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                ) {
                                    val ok = viewModel.liveLightingGood
                                    Text(if (ok) "✓" else "○", color = if (ok) OnlineSuccess else TextMuted, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (ok) "Even, balanced lighting" else "Adjust room lighting / face camera",
                                        fontSize = 13.sp,
                                        color = if (ok) TextPrimary else TextSecondary,
                                        fontWeight = if (ok) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }

                                // Indicator 3: Straight Angle
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                ) {
                                    val ok = viewModel.liveLookingStraight
                                    Text(if (ok) "✓" else "○", color = if (ok) OnlineSuccess else TextMuted, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (ok) "Direct portrait eye-level" else "Look directly at the front camera",
                                        fontSize = 13.sp,
                                        color = if (ok) TextPrimary else TextSecondary,
                                        fontWeight = if (ok) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    OnboardingViewModel.FaceStepPhase.REVIEW_SELFIE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE2F6F3))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "SELFIE QUALITY CONFIRMED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnlineSuccess,
                                    letterSpacing = 0.6.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✓", color = OnlineSuccess, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("High-resolution facial snapshot", fontSize = 13.sp, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✓", color = OnlineSuccess, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Unobstructed portrait view", fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    else -> {
                        // Real Interactive Liveness Challenge Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE2EFF1))
                                .padding(vertical = 14.dp, horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (viewModel.livenessPhase) {
                                        OnboardingViewModel.LivenessPhase.HOLD_CENTER -> "1. Center & Hold Still"
                                        OnboardingViewModel.LivenessPhase.BLINK -> "2. Blink Both Eyes"
                                        OnboardingViewModel.LivenessPhase.TURN_LEFT -> "3. Turn Head Left"
                                        OnboardingViewModel.LivenessPhase.TURN_RIGHT -> "4. Turn Head Right"
                                        OnboardingViewModel.LivenessPhase.COMPLETE -> "Verified Successfully! ✓"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewModel.livenessPhase == OnboardingViewModel.LivenessPhase.COMPLETE) OnlineSuccess else Primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = viewModel.livenessPrompt,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                if (viewModel.livenessPhase != OnboardingViewModel.LivenessPhase.COMPLETE && viewModel.liveLivenessProgress > 0f) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { viewModel.liveLivenessProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Accent,
                                        trackColor = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4-Step Challenge Indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isCenterDone = viewModel.livenessPhase != OnboardingViewModel.LivenessPhase.HOLD_CENTER
                            val isBlinkDone = viewModel.blinkDetected || viewModel.livenessPhase in listOf(
                                OnboardingViewModel.LivenessPhase.TURN_LEFT,
                                OnboardingViewModel.LivenessPhase.TURN_RIGHT,
                                OnboardingViewModel.LivenessPhase.COMPLETE
                            )
                            val isTurnLeftDone = viewModel.headTurnLeftDetected || viewModel.livenessPhase in listOf(
                                OnboardingViewModel.LivenessPhase.TURN_RIGHT,
                                OnboardingViewModel.LivenessPhase.COMPLETE
                            )
                            val isTurnRightDone = viewModel.headTurnRightDetected || viewModel.livenessPhase == OnboardingViewModel.LivenessPhase.COMPLETE

                            // 1. Center
                            StepBadge(
                                title = "Center",
                                isDone = isCenterDone,
                                isActive = viewModel.livenessPhase == OnboardingViewModel.LivenessPhase.HOLD_CENTER
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // 2. Blink 3x
                            StepBadge(
                                title = if (viewModel.livenessPhase == OnboardingViewModel.LivenessPhase.BLINK) "Blink ${viewModel.blinkCount}/3" else "Blink 3x",
                                isDone = isBlinkDone,
                                isActive = viewModel.livenessPhase == OnboardingViewModel.LivenessPhase.BLINK
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // 3. Left
                            StepBadge(
                                title = "Left",
                                isDone = isTurnLeftDone,
                                isActive = viewModel.livenessPhase == OnboardingViewModel.LivenessPhase.TURN_LEFT
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // 4. Right
                            StepBadge(
                                title = "Right",
                                isDone = isTurnRightDone,
                                isActive = viewModel.livenessPhase == OnboardingViewModel.LivenessPhase.TURN_RIGHT
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom CTA Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (viewModel.faceStepPhase) {
                    OnboardingViewModel.FaceStepPhase.CAPTURE_SELFIE -> {
                        val canTakeSelfie = viewModel.liveFaceCount == 1 && viewModel.liveLookingStraight
                        Button(
                            onClick = { viewModel.takeSelfiePhoto() },
                            enabled = canTakeSelfie,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                disabledContainerColor = Color(0xFFCADFE1)
                            )
                        ) {
                            Text(
                                text = when {
                                    viewModel.liveFaceCount == 0 -> "Position Face In Camera"
                                    viewModel.liveFaceCount > 1 -> "Single Person Only"
                                    !viewModel.liveLookingStraight -> "Look Straight Ahead"
                                    else -> "📸 Take Selfie Photo"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canTakeSelfie) Color.White else Color(0xFF5F7577)
                            )
                        }
                    }

                    OnboardingViewModel.FaceStepPhase.REVIEW_SELFIE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.retakeSelfie() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, BorderSubtle)
                            ) {
                                Text("Retake", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Button(
                                onClick = { viewModel.confirmSelfieAndStartLiveness() },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text("Start Liveness Check", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    else -> {
                        if (viewModel.faceVerificationStatus == OnboardingViewModel.FaceVerificationStatus.CAPTURING) {
                            Text(
                                text = "Evaluating live ML vision feed in real-time...",
                                fontSize = 12.5.sp,
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StepBadge(title: String, isDone: Boolean, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> OnlineSuccess
                        isActive -> Accent
                        else -> BorderSubtle
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isDone) "✓" else "•",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 10.5.sp,
            color = if (isDone || isActive) TextPrimary else TextMuted,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}
