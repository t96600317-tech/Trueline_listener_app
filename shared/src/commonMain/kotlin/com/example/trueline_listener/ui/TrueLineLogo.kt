package com.example.trueline_listener.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.theme.Accent
import com.example.trueline_listener.ui.theme.Primary
import com.example.trueline_listener.ui.theme.TextPrimary

@Composable
fun TrueLineLogoBadge(
    size: Dp = 48.dp,
    isDarkTheme: Boolean = false
) {
    val badgeBg = if (isDarkTheme) Color(0xFF1E3F43) else Color(0xFFEEF5F6)
    val outerRing = if (isDarkTheme) Color(0x33FFFFFF) else Color(0xFFD3E4E6)
    val tealBarColor = if (isDarkTheme) Color.White else Color(0xFF245255)
    val yellowBarColor = Color(0xFFE88F38) // Warm Amber-Yellow from brand logo

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(badgeBg)
            .border(
                width = (size * 0.045f).coerceAtLeast(1.2.dp),
                color = outerRing,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        val barWidth = (size * 0.088f).coerceAtLeast(2.4.dp)
        val cornerRadius = barWidth / 2

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = size * 0.16f),
            horizontalArrangement = Arrangement.spacedBy(size * 0.06f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Line 1: Teal (Short dot/pill)
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(0.26f)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(tealBarColor)
            )

            // Line 2: Teal (Medium-Tall)
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(0.62f)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(tealBarColor)
            )

            // Line 3: Yellow/Amber (Tallest)
            Box(
                modifier = Modifier
                    .width(barWidth * 1.05f)
                    .fillMaxHeight(0.88f)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(yellowBarColor)
            )

            // Line 4: Teal (Medium)
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(0.48f)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(tealBarColor)
            )
        }
    }
}

@Composable
fun TrueLineLogo(
    size: Dp = 48.dp,
    withRings: Boolean = true,
    ringColor: Color = Color(0xFFCADFE1)
) {
    TrueLineLogoBadge(size = size, isDarkTheme = false)
}

@Composable
fun TrueLineBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 38.dp,
    titleSize: androidx.compose.ui.unit.TextUnit = 28.sp,
    showSubtitle: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TrueLineLogoBadge(size = logoSize, isDarkTheme = false)
        Spacer(modifier = Modifier.width(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "True",
                fontSize = titleSize,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Line",
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF286366),
                letterSpacing = (-0.5).sp
            )
        }
    }
}
