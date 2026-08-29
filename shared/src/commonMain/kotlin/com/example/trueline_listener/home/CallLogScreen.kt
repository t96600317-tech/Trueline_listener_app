package com.example.trueline_listener.home

import androidx.compose.foundation.background
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
import com.example.trueline_listener.network.CallLogHistoryItem
import com.example.trueline_listener.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchCallHistory()
    }

    val history = viewModel.callHistoryData
    val allCalls = history?.calls ?: emptyList()
    val todayCalls = allCalls.filter { it.section == "TODAY" }
    val yesterdayCalls = allCalls.filter { it.section == "YESTERDAY" }
    val earlierCalls = allCalls.filter { it.section == "EARLIER" }

    val answeredCount = history?.total_answered ?: 0
    val avgDuration = history?.avg_duration_min ?: 0.0
    val ratingDisplay = if ((history?.rating_count ?: 0) > 0) {
        history?.avg_rating?.toString() ?: "-.-"
    } else {
        "-.-"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFB))
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header: Calls Title & Last 7 days Filter Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calls",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE6F4F1),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCCE8E3))
            ) {
                Text(
                    text = "Last 7 days",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D6A62),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Top Stats Row: ANSWERED | AVG MIN | RATING
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CallStatCard(
                modifier = Modifier.weight(1f),
                title = "ANSWERED",
                value = answeredCount.toString()
            )
            CallStatCard(
                modifier = Modifier.weight(1f),
                title = "AVG MIN",
                value = avgDuration.toString()
            )
            CallStatCard(
                modifier = Modifier.weight(1f),
                title = "RATING",
                value = ratingDisplay
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        if (viewModel.isCallHistoryLoading && allCalls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color(0xFF134E4A)
                )
            }
        } else if (allCalls.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No calls yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When users call you on TrueLine, your call records and earnings will appear here.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            // 3. TODAY Section
            if (todayCalls.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "TODAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column {
                        todayCalls.forEachIndexed { index, item ->
                            CallLogRowItem(item = item)
                            if (index < todayCalls.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
            }

            // 4. YESTERDAY Section
            if (yesterdayCalls.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "YESTERDAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column {
                        yesterdayCalls.forEachIndexed { index, item ->
                            CallLogRowItem(item = item)
                            if (index < yesterdayCalls.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
            }

            // 5. EARLIER Section
            if (earlierCalls.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "EARLIER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column {
                        earlierCalls.forEachIndexed { index, item ->
                            CallLogRowItem(item = item)
                            if (index < earlierCalls.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun CallStatCard(
    modifier: Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
private fun CallLogRowItem(item: CallLogHistoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Avatar Circle
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = if (item.is_peach_avatar) Color(0xFFFEE2E2) else Color(0xFFE2ECE9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.avatar_text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.is_peach_avatar) Color(0xFF991B1B) else Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.caller_name,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.timestamp_details,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right earnings/penalty amount
        Text(
            text = item.amount_str,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (item.is_negative) Color(0xFFEA580C) else Color(0xFF0F172A)
        )
    }
}
