package com.example.trueline_listener.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.trueline_listener.ui.OnboardingProgressHeader
import com.example.trueline_listener.ui.TrueLineWaveformLoader
import com.example.trueline_listener.ui.theme.*

@Composable
fun KycDocumentScreen(viewModel: OnboardingViewModel) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                OnboardingProgressHeader(
                    currentStep = 5,
                    totalSteps = 7,
                    titleNormal = "Verify your",
                    titleHighlight = "identity & payout",
                    subtitle = "Required by compliance for listener onboarding and TDS payout processing."
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Card 1: PAN Verification Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PAN VERIFICATION (FOR TDS)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.8.sp
                            )
                            if (viewModel.panNumber.length == 10) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE2F6F3))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("Valid PAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnlineSuccess)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // PAN Number Input
                        Text("PAN Number", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = viewModel.panNumber,
                            onValueChange = { viewModel.onPanNumberChanged(it) },
                            placeholder = { Text("e.g. ABCDE1234F", color = TextMuted, fontSize = 15.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                keyboardType = KeyboardType.Ascii
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Light,
                                unfocusedContainerColor = Light,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Primary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Name on PAN Card
                        Text("Name on PAN Card", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = viewModel.panName,
                            onValueChange = { viewModel.onPanNameChanged(it) },
                            placeholder = { Text("As per official PAN record", color = TextMuted, fontSize = 15.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Light,
                                unfocusedContainerColor = Light,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card 2: Payout Destination (UPI or Bank Account)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "EARNINGS PAYOUT METHOD",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Segmented Tab Selector (UPI vs Bank)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEFF5F6))
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (viewModel.payoutType == "upi") Primary else Color.Transparent)
                                    .clickable { viewModel.onPayoutTypeChanged("upi") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "UPI ID (VPA)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewModel.payoutType == "upi") Color.White else TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (viewModel.payoutType == "bank") Primary else Color.Transparent)
                                    .clickable { viewModel.onPayoutTypeChanged("bank") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Bank Account",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewModel.payoutType == "bank") Color.White else TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (viewModel.payoutType == "upi") {
                            // UPI ID Input
                            Text("UPI ID / VPA", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = viewModel.upiId,
                                onValueChange = { viewModel.onUpiIdChanged(it) },
                                placeholder = { Text("e.g. mobile@paytm or name@okaxis", color = TextMuted, fontSize = 15.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Light,
                                    unfocusedContainerColor = Light,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = Primary
                                )
                            )
                        } else {
                            // Bank Account Number
                            Text("Account Number", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = viewModel.accountNumber,
                                onValueChange = { viewModel.onAccountNumberChanged(it) },
                                placeholder = { Text("9 to 18 digit account number", color = TextMuted, fontSize = 15.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Light,
                                    unfocusedContainerColor = Light,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = Primary
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // IFSC Code
                            Text("IFSC Code", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = viewModel.ifscCode,
                                onValueChange = { viewModel.onIfscCodeChanged(it) },
                                placeholder = { Text("e.g. HDFC0001234", color = TextMuted, fontSize = 15.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    keyboardType = KeyboardType.Ascii
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Light,
                                    unfocusedContainerColor = Light,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = Primary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Privacy Guarantee Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE2F4F2))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "🔒 Privacy & Security Guarantee",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your financial credentials are encrypted with 256-bit AES and used solely for payouts. Raw document images are never stored.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = TextSecondary
                        )
                    }
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

                Spacer(modifier = Modifier.height(100.dp))
            }

            // Bottom CTA
            Button(
                onClick = { viewModel.submitKycStep() },
                enabled = viewModel.isKYCValid && !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
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
                        text = "Verify & Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
