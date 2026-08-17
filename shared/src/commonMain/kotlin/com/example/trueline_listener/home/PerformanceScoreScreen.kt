package com.example.trueline_listener.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.theme.*

@Composable
fun PerformanceScoreScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()
    val data = viewModel.scoreData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.closeSubScreen() }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Back to Earnings",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Dark Teal Score Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF235356))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your listener score",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )

                    // Tier Badge Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFEE8B27))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = "Tier",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = data.tier,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${data.score}",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = " /100",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Text(
                    text = data.rank_text,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Metric 1: Repeat Callers
                ScoreProgressBar(
                    label = "Repeat callers (50%)",
                    valueText = "${data.repeat_callers_pct}%",
                    progress = data.repeat_callers_pct / 100f
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Metric 2: Answer Rate
                ScoreProgressBar(
                    label = "Answer rate (30%)",
                    valueText = "${data.answer_rate_pct}%",
                    progress = data.answer_rate_pct / 100f
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Metric 3: Rating
                ScoreProgressBar(
                    label = "Rating (20%)",
                    valueText = "${data.rating_score}",
                    hasStar = true,
                    progress = (data.rating_score / 5.0).toFloat()
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 2. HOW TO MOVE UP Header
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "HOW TO MOVE UP",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tip Cards
        TipCard(
            title = "Be online in peak hours (8 PM – midnight)",
            description = "Most calls happen at night. More online hours in peak = more calls sent your way."
        )

        Spacer(modifier = Modifier.height(10.dp))

        TipCard(
            title = "Answer quickly when you're online",
            description = "Missed calls lower your answer rate. Go offline instead of missing calls."
        )

        Spacer(modifier = Modifier.height(10.dp))

        TipCard(
            title = "Gold listeners earn a higher coin rate",
            description = "Stay Gold for 4 weeks to unlock the Platinum listener tier."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ScoreProgressBar(
    label: String,
    valueText: String,
    hasStar: Boolean = false,
    progress: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valueText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (hasStar) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Star",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
        )
    }
}

@Composable
private fun TipCard(
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.2.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = "Tip",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFD97706)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.5.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
