package com.example.trueline_listener.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.theme.*

@Composable
fun PhoneInputScreen(
    viewModel: OnboardingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Light)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "TrueLine Listener",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Earn from home by taking voice calls",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                color = Dark
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(60.dp))

        OutlinedTextField(
            value = viewModel.phoneNumber,
            onValueChange = { viewModel.onPhoneNumberChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mobile Number") },
            prefix = { Text("+91 ", color = Dark) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextMutedGrey
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.sendOtp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = viewModel.isPhoneValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                disabledContainerColor = Accent.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Send OTP",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "By continuing, you agree to the TrueLine Partner Terms. 18+ only.",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                color = TextResendMuted
            ),
            textAlign = TextAlign.Center
        )
    }
}
