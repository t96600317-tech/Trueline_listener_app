package com.example.trueline_listener.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Large Profile Header
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Color(0xFF6DA2C2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.listener_name.take(1).ifBlank { "P" },
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = data.listener_name.ifBlank { "Priya" },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Listener ID · ${data.listener_id_tag}",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Verified Pill Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFD4F0EB))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Verified",
                    modifier = Modifier.size(16.dp),
                    tint = OnlineSuccess
                )
                Text(
                    text = "Verified Listener · KYC complete",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnlineSuccess
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. VOICE INTRODUCTION SECTION
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "VOICE SAMPLE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Voice Intro Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.2.dp, BorderSubtle, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Voice Intro",
                            modifier = Modifier.size(24.dp),
                            tint = Primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voice Introduction Sample",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Callers listen to this before connecting",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Playback progress bar
                if (viewModel.isPlayingVoiceIntro) {
                    LinearProgressIndicator(
                        progress = { viewModel.voicePlaybackProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Primary,
                        trackColor = Color(0xFFE2E8F0)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Play / Pause Button
                    OutlinedButton(
                        onClick = { viewModel.toggleVoicePlayback() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (viewModel.isPlayingVoiceIntro) Primary.copy(alpha = 0.08f) else Color.Transparent,
                            contentColor = Primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, Primary)
                    ) {
                        Icon(
                            imageVector = if (viewModel.isPlayingVoiceIntro) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (viewModel.isPlayingVoiceIntro) "Pause" else "Play",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (viewModel.isPlayingVoiceIntro) "Stop" else "Listen",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Update Sample Button
                    Button(
                        onClick = { viewModel.openVoiceUpdateModal() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Update Voice",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Update",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 3. Preferences Group Header
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "PREFERENCES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preferences Group Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.2.dp, BorderSubtle, RoundedCornerShape(20.dp))
        ) {
            Column {
                ProfileMenuRow(
                    icon = Icons.Rounded.Translate,
                    iconTint = Color(0xFF2563EB),
                    iconBg = Color(0xFFEFF6FF),
                    title = "Languages",
                    subtitle = viewModel.languagesText,
                    onClick = { viewModel.openSubScreen(PortalSubScreen.AVAILABLE_HOURS) }
                )
                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                ProfileMenuRow(
                    icon = Icons.Rounded.AccessTime,
                    iconTint = Color(0xFFD97706),
                    iconBg = Color(0xFFFEF3C7),
                    title = "My available hours",
                    subtitle = viewModel.availableHoursText,
                    onClick = { viewModel.openSubScreen(PortalSubScreen.AVAILABLE_HOURS) }
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 4. SAFETY Section Header
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SAFETY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Safety Group Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.2.dp, BorderSubtle, RoundedCornerShape(20.dp))
        ) {
            Column {
                ProfileMenuRow(
                    icon = Icons.Rounded.Block,
                    iconTint = Color(0xFFDC2626),
                    iconBg = Color(0xFFFEE2E2),
                    title = "Blocked users",
                    subtitle = "${viewModel.blockedUsers.size} users blocked",
                    onClick = { viewModel.openSubScreen(PortalSubScreen.BLOCKED_USERS) }
                )
                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                ProfileMenuRow(
                    icon = Icons.Rounded.ReportProblem,
                    iconTint = Color(0xFFEA580C),
                    iconBg = Color(0xFFFFEDD5),
                    title = "Report a user",
                    subtitle = "We act within 24 hours",
                    onClick = { viewModel.openSubScreen(PortalSubScreen.REPORT_USER) }
                )
                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                ProfileMenuRow(
                    icon = Icons.Rounded.Lock,
                    iconTint = Color(0xFF059669),
                    iconBg = Color(0xFFD1FAE5),
                    title = "Your privacy",
                    subtitle = "No photo, no number shared. Ever.",
                    onClick = { viewModel.openSubScreen(PortalSubScreen.PRIVACY_INFO) }
                )
                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                ProfileMenuRow(
                    icon = Icons.Rounded.HeadsetMic,
                    iconTint = Primary,
                    iconBg = Primary.copy(alpha = 0.12f),
                    title = "Listener support",
                    subtitle = "24/7 dedicated partner assistance",
                    onClick = { viewModel.openSubScreen(PortalSubScreen.SUPPORT_INFO) }
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 5. ACCOUNT Section Header
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ACCOUNT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Log Out Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.2.dp, BorderSubtle, RoundedCornerShape(20.dp))
        ) {
            ProfileMenuRow(
                icon = Icons.Rounded.Logout,
                iconTint = Color(0xFFDC2626),
                iconBg = Color(0xFFFEE2E2),
                title = "Log Out",
                subtitle = "Sign out from this listener account",
                onClick = { showLogoutConfirmation = true }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Interactive Voice Update Modal Dialog
    if (viewModel.showVoiceUpdateModal) {
        VoiceUpdateModal(viewModel = viewModel)
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = {
                Text(
                    text = "Log Out from TrueLine?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "You will be signed out from your listener profile. You can log back in anytime using your phone number.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmation = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutConfirmation = false }
                ) {
                    Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    iconTint: Color = Primary,
    iconBg: Color = Color(0xFFF1F5F9),
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                color = TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = "Open",
            modifier = Modifier.size(20.dp),
            tint = TextMuted
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
                        color = TextPrimary
                    )
                    IconButton(onClick = { viewModel.closeVoiceUpdateModal() }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Record a clear, 10–30 second introduction speaking naturally with warmth and empathy.",
                    fontSize = 13.sp,
                    color = TextSecondary,
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
                            color = if (viewModel.isRecordingNewVoice) Color(0xFFDC2626) else TextPrimary
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
                    color = TextSecondary,
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
