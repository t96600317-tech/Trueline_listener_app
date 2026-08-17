package com.example.trueline_listener.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.trueline_listener.ui.OnboardingProgressHeader
import com.example.trueline_listener.ui.TrueLineBrandHeader
import com.example.trueline_listener.ui.TrueLineWaveformLoader
import com.example.trueline_listener.ui.theme.*

@Composable
fun PhoneInputScreen(viewModel: OnboardingViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
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
                TrueLineBrandHeader(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    logoSize = 52.dp,
                    titleSize = 24.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                OnboardingProgressHeader(
                    currentStep = 1,
                    totalSteps = 7,
                    titleNormal = "Become a",
                    titleHighlight = "Verified Listener",
                    subtitle = "Join India's empathetic voice network. Earn per minute while helping people through compassionate conversations."
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "MOBILE NUMBER",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.5.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+91",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(BorderSubtle)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    TextField(
                        value = viewModel.phoneNumber,
                        onValueChange = { viewModel.onPhoneNumberChanged(it) },
                        placeholder = {
                            Text(
                                text = "98765 43210",
                                color = TextMuted,
                                fontSize = 17.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Primary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = Danger,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { viewModel.requestOtp() },
                    enabled = viewModel.isPhoneValid && !viewModel.isLoading,
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
                            text = "Get OTP",
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
