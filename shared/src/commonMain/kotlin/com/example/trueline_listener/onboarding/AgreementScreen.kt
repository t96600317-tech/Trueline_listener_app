package com.example.trueline_listener.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.OnboardingProgressHeader
import com.example.trueline_listener.ui.TrueLineWaveformLoader
import com.example.trueline_listener.ui.theme.*

@Composable
fun AgreementScreen(viewModel: OnboardingViewModel) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Light
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                OnboardingProgressHeader(
                    currentStep = 6,
                    totalSteps = 7,
                    titleNormal = "Your",
                    titleHighlight = "agreement"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Agreement Clauses Card (matching Image 5)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("1. Your role.", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "You join TrueLine as an independent partner. This is not employment — you choose your own hours and are free to work elsewhere.",
                                fontSize = 13.5.sp,
                                color = TextSecondary,
                                lineHeight = 19.sp
                            )
                        }

                        Column {
                            Text("2. What you earn.", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "₹1.14 per minute of connected call time. Gifts received are credited separately.",
                                fontSize = 13.5.sp,
                                color = TextSecondary,
                                lineHeight = 19.sp
                            )
                        }

                        Column {
                            Text("3. Payouts.", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Weekly to your UPI, within 24 hours of request. TDS deducted as required by law.",
                                fontSize = 13.5.sp,
                                color = TextSecondary,
                                lineHeight = 19.sp
                            )
                        }

                        Column {
                            Text("4. Your privacy.", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Your real name, photo, and personal contact info are never shared with callers.",
                                fontSize = 13.5.sp,
                                color = TextSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Checkbox Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleAgreement(!viewModel.agreementAccepted) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viewModel.agreementAccepted) Primary else Color.White)
                            .border(
                                1.5.dp,
                                if (viewModel.agreementAccepted) Primary else BorderSubtle,
                                RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.agreementAccepted) {
                            Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "I have read and accept the Partner Agreement and Privacy Policy.",
                        fontSize = 13.5.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }

                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = Danger,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Submit CTA
            Button(
                onClick = { viewModel.submitAgreementAndFinalOnboarding() },
                enabled = viewModel.agreementAccepted && !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.4f)
                )
            ) {
                if (viewModel.isLoading) {
                    TrueLineWaveformLoader(size = 24.dp, barColor = Color.White, accentColor = Accent)
                } else {
                    Text(
                        text = "Submit for approval",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
