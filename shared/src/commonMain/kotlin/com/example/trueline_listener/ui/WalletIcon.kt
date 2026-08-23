package com.example.trueline_listener.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WalletIcon(
    modifier: Modifier = Modifier.size(24.dp),
    isSelected: Boolean = false
) {
    val mainColor = if (isSelected) Color(0xFF134E4A) else Color(0xFF94A3B8)
    val cardBack = if (isSelected) Color(0xFF0F766E) else Color(0xFF64748B)
    val cardFront = if (isSelected) Color(0xFF2DD4BF) else Color(0xFFCBD5E1)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Top Card 1 (Back Card)
        val path1 = Path().apply {
            moveTo(w * 0.25f, h * 0.26f)
            lineTo(w * 0.65f, h * 0.08f)
            lineTo(w * 0.68f, h * 0.17f)
            lineTo(w * 0.28f, h * 0.35f)
            close()
        }
        drawPath(path1, color = cardBack)

        // 2. Top Card 2 (Front Card)
        val path2 = Path().apply {
            moveTo(w * 0.30f, h * 0.27f)
            lineTo(w * 0.77f, h * 0.15f)
            lineTo(w * 0.77f, h * 0.28f)
            lineTo(w * 0.30f, h * 0.40f)
            close()
        }
        drawPath(path2, color = cardFront)

        // 3. Main Wallet Body
        val walletTop = h * 0.28f
        val walletHeight = h * 0.68f
        val walletWidth = w * 0.90f

        drawRoundRect(
            color = mainColor,
            topLeft = Offset(0f, walletTop),
            size = Size(walletWidth, walletHeight),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
        )

        // 4. Snap Clasp (Right Tab)
        val claspTop = h * 0.51f
        val claspHeight = h * 0.26f
        val claspWidth = w * 0.26f
        val claspLeft = w * 0.70f

        drawRoundRect(
            color = mainColor,
            topLeft = Offset(claspLeft, claspTop),
            size = Size(claspWidth, claspHeight),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )

        // Clasp border outline
        drawRoundRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = Offset(claspLeft, claspTop),
            size = Size(claspWidth, claspHeight),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.04f)
        )

        // White dot inside clasp button
        drawCircle(
            color = Color.White,
            radius = w * 0.045f,
            center = Offset(claspLeft + claspWidth * 0.5f, claspTop + claspHeight * 0.5f)
        )
    }
}
