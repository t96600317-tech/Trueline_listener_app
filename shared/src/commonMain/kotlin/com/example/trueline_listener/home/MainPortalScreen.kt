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
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.trueline_listener.ui.TrueLineLogoBadge
import com.example.trueline_listener.ui.theme.*

@Composable
fun MainPortalScreen(viewModel: MainPortalViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFB)
    ) {
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

@Composable
private fun PortalTopHeader(viewModel: MainPortalViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFB))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TrueLine LISTENER Brand Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            TrueLineLogoBadge(size = 32.dp, isDarkTheme = false)
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "True",
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )
            Text(
                text = "Line",
                fontSize = 19.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Accent.copy(alpha = 0.15f),
                modifier = Modifier.padding(top = 1.dp)
            ) {
                Text(
                    text = "PARTNER",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFB45309),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }

        // Live Earning Rate Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Primary.copy(alpha = 0.09f),
            modifier = Modifier.padding(top = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (viewModel.isOnline) OnlineSuccess else Color.Gray,
                    modifier = Modifier.size(7.dp)
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (viewModel.isOnline) "₹4.5/min Live" else "Offline",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.isOnline) Color(0xFF15803D) else Color.Gray
                )
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
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
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

            // Chat Tab
            BottomNavItem(
                title = "Chat",
                icon = Icons.AutoMirrored.Filled.Chat,
                badgeCount = unreadCount,
                isSelected = currentTab == PortalTab.CHAT,
                onClick = { onTabSelected(PortalTab.CHAT) }
            )

            // Earnings Tab
            BottomNavItem(
                title = "Earnings",
                icon = Icons.Rounded.MonetizationOn,
                isSelected = currentTab == PortalTab.EARNINGS,
                onClick = { onTabSelected(PortalTab.EARNINGS) }
            )

            // Profile Tab
            BottomNavItem(
                title = "Profile",
                icon = Icons.Rounded.Person,
                isSelected = currentTab == PortalTab.PROFILE,
                onClick = { onTabSelected(PortalTab.PROFILE) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) Primary else TextMuted
            )
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
                            color = Dark
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
            color = if (isSelected) Primary else TextMuted
        )
    }
}
