package com.example.trueline_listener.onboarding

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
fun FaceRetryScreen(viewModel: OnboardingViewModel) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Light
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Refresh Circular Badge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFBF1E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔄", fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(28.dp))

                OnboardingProgressHeader(
                    currentStep = 4,
                    totalSteps = 7,
                    titleNormal = "We need",
                    titleHighlight = "one more try",
                    subtitle = "Your face check didn't match your ID clearly — usually a lighting issue, not a problem with you."
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Tips Card (matching Image 8)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "TIPS FOR NEXT TIME",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "• Face a window or bright light\n• Remove sunglasses or a cap\n• Hold the phone at eye level\n• Make sure your whole face fits the oval",
                            fontSize = 14.sp,
                            lineHeight = 24.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Retry CTA
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { viewModel.proceedFromFaceRetry() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text(
                        text = "Try face check again",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Still stuck? WhatsApp listener support and a person will help you.",
                    fontSize = 12.5.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
