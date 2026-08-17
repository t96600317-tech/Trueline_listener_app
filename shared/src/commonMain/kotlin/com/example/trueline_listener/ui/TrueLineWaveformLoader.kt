package com.example.trueline_listener.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.theme.Accent
import com.example.trueline_listener.ui.theme.Secondary
import com.example.trueline_listener.ui.theme.TextSecondary

@Composable
fun TrueLineWaveformLoader(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    barColor: Color = Secondary,
    accentColor: Color = Accent
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformLoaderTransition")

    // 4 Oscillating Soundbars with staggered sine-wave timing
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar1Scale"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, delayMillis = 100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar2Scale"
    )

    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar3Scale"
    )

    val scale4 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 580, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar4Scale"
    )

    val barWidth = size * 0.16f
    val cornerRadius = barWidth / 2

    Row(
        modifier = modifier
            .height(size)
            .width(size * 1.4f),
        horizontalArrangement = Arrangement.spacedBy(size * 0.14f, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bar 1 (Left - Sky Blue)
        Box(
            modifier = Modifier
                .width(barWidth)
                .fillMaxHeight(scale1)
                .clip(RoundedCornerShape(cornerRadius))
                .background(barColor)
        )

        // Bar 2 (Mid-Left - Sky Blue)
        Box(
            modifier = Modifier
                .width(barWidth)
                .fillMaxHeight(scale2)
                .clip(RoundedCornerShape(cornerRadius))
                .background(barColor)
        )

        // Bar 3 (Center - Amber Accent)
        Box(
            modifier = Modifier
                .width(barWidth * 1.1f)
                .fillMaxHeight(scale3)
                .clip(RoundedCornerShape(cornerRadius))
                .background(accentColor)
        )

        // Bar 4 (Right - Sky Blue)
        Box(
            modifier = Modifier
                .width(barWidth)
                .fillMaxHeight(scale4)
                .clip(RoundedCornerShape(cornerRadius))
                .background(barColor)
        )
    }
}

@Composable
fun FullScreenWaveformLoader(
    message: String = "Processing request...",
    subMessage: String = "Please wait a moment"
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.94f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TrueLineWaveformLoader(size = 56.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = com.example.trueline_listener.ui.theme.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subMessage,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}
