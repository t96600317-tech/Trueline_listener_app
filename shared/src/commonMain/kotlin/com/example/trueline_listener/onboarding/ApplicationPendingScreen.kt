package com.example.trueline_listener.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.OnboardingProgressHeader
import com.example.trueline_listener.ui.theme.*

@Composable
fun ApplicationPendingScreen(viewModel: OnboardingViewModel) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Light
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

                Spacer(modifier = Modifier.height(36.dp))

                // Hourglass Badge (Tapping logs out for testing)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFBF1E6))
                        .clickable { viewModel.logout() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏳", fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(36.dp))

                // 3-Step Review Timeline
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
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
                            Text("Today, Just now", fontSize = 12.5.sp, color = TextSecondary)
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
                            Text("2", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Team review", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                            Text("Start taking calls", fontSize = 12.5.sp, color = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }

            // Footer Support Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Questions? WhatsApp our listener support any time.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
