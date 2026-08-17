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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.Star
import com.example.trueline_listener.network.PastPayoutItem
import com.example.trueline_listener.ui.theme.*

@Composable
fun EarningsHubScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()
    val data = viewModel.detailedEarnings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Available to Withdraw Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .border(1.2.dp, BorderSubtle, RoundedCornerShape(22.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Available to withdraw",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${data.available_to_withdraw_coins.toInt()}",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Withdraw Button
                Button(
                    onClick = { viewModel.openWithdrawModal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = "Withdraw to UPI",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Paid within 24 hours · TDS deducted as applicable",
                    fontSize = 11.5.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Performance Score Banner (Opens PerformanceScoreScreen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF235356))
                .clickable { viewModel.openSubScreen(PortalSubScreen.PERFORMANCE_SCORE) }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "Score",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Listener Score: ${viewModel.scoreData.score}/100",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${viewModel.scoreData.tier} Tier · Rank 7",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View Score",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Open",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. THIS WEEK Breakdown Section
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "THIS WEEK",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Call Earnings
        BreakdownRowCard(
            icon = Icons.Rounded.PhoneInTalk,
            iconTint = Primary,
            iconBg = Color(0xFFE2F3F3),
            title = "Call earnings",
            subtitle = data.call_hours_string,
            amount = "+₹${data.call_earnings_coins.toInt()}"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Gifts Received
        BreakdownRowCard(
            icon = Icons.Rounded.CardGiftcard,
            iconTint = Color(0xFFD97706),
            iconBg = Color(0xFFFFF0E0),
            title = "Gifts received",
            subtitle = data.gifts_count_string,
            amount = "+₹${data.gifts_received_coins.toInt()}"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Gold Tier Bonus
        BreakdownRowCard(
            icon = Icons.Rounded.EmojiEvents,
            iconTint = Color(0xFF059669),
            iconBg = Color(0xFFE3F7EB),
            title = "Gold tier bonus",
            subtitle = data.tier_bonus_subtitle,
            amount = "+₹${data.gold_tier_bonus_coins.toInt()}"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. PAST PAYOUTS Section
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "PAST PAYOUTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        data.past_payouts.forEach { payout ->
            PastPayoutCard(item = payout)
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Withdraw Modal
    if (viewModel.showWithdrawModal) {
        WithdrawDialog(viewModel = viewModel)
    }
}

@Composable
private fun BreakdownRowCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    amount: String
) {
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(22.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Text(
                text = amount,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = OnlineSuccess
            )
        }
    }
}

@Composable
private fun PastPayoutCard(item: PastPayoutItem) {
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
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2F6F3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = "Payout",
                    modifier = Modifier.size(18.dp),
                    tint = OnlineSuccess
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.date_string,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Text(
                text = "₹${item.amount_coins.toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun WithdrawDialog(viewModel: MainPortalViewModel) {
    val amountNum = viewModel.withdrawAmount.toDoubleOrNull() ?: 0.0
    val tds = amountNum * 0.10
    val net = (amountNum - tds).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = { viewModel.closeWithdrawModal() },
        title = {
            Text(
                text = "Confirm Withdrawal",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text("Enter amount to withdraw to your verified UPI ID:", fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = viewModel.withdrawAmount,
                    onValueChange = { viewModel.onWithdrawAmountChange(it) },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.withdrawUpiId,
                    onValueChange = { viewModel.onWithdrawUpiChange(it) },
                    label = { Text("UPI ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // TDS Calculation Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gross Amount:", fontSize = 12.sp, color = TextSecondary)
                            Text("₹${amountNum.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TDS (10%):", fontSize = 12.sp, color = TextSecondary)
                            Text("- ₹${tds.toInt()}", fontSize = 12.sp, color = Accent, fontWeight = FontWeight.Bold)
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = BorderSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Payout:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("₹${net.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnlineSuccess)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.submitWithdrawal() },
                enabled = !viewModel.isWithdrawing && amountNum >= 100.0,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(if (viewModel.isWithdrawing) "Processing..." else "Confirm & Withdraw", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeWithdrawModal() }) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
