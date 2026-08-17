package com.example.trueline_listener.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.network.MilestoneItem
import com.example.trueline_listener.ui.theme.*

@Composable
fun HomeMilestonesScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()
    val data = viewModel.milestonesData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Welcome, ${data.listener_name.ifBlank { "Listener" }}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.WavingHand,
                        contentDescription = "Welcome",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Complete these steps to start earning. Bonuses are paid with your weekly payout.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Milestones List
        data.milestones.forEachIndexed { index, milestone ->
            MilestoneCard(index = index + 1, item = milestone)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Week-One Earnings Guarantee Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFE2F3F3))
                .border(1.dp, Color(0xFFC3E4E4), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = "Guarantee",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Week-one earnings guarantee",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Stay online 3 hours a day for 7 days and you earn at least ₹1,500 — even if calls are slow. We take the risk, not you.",
                    fontSize = 12.5.sp,
                    color = TextPrimary.copy(alpha = 0.85f),
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Switch to Live Dashboard Button
        OutlinedButton(
            onClick = { viewModel.toggleMilestonesView(false) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Go to Live Dashboard",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Go",
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MilestoneCard(index: Int, item: MilestoneItem) {
    val isCompleted = item.is_completed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.2.dp, if (isCompleted) Color(0xFFD4EDE8) else BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step Number or Checkmark Circle
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> OnlineSuccess
                            index == 2 -> Accent
                            else -> Color(0xFFE2E8F0)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "$index",
                        color = if (index == 2) Color.White else TextMuted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Milestone Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = if (isCompleted) TextMuted else TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Bonus Amount
            Text(
                text = "+₹${item.reward_coins.toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) OnlineSuccess else Accent
            )
        }
    }
}
