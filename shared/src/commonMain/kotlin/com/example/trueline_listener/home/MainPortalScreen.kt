package com.example.trueline_listener.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.trueline_listener.ui.TrueLineBrandLockup
import com.example.trueline_listener.ui.TrueLineLogoBadge
import com.example.trueline_listener.ui.WalletIcon
import com.example.trueline_listener.ui.theme.*

@Composable
fun MainPortalScreen(viewModel: MainPortalViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFB)
    ) {
        if (viewModel.incomingCallSession != null) {
            IncomingCallOverlay(viewModel = viewModel)
        } else if (viewModel.activeChatUserId != null) {
            ListenerIndividualChatScreen(viewModel = viewModel)
        } else {
            Scaffold(
                topBar = {
                    PortalTopHeader(viewModel = viewModel)
                },
            bottomBar = {
                val unreadCount = viewModel.conversations.sumOf { it.unread_count }
                PortalBottomNavigation(
                    currentTab = viewModel.currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    unreadCount = unreadCount
                )
            },
            containerColor = Color(0xFFF8FAFB)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Sub-Screen Overlays or Tab Content
                when (viewModel.activeSubScreen) {
                    PortalSubScreen.PERFORMANCE_SCORE -> {
                        PerformanceScoreScreen(viewModel = viewModel)
                    }
                    PortalSubScreen.BLOCKED_USERS -> {
                        BlockedUsersSubScreen(viewModel = viewModel)
                    }
                    PortalSubScreen.REPORT_USER -> {
                        ReportUserModal(viewModel = viewModel)
                    }
                    PortalSubScreen.AVAILABLE_HOURS -> {
                        AvailableHoursModal(viewModel = viewModel)
                    }
                    PortalSubScreen.PRIVACY_INFO -> {
                        PrivacyInfoModal(viewModel = viewModel)
                    }
                    PortalSubScreen.SUPPORT_INFO -> {
                        SupportInfoModal(viewModel = viewModel)
                    }
                    PortalSubScreen.TRANSACTIONS -> {
                        TransactionsScreen(viewModel = viewModel)
                    }
                    PortalSubScreen.NOTIFICATIONS -> {
                        NotificationsScreen(viewModel = viewModel)
                    }
                    PortalSubScreen.NONE -> {
                        // Main Tabs
                        when (viewModel.currentTab) {
                            PortalTab.HOME -> {
                                if (viewModel.showMilestoneChecklist) {
                                    HomeMilestonesScreen(viewModel = viewModel)
                                } else {
                                    HomeDashboardScreen(viewModel = viewModel)
                                }
                            }
                            PortalTab.CALLS -> {
                                CallLogScreen(viewModel = viewModel)
                            }
                            PortalTab.CHAT -> {
                                ListenerChatScreen(viewModel = viewModel)
                            }
                            PortalTab.EARNINGS -> {
                                EarningsHubScreen(viewModel = viewModel)
                            }
                            PortalTab.PROFILE -> {
                                ProfileSafetyScreen(viewModel = viewModel)
                            }
                        }
                    }
                }

                // Go Offline Confirmation Modal
                if (viewModel.showGoOfflineModal) {
                    GoOfflineConfirmModal(viewModel = viewModel)
                }

                // Toast Notifications
                viewModel.successNotification?.let { msg ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OnlineSuccess)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(text = "✓ $msg", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                viewModel.errorMessage?.let { err ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE53E3E))
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(text = "⚠️ $err", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
}

@Composable
private fun PortalTopHeader(viewModel: MainPortalViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFB))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Back arrow on Profile tab or Milestones/Lessons page, or Profile Avatar on other tabs
        if (viewModel.currentTab == PortalTab.PROFILE) {
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.selectTab(PortalTab.HOME) },
                shape = CircleShape,
                color = Color(0xFFE2E8F0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else if (viewModel.currentTab == PortalTab.HOME && viewModel.showMilestoneChecklist) {
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.hideMilestones() },
                shape = CircleShape,
                color = Color(0xFFE2E8F0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to Home Dashboard",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.selectTab(PortalTab.PROFILE) },
                shape = CircleShape,
                color = Color(0xFFE2E8F0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val displayName = viewModel.dashboardData.listener_name.ifBlank { "Listener" }
                    val initials = displayName.split(" ")
                        .filter { it.isNotBlank() }
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()
                        .ifBlank { "TL" }
                    Text(
                        text = initials,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        // Center: TrueLine listener brand lockup matching official asset
        TrueLineBrandLockup(height = 26.dp, isDarkTheme = false)

        // Right: Notification Bell Button with alert dot
        Surface(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable { viewModel.openSubScreen(PortalSubScreen.NOTIFICATIONS) },
            shape = CircleShape,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(20.dp)
                )

                if (viewModel.unreadNotificationsCount > 0) {
                    // Orange Alert Badge Dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF97316))
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PortalBottomNavigation(
    currentTab: PortalTab,
    onTabSelected: (PortalTab) -> Unit,
    unreadCount: Int = 0
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab
            BottomNavItem(
                title = "Home",
                icon = Icons.Rounded.Home,
                isSelected = currentTab == PortalTab.HOME,
                onClick = { onTabSelected(PortalTab.HOME) }
            )

            // Calls Tab
            BottomNavItem(
                title = "Calls",
                icon = Icons.Rounded.Call,
                isSelected = currentTab == PortalTab.CALLS,
                onClick = { onTabSelected(PortalTab.CALLS) }
            )

            // Chat Tab
            BottomNavItem(
                title = "Chat",
                icon = Icons.AutoMirrored.Filled.Chat,
                badgeCount = unreadCount,
                isSelected = currentTab == PortalTab.CHAT,
                onClick = { onTabSelected(PortalTab.CHAT) }
            )

            // Wallet Tab
            BottomNavItem(
                title = "Wallet",
                customIcon = { WalletIcon(modifier = Modifier.size(24.dp), isSelected = currentTab == PortalTab.EARNINGS) },
                isSelected = currentTab == PortalTab.EARNINGS,
                onClick = { onTabSelected(PortalTab.EARNINGS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: ImageVector? = null,
    customIcon: (@Composable () -> Unit)? = null,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFF134E4A)
    val inactiveColor = Color(0xFF94A3B8)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (customIcon != null) {
                customIcon()
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) activeColor else inactiveColor
                )
            }
            if (badgeCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = Accent,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}
