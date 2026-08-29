package com.example.trueline_listener.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
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
import com.example.trueline_listener.ui.theme.*

@Composable
fun ProfileSafetyScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()
    val data = viewModel.dashboardData
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    val displayName = data.listener_name.ifBlank { "Listener" }
    val initials = displayName.split(" ")
        .filter { it.isNotBlank() }
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifBlank { "TL" }
    val idTag = data.listener_id_tag.ifBlank { "ID 40219" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFB))
    ) {
        // 1. Constant / Fixed Profile Header Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF8FAFB),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar Circle
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = Color(0xFFE2E8F0)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = displayName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Verified",
                                modifier = Modifier.size(15.dp),
                                tint = Color(0xFF0F766E)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Verified Listener · $idTag",
                                fontSize = 12.5.sp,
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Edit Action Link
                Text(
                    text = "Edit",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.openVoiceUpdateModal() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // 2. Scrollable Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        // 2. Rating & Answer Rate Cards (Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Rating Card: Shows "-.-" if no rated calls yet, or average of last 50 calls
            val ratingDisplay = if (data.rating_count > 0 && data.rating_avg > 0.0) {
                "${data.rating_avg}"
            } else {
                "-.-"
            }
            MetricStatCard(
                modifier = Modifier.weight(1f),
                title = "RATING",
                value = ratingDisplay,
                subtitle = "last 50 calls"
            )

            // Answer Rate Card: Shows 0% if no calls yet, without target subtext
            val answerRateDisplay = if (data.total_calls_count > 0) {
                "${data.answer_rate_pct}%"
            } else {
                "0%"
            }
            MetricStatCard(
                modifier = Modifier.weight(1f),
                title = "ANSWER RATE",
                value = answerRateDisplay,
                subtitle = null
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Verification Card
        VerificationCard(
            onReVerifyBankClick = { viewModel.openWithdrawModal() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Languages & hours Card
        LanguagesAndHoursCard(
            languagesText = viewModel.languagesText,
            preferredHoursText = viewModel.availableHoursText,
            onAddLanguageClick = { viewModel.openSubScreen(PortalSubScreen.AVAILABLE_HOURS) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Support & safety Options Card
        SupportAndSafetyCard(
            onSupportClick = { viewModel.openSubScreen(PortalSubScreen.SUPPORT_INFO) },
            onPayoutMethodClick = { viewModel.openSubScreen(PortalSubScreen.TRANSACTIONS) },
            onLogoutClick = { showLogoutConfirmation = true }
        )

        Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Voice Intro Update Modal
    if (viewModel.showVoiceUpdateModal) {
        VoiceUpdateModal(viewModel = viewModel)
    }

    // Logout Dialog Confirmation
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of your TrueLine Listener account?",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmation = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun MetricStatCard(
    modifier: Modifier,
    title: String,
    value: String,
    subtitle: String? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun VerificationCard(
    onReVerifyBankClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Verification",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(14.dp))

            VerificationRow(title = "Phone number", status = "Verified", isReVerify = false)
            Spacer(modifier = Modifier.height(12.dp))
            VerificationRow(title = "Face liveness check", status = "Verified", isReVerify = false)
            Spacer(modifier = Modifier.height(12.dp))
            VerificationRow(title = "Government ID", status = "Verified", isReVerify = false)
            Spacer(modifier = Modifier.height(12.dp))
            VerificationRow(
                title = "Bank / UPI",
                status = "Re-verify",
                isReVerify = true,
                onClick = onReVerifyBankClick
            )
        }
    }
}

@Composable
private fun VerificationRow(
    title: String,
    status: String,
    isReVerify: Boolean,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 13.5.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )

        Text(
            text = status,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isReVerify) Color(0xFFEA580C) else Color(0xFF0F766E)
        )
    }
}

@Composable
private fun LanguagesAndHoursCard(
    languagesText: String,
    preferredHoursText: String,
    onAddLanguageClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Languages & hours",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Language Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillChip(text = "Hindi", isAdd = false)
                PillChip(text = "Bhojpuri", isAdd = false)
                PillChip(text = "+ add", isAdd = true, onClick = onAddLanguageClick)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferred Hours Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preferred hours",
                    fontSize = 13.5.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "6 PM – 12 AM",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
private fun PillChip(
    text: String,
    isAdd: Boolean,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        color = if (isAdd) Color(0xFFF1F5F9) else Color(0xFFE2E8F0)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isAdd) FontWeight.Medium else FontWeight.Bold,
            color = if (isAdd) Color(0xFF64748B) else Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SupportAndSafetyCard(
    onSupportClick: () -> Unit,
    onPayoutMethodClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            MenuArrowRow(
                title = "Support & safety",
                isWarning = false,
                onClick = onSupportClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            MenuArrowRow(
                title = "Payout method",
                isWarning = false,
                onClick = onPayoutMethodClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            MenuArrowRow(
                title = "Logout",
                isWarning = true,
                onClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun MenuArrowRow(
    title: String,
    isWarning: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) Color(0xFFC2410C) else Color(0xFF0F172A)
        )

        Text(
            text = "→",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
fun VoiceUpdateModal(viewModel: MainPortalViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { viewModel.closeVoiceUpdateModal() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Update Voice Introduction",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = { viewModel.closeVoiceUpdateModal() }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Record a clear, 10–30 second introduction speaking naturally with warmth and empathy.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Timer & Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (viewModel.isRecordingNewVoice) Color(0xFFFEE2E2) else Color(0xFFF1F5F9))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (viewModel.isRecordingNewVoice) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626))
                            )
                        }
                        Text(
                            text = if (viewModel.isRecordingNewVoice) {
                                "Recording: 00:${viewModel.newVoiceDuration.toString().padStart(2, '0')} / 00:30"
                            } else if (viewModel.newRecordedVoicePath != null) {
                                "Recorded (${viewModel.newVoiceDuration}s) · Ready to save"
                            } else {
                                "Tap below to start recording (Min 10s)"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.isRecordingNewVoice) Color(0xFFDC2626) else Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mic Action Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (viewModel.isRecordingNewVoice) Color(0xFFDC2626) else Primary)
                        .clickable {
                            if (viewModel.isRecordingNewVoice) {
                                viewModel.stopNewVoiceRecording()
                            } else {
                                viewModel.startNewVoiceRecording()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (viewModel.isRecordingNewVoice) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = if (viewModel.isRecordingNewVoice) "Stop" else "Record",
                        modifier = Modifier.size(38.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (viewModel.isRecordingNewVoice) "Tap to Stop" else if (viewModel.newRecordedVoicePath != null) "Tap to Re-record" else "Tap to Record",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = { viewModel.saveAndUploadNewVoice() },
                    enabled = viewModel.newRecordedVoicePath != null && viewModel.newVoiceDuration >= 10 && !viewModel.isUpdatingVoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Color(0xFFCADFE1)
                    )
                ) {
                    if (viewModel.isUpdatingVoice) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text = "Save & Update Voice Intro",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.newRecordedVoicePath != null && viewModel.newVoiceDuration >= 10) Color.White else Color(0xFF5F7577)
                        )
                    }
                }
            }
        }
    }
}
