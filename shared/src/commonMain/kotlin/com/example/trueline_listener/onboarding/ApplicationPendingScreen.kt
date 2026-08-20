package com.example.trueline_listener.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.OnboardingProgressHeader
import com.example.trueline_listener.ui.TrueLineWaveformLoader
import com.example.trueline_listener.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ApplicationPendingScreen(viewModel: OnboardingViewModel) {
    val scrollState = rememberScrollState()

    // Silent background polling every 10s — no animation, no visible state change
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            viewModel.pollApprovalStatusSilently()
        }
    }

    // Pull-to-refresh only shows spinner when manually triggered
    val pullRefreshState = rememberPullRefreshState(
        refreshing = viewModel.isCheckingApprovalStatus,
        onRefresh = { viewModel.checkApprovalStatus() }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Light
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OnboardingProgressHeader(
                        currentStep = 7,
                        totalSteps = 7,
                        titleNormal = "We're reviewing",
                        titleHighlight = "your application",
                        subtitle = "Usually approved within 24 hours. We'll notify you as soon as it's done."
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Hourglass Badge (Tapping logs out for testing)
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBF1E6))
                            .clickable { viewModel.logout() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏳", fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // 3-Step Review Timeline
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Timeline Item 1: Application submitted (Done)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(OnlineSuccess),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Application submitted", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Today, Completed", fontSize = 12.5.sp, color = TextSecondary)
                            }
                        }

                        // Timeline Item 2: Team review (Active Amber)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", color = Dark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Team review & KYC verification", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("In progress", fontSize = 12.5.sp, color = TextSecondary)
                            }
                        }

                        // Timeline Item 3: Approved & live (Pending)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BorderSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Approved & live", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text("Start taking paid calls", fontSize = 12.5.sp, color = TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Footer Actions & Reload Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { viewModel.checkApprovalStatus() },
                        enabled = !viewModel.isCheckingApprovalStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        if (viewModel.isCheckingApprovalStatus) {
                            TrueLineWaveformLoader(
                                size = 24.dp,
                                barColor = Dark,
                                accentColor = Primary
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Dark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check Status Now", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Dark)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Questions? WhatsApp listener support anytime.",
                        fontSize = 12.5.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Only shows spinner when manually triggered (pull-to-refresh or button)
            PullRefreshIndicator(
                refreshing = viewModel.isCheckingApprovalStatus,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.White,
                contentColor = Primary
            )
        }
    }
}
