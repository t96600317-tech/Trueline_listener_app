package com.example.trueline_listener.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.example.trueline_listener.ui.theme.*

@Composable
fun OtpVerificationScreen(
    viewModel: OnboardingViewModel
) {
    val resendTimer by viewModel.resendTimer.collectAsState()
    val otp = viewModel.otp
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Light)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Verify Mobile Number",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Dark
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Enter 6-digit OTP sent to",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = TextMutedGrey
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "+91 ${viewModel.phoneNumber.take(5)} ${viewModel.phoneNumber.drop(5)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Dark
                ),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Single Curved OTP Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(SurfaceWhite, RoundedCornerShape(14.dp))
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = if (isFocused) Primary else BorderSubtle,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) viewModel.onOtpChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Dark,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.25.em
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (resendTimer > 0) {
            Text(
                text = "Resend OTP in ${resendTimer}s",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = TextResendMuted
                )
            )
        } else {
            TextButton(onClick = { viewModel.resendOtp() }) {
                Text(
                    text = "Resend OTP",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.verifyOtp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = viewModel.isOtpValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                disabledContainerColor = AccentDisabled
            ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Verify & Continue",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(
            onClick = { viewModel.backToPhoneInput() },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Edit Phone Number",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
