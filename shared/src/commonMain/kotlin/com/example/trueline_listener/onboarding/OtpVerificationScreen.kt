package com.example.trueline_listener.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.OnboardingProgressHeader
import com.example.trueline_listener.ui.TrueLineWaveformLoader
import com.example.trueline_listener.ui.theme.*

@Composable
fun OtpVerificationScreen(viewModel: OnboardingViewModel) {
    val resendTimer by viewModel.resendTimer.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            },
        color = Light
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                OnboardingProgressHeader(
                    currentStep = 1,
                    totalSteps = 7,
                    titleNormal = "Verify your",
                    titleHighlight = "mobile number",
                    subtitle = "We've sent a 6-digit code to\n+91 ${viewModel.phoneNumber.ifBlank { "98765 43210" }}"
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "ENTER OTP",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Auto-submits on 6th digit & dismisses keyboard
                BasicTextField(
                    value = viewModel.otp,
                    onValueChange = { input ->
                        val sanitized = input.filter { it.isDigit() }.take(6)
                        viewModel.onOtpChanged(sanitized)
                        if (sanitized.length == 6) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.verifyOtp()
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    decorationBox = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (i in 0 until 6) {
                                val digit = viewModel.otp.getOrNull(i)?.toString() ?: ""
                                val isCurrentFocused = (viewModel.otp.length == i) || (i == 5 && viewModel.otp.length == 6)
                                val isFilled = digit.isNotEmpty()

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(
                                            width = if (isCurrentFocused) 2.dp else 1.2.dp,
                                            color = if (isCurrentFocused) Primary else if (isFilled) PrimaryDim else BorderSubtle,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (digit.isNotEmpty()) digit else "-",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (digit.isNotEmpty()) TextPrimary else TextMuted
                                    )
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Resend Countdown / Trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (resendTimer > 0) {
                        val formattedSeconds = if (resendTimer < 10) "0$resendTimer" else "$resendTimer"
                        Text(
                            text = "Resend code in 00:$formattedSeconds",
                            fontSize = 13.5.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Resend OTP",
                            fontSize = 13.5.sp,
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                viewModel.startResendTimer()
                                viewModel.requestOtp()
                            }
                        )
                    }
                }

                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = Danger,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.verifyOtp()
                    },
                    enabled = viewModel.isOtpValid && !viewModel.isLoading,
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
                            text = "Verify & continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Your number is never shown to users on TrueLine.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
