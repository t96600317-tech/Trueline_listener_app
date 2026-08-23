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
import com.example.trueline_listener.ui.theme.*

data class CallLogItem(
    val avatarText: String,
    val callerName: String,
    val isMissed: Boolean,
    val timestampDetails: String,
    val amountStr: String,
    val isNegative: Boolean,
    val isPeachAvatar: Boolean = false
)

@Composable
fun CallLogScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()

    val todayCalls = listOf(
        CallLogItem(
            avatarText = "8F",
            callerName = "caller 8f21 · Hindi",
            isMissed = false,
            timestampDetails = "9:08 PM · 11 min 04 s · ★ 5",
            amountStr = "₹49.50",
            isNegative = false
        ),
        CallLogItem(
            avatarText = "2C",
            callerName = "caller 2c07 · Hindi",
            isMissed = false,
            timestampDetails = "7:41 PM · 6 min 12 s · ★ 5",
            amountStr = "₹27.40",
            isNegative = false
        ),
        CallLogItem(
            avatarText = "4A",
            callerName = "caller 4a55 · missed",
            isMissed = true,
            timestampDetails = "6:20 PM · rang 50 s",
            amountStr = "- ₹10",
            isNegative = true,
            isPeachAvatar = true
        ),
        CallLogItem(
            avatarText = "6B",
            callerName = "caller 6b93 · Hindi",
            isMissed = false,
            timestampDetails = "4:02 PM · 9 min 31 s · ★ 4",
            amountStr = "₹41.80",
            isNegative = false
        )
    )

    val yesterdayCalls = listOf(
        CallLogItem(
            avatarText = "D1",
            callerName = "caller d1f8 · Hindi",
            isMissed = false,
            timestampDetails = "10:14 PM · 14 min 02 s · ★ 5",
            amountStr = "₹63.10",
            isNegative = false
        ),
        CallLogItem(
            avatarText = "7E",
            callerName = "caller 7e20 · Hindi",
            isMissed = false,
            timestampDetails = "9:36 PM · 5 min 48 s · ★ 5",
            amountStr = "₹26.10",
            isNegative = false
        )
    )

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
                value = "57"
            )
            CallStatCard(
                modifier = Modifier.weight(1f),
                title = "AVG MIN",
                value = "6.4"
            )
            CallStatCard(
                modifier = Modifier.weight(1f),
                title = "RATING",
                value = "4.8"
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 3. TODAY Section Header
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

        // 4. TODAY Group Card Container
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

        // 5. YESTERDAY Section Header
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

        // 6. YESTERDAY Group Card Container
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
private fun CallLogRowItem(item: CallLogItem) {
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
                color = if (item.isPeachAvatar) Color(0xFFFEE2E2) else Color(0xFFE2ECE9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.avatarText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isPeachAvatar) Color(0xFF991B1B) else Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.callerName,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.timestampDetails,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right earnings/penalty amount
        Text(
            text = item.amountStr,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (item.isNegative) Color(0xFFEA580C) else Color(0xFF0F172A)
        )
    }
}
