package com.example.trueline_listener.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import com.example.trueline_listener.network.RecentCallItem
import com.example.trueline_listener.ui.theme.*

@Composable
fun HomeDashboardScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()
    val data = viewModel.dashboardData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Dark Teal Stats Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF235356))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Today's earnings",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "₹${data.today_earnings_coins.toInt()}",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${data.today_minutes} minutes of listening · ${data.today_calls} calls",
                    fontSize = 12.5.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3 Mini Stat Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // This week
                    StatSubCard(
                        modifier = Modifier.weight(1f),
                        value = "₹${data.this_week_earnings_coins.toInt()}",
                        label = "This week"
                    )
                    // Rating
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${data.rating_avg}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "Rating",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFBBF24)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rating",
                                fontSize = 10.5.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                    // Total Calls
                    StatSubCard(
                        modifier = Modifier.weight(1f),
                        value = "${data.total_calls_count}",
                        label = "Calls"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Availability Switch Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.2.dp, BorderSubtle, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "I'm available",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (viewModel.isOnline) OnlineSuccess else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (viewModel.isOnline) "Online — receiving calls" else "Offline — not receiving calls",
                            fontSize = 12.5.sp,
                            color = if (viewModel.isOnline) OnlineSuccess else TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Switch(
                    checked = viewModel.isOnline,
                    onCheckedChange = { viewModel.toggleAvailability() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OnlineSuccess,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Recent Calls Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT CALLS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )

            Text(
                text = "View Checklist",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary,
                modifier = Modifier.clickable { viewModel.toggleMilestonesView(true) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Recent Calls List
        if (data.recent_calls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No calls yet. Go online to receive calls!",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        } else {
            data.recent_calls.forEach { call ->
                RecentCallCard(item = call)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatSubCard(
    modifier: Modifier,
    value: String,
    label: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.5.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun RecentCallCard(item: RecentCallItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.2.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6DA2C2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.caller_initial.ifBlank { item.caller_name.take(1) },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.caller_name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.duration_minutes} min · ${item.time_string}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    if (item.is_repeat_caller) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = "Repeat Caller",
                                modifier = Modifier.size(12.dp),
                                tint = Accent
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Repeat",
                                fontSize = 11.sp,
                                color = Accent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (item.gift_received.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CardGiftcard,
                                contentDescription = "Gift",
                                modifier = Modifier.size(12.dp),
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Gift",
                                fontSize = 11.sp,
                                color = Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Earning
            Text(
                text = "+₹${item.earning_coins.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnlineSuccess
            )
        }
    }
}
