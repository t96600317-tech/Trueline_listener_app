package com.example.trueline_listener.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
                PortalBottomNavigation(
                    currentTab = viewModel.currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
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
                fontWeight = FontWeight.Black,
                color = Primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE2EFF1))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "LISTENER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Notification Bell Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { /* Notifications */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Notifications",
                tint = Primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun PortalBottomNavigation(
    currentTab: PortalTab,
    onTabSelected: (PortalTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 24.dp),
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
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) Primary else TextMuted
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Primary else TextMuted
        )
    }
}
