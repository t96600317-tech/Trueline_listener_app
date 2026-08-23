package com.example.trueline_listener.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.TrueLineLogoBadge
import com.example.trueline_listener.ui.theme.*

@Composable
fun EarningsHubScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFB))
    ) {
        // 1. Dark Hero Header Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0F172A) // Dark slate navy
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Top Row: Back Arrow + Logo Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.selectTab(PortalTab.HOME) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TrueLineLogoBadge(size = 24.dp, isDarkTheme = true)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // NEXT PAYOUT Header Label
                Text(
                    text = "NEXT PAYOUT · MON 24 AUG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Main Amount Display
                Text(
                    text = "₹1,248.60",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // UPI Subtext
                Text(
                    text = "To UPI akshaya@okaxis · ••••3421",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 2. Scrollable Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card 1: This week's breakdown
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "This week's breakdown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 1: Talk time
                    BreakdownRow(
                        label = "Talk time · 264 min",
                        amount = "₹1,188.00"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Streak bonuses
                    BreakdownRow(
                        label = "Streak bonuses",
                        amount = "₹120.00"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 3: Missed-call penalty
                    BreakdownRow(
                        label = "Missed-call penalty",
                        amount = "- ₹20.00",
                        amountColor = Color(0xFFEA580C)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 4: Pending
                    BreakdownRow(
                        label = "Pending (clears in 48h)",
                        amount = "₹39.40",
                        amountColor = Color(0xFFD97706)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Spacer(modifier = Modifier.height(14.dp))

                    // Total Payable Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Payable",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "₹1,248.60",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Recent payouts
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Recent payouts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Payout Item 1
                    RecentPayoutRow(
                        dateStr = "Mon 17 Aug",
                        subtitleStr = "UPI · 2 h 14 m to settle",
                        amountStr = "₹2,910.00"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Payout Item 2
                    RecentPayoutRow(
                        dateStr = "Mon 10 Aug",
                        subtitleStr = "UPI · same day",
                        amountStr = "₹2,184.50"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: Policy Footer Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE6F4F1),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCCE8E3))
            ) {
                Text(
                    text = "Payouts run every Monday for the week that ended Sunday. No minimum, no fee.",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D6A62),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    amount: String,
    amountColor: Color = Color(0xFF0F172A)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.5.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = amount,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

@Composable
private fun RecentPayoutRow(
    dateStr: String,
    subtitleStr: String,
    amountStr: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = dateStr,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitleStr,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = amountStr,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
    }
}
