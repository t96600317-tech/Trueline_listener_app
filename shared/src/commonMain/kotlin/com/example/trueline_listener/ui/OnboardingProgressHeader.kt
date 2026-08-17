package com.example.trueline_listener.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.theme.BorderSubtle
import com.example.trueline_listener.ui.theme.Primary
import com.example.trueline_listener.ui.theme.TextMuted
import com.example.trueline_listener.ui.theme.TextPrimary
import com.example.trueline_listener.ui.theme.TextSecondary

@Composable
fun OnboardingProgressHeader(
    currentStep: Int,
    totalSteps: Int = 7,
    titleNormal: String,
    titleHighlight: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 1. Segmented Progress Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (step in 1..totalSteps) {
                val isActive = step <= currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isActive) Primary else BorderSubtle)
                )
            }
        }

        // 2. Step Indicator Label (e.g. STEP 1 OF 7)
        Text(
            text = "STEP $currentStep OF $totalSteps",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.0.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Two-Tone Heading (e.g. "Verify your mobile number")
        val headingText = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )
            ) {
                append("$titleNormal ")
            }
            withStyle(
                SpanStyle(
                    color = Primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )
            ) {
                append(titleHighlight)
            }
        }

        Text(
            text = headingText,
            lineHeight = 34.sp
        )

        // 4. Subtitle / Helper Instruction
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 14.5.sp,
                lineHeight = 21.sp,
                color = TextSecondary
            )
        }
    }
}
