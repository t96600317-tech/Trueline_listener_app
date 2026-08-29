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
import com.example.trueline_listener.ui.theme.*

@Composable
fun HomeDashboardScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()
    val data = viewModel.dashboardData

    val balanceStr = if (data.this_week_earnings_coins > 0) {
        "₹${(data.this_week_earnings_coins * 100).toLong() / 100.0}"
    } else {
        "₹0.00"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFB))
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. WALLET BALANCE Card
        WalletBalanceCard(
            balanceStr = balanceStr,
            weekStr = "WEEK 1",
            payoutSubtext = "Weekly payout to UPI",
            onSeeDetailsClick = { viewModel.openSubScreen(PortalSubScreen.TRANSACTIONS) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Availability Card
        AvailabilityCard(
            mode = viewModel.availabilityMode,
            onModeSelected = { newMode ->
                if (newMode == AvailabilityMode.OFFLINE && viewModel.availabilityMode != AvailabilityMode.OFFLINE) {
                    viewModel.openGoOfflineModal()
                } else {
                    viewModel.updateAvailabilityMode(newMode)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. First-week guarantee Card
        FirstWeekGuaranteeCard(
            completedCalls = data.total_calls_count,
            targetCalls = 20,
            onClick = { viewModel.showMilestones() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Daily streak Card
        val dayNumber = if (data.total_calls_count > 0) 1 else 0
        DailyStreakCard(
            dayNumber = dayNumber,
            completedDays = dayNumber,
            totalDays = 5,
            rewardText = "Go online 2 hours today for +₹40"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Performance / Stats Card
        PerformanceStatsCard(
            data = data,
            selectedTab = viewModel.selectedStatsTab,
            onTabSelected = { viewModel.selectStatsTab(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WalletBalanceCard(
    balanceStr: String,
    weekStr: String,
    payoutSubtext: String,
    onSeeDetailsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF134E4A), // Dark teal background from design mockup
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: WALLET BALANCE + WEEK 3 Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WALLET BALANCE",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )

                // Pill on top right
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF3F3B2A)
                ) {
                    Text(
                        text = weekStr,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFF59E0B),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Balance Amount
            Text(
                text = balanceStr,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtext: Weekly payout date
            Text(
                text = payoutSubtext,
                fontSize = 12.5.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Link: See payout details ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeeDetailsClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "See payout details",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "→",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF97316) // Warm orange accent arrow
                )
            }
        }
    }
}

@Composable
private fun AvailabilityCard(
    mode: AvailabilityMode,
    onModeSelected: (AvailabilityMode) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Availability title + Green time indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Availability",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F766E))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "3h 12m today",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtext
            Text(
                text = if (mode == AvailabilityMode.OFFLINE) "You're offline right now" else "You're taking calls now",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Way Segmented Toggle Control: Offline | Busy | Online
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Offline
                    SegmentItem(
                        modifier = Modifier.weight(1f),
                        title = "Offline",
                        isSelected = mode == AvailabilityMode.OFFLINE,
                        onClick = { onModeSelected(AvailabilityMode.OFFLINE) }
                    )

                    // Busy
                    SegmentItem(
                        modifier = Modifier.weight(1f),
                        title = "Busy",
                        isSelected = mode == AvailabilityMode.BUSY,
                        onClick = { onModeSelected(AvailabilityMode.BUSY) }
                    )

                    // Online
                    SegmentItem(
                        modifier = Modifier.weight(1f),
                        title = "Online",
                        isSelected = mode == AvailabilityMode.ONLINE,
                        onClick = { onModeSelected(AvailabilityMode.ONLINE) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentItem(
    modifier: Modifier,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF134E4A) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF64748B)
        )
    }
}

@Composable
private fun FirstWeekGuaranteeCard(
    completedCalls: Int,
    targetCalls: Int,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "First-week guarantee",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = "$completedCalls/$targetCalls calls",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF134E4A)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            val progressFraction = (completedCalls.toFloat() / targetCalls.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .clip(CircleShape)
                        .background(Color(0xFF134E4A))
                )
            }
        }
    }
}

@Composable
private fun DailyStreakCard(
    dayNumber: Int,
    completedDays: Int,
    totalDays: Int,
    rewardText: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF7ED), // Soft amber/beige container background
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily streak • Day $dayNumber",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9A3412)
                )

                // 5 Dots indicator
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(totalDays) { index ->
                        val isFilled = index < completedDays
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Color(0xFFEA580C) else Color(0xFFFED7AA))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = rewardText,
                fontSize = 12.5.sp,
                color = Color(0xFFC2410C),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PerformanceStatsCard(
    data: com.example.trueline_listener.network.HomeDashboardResponse,
    selectedTab: PerformanceStatsTab,
    onTabSelected: (PerformanceStatsTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Tab row: Today | Yesterday | This week
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatTabItem(
                    title = "Today",
                    isSelected = selectedTab == PerformanceStatsTab.TODAY,
                    onClick = { onTabSelected(PerformanceStatsTab.TODAY) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                StatTabItem(
                    title = "Yesterday",
                    isSelected = selectedTab == PerformanceStatsTab.YESTERDAY,
                    onClick = { onTabSelected(PerformanceStatsTab.YESTERDAY) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                StatTabItem(
                    title = "This week",
                    isSelected = selectedTab == PerformanceStatsTab.THIS_WEEK,
                    onClick = { onTabSelected(PerformanceStatsTab.THIS_WEEK) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(14.dp))

            // Metrics depending on selectedTab using real data
            val (earningsStr, talkMinsStr, callsStr) = when (selectedTab) {
                PerformanceStatsTab.TODAY -> {
                    val earn = if (data.today_earnings_coins > 0) "₹${(data.today_earnings_coins * 100).toLong() / 100.0}" else "₹0.00"
                    val mins = "${data.today_minutes}.0"
                    val calls = "${data.today_calls} · 0"
                    Triple(earn, mins, calls)
                }
                PerformanceStatsTab.YESTERDAY -> {
                    Triple("₹0.00", "0.0", "0 · 0")
                }
                PerformanceStatsTab.THIS_WEEK -> {
                    val earn = if (data.this_week_earnings_coins > 0) "₹${(data.this_week_earnings_coins * 100).toLong() / 100.0}" else "₹0.00"
                    val mins = "${data.today_minutes}.0"
                    val calls = "${data.total_calls_count} · 0"
                    Triple(earn, mins, calls)
                }
            }

            // Row 1: Earnings
            MetricRow(label = "Earnings", value = earningsStr)
            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Talk minutes
            MetricRow(label = "Talk minutes", value = talkMinsStr)
            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Calls answered • missed
            MetricRow(label = "Calls answered · missed", value = callsStr)
        }
    }
}

@Composable
private fun StatTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF0F172A) else Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(if (isSelected) Color(0xFF134E4A) else Color.Transparent)
        )
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String
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
            text = value,
            fontSize = 14.5.sp,
            color = Color(0xFF0F172A),
            fontWeight = FontWeight.Bold
        )
    }
}
