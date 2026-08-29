package com.example.trueline_listener.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.network.TransactionItemData
import com.example.trueline_listener.ui.theme.*

@Composable
fun TransactionsScreen(viewModel: MainPortalViewModel) {
    val verticalScrollState = rememberScrollState()
    val filterScrollState = rememberScrollState()
    val currentFilter = viewModel.selectedTransactionFilter

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchTransactions()
    }

    val allTransactions = viewModel.transactionsList

    val filteredList = when (currentFilter) {
        TransactionFilter.ALL -> allTransactions
        TransactionFilter.CALLS -> allTransactions.filter { it.filter_type == "CALLS" }
        TransactionFilter.BONUS -> allTransactions.filter { it.filter_type == "BONUS" }
        TransactionFilter.PAYOUT -> allTransactions.filter { it.filter_type == "PAYOUT" }
        TransactionFilter.PENALTY -> allTransactions.filter { it.filter_type == "PENALTY" }
    }

    // Group filtered transactions by month (preserving order)
    val groupedByMonth = filteredList.groupBy { it.month_group }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFB))
    ) {
        // 1. Top Bar: Back Arrow + Title + Help Question Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF8FAFB)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.closeSubScreen() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Transactions",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                // Help (?) button pill
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.openSubScreen(PortalSubScreen.SUPPORT_INFO) },
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Scrollable Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 2. Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(filterScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterPillChip(
                    title = "All",
                    isSelected = currentFilter == TransactionFilter.ALL,
                    onClick = { viewModel.selectTransactionFilter(TransactionFilter.ALL) }
                )
                FilterPillChip(
                    title = "Calls",
                    isSelected = currentFilter == TransactionFilter.CALLS,
                    onClick = { viewModel.selectTransactionFilter(TransactionFilter.CALLS) }
                )
                FilterPillChip(
                    title = "Bonus",
                    isSelected = currentFilter == TransactionFilter.BONUS,
                    onClick = { viewModel.selectTransactionFilter(TransactionFilter.BONUS) }
                )
                FilterPillChip(
                    title = "Payout",
                    isSelected = currentFilter == TransactionFilter.PAYOUT,
                    onClick = { viewModel.selectTransactionFilter(TransactionFilter.PAYOUT) }
                )
                FilterPillChip(
                    title = "Penalty",
                    isSelected = currentFilter == TransactionFilter.PENALTY,
                    onClick = { viewModel.selectTransactionFilter(TransactionFilter.PENALTY) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (viewModel.isTransactionsLoading && allTransactions.isEmpty()) {
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
            } else if (filteredList.isEmpty()) {
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
                            text = if (currentFilter == TransactionFilter.CALLS) "No call earnings yet" else "No transactions yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (currentFilter == TransactionFilter.CALLS) {
                                "When you receive and complete calls with users, your earnings will appear here."
                            } else {
                                "When you complete calls, receive bonuses, or request payouts, they will appear here."
                            },
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // 3. Render each month group
                groupedByMonth.forEach { (monthName, items) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = monthName,
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
                            items.forEachIndexed { index, item ->
                                TransactionRowItem(item = item)
                                if (index < items.lastIndex) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Footer Notice
            Text(
                text = "Amounts clear 48 hours after the call ends.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FilterPillChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF134E4A) else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun TransactionRowItem(item: TransactionItemData) {
    val statusColor = when (item.status_color.lowercase()) {
        "orange" -> Color(0xFFEA580C)
        "green" -> Color(0xFF16A34A)
        else -> Color(0xFF94A3B8)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.timestamp,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right details: Amount + Status
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.amount,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.title.contains("penalty", ignoreCase = true)) Color(0xFFC2410C) else if (item.is_positive) Color(0xFFEA580C) else Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.status,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }
    }
}
