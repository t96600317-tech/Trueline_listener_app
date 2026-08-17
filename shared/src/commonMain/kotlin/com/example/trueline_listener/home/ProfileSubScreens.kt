package com.example.trueline_listener.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.theme.*

@Composable
fun BlockedUsersSubScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Back to Profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.clickable { viewModel.closeSubScreen() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Blocked Users",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Blocked users cannot see your profile, call you, or send messages.",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (viewModel.blockedUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No blocked users.", fontSize = 13.sp, color = TextSecondary)
            }
        } else {
            viewModel.blockedUsers.forEach { user ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(user.user_name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Blocked on ${user.blocked_date} · ${user.reason}", fontSize = 12.sp, color = TextSecondary)
                        }

                        TextButton(onClick = { viewModel.unblockUser(user.id) }) {
                            Text("Unblock", color = Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ReportUserModal(viewModel: MainPortalViewModel) {
    var reason by remember { mutableStateOf("Inappropriate behavior / language") }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { viewModel.closeSubScreen() },
        title = {
            Text("Report a User", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        },
        text = {
            Column {
                Text(
                    "Our trust and safety team reviews all incident reports within 24 hours. Your identity is strictly anonymous.",
                    fontSize = 12.5.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details (Optional)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.submitReportUser(reason, details) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Submit Report", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeSubScreen() }) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AvailableHoursModal(viewModel: MainPortalViewModel) {
    var hours by remember { mutableStateOf(viewModel.availableHoursText) }

    AlertDialog(
        onDismissRequest = { viewModel.closeSubScreen() },
        title = {
            Text("Set Available Hours", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        },
        text = {
            Column {
                Text(
                    "Setting your regular hours helps TrueLine highlight you to repeat callers when you are most likely online.",
                    fontSize = 12.5.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Schedule") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.updateAvailableHours(hours) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save Schedule", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeSubScreen() }) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun PrivacyInfoModal(viewModel: MainPortalViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.closeSubScreen() },
        title = {
            Text("Your Privacy Protection", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        },
        text = {
            Column {
                Text("🛡 100% Anonymous VoIP Audio", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
                Spacer(modifier = Modifier.height(3.dp))
                Text("Your real phone number, photo, and identity documents are never revealed to callers under any circumstance.", fontSize = 12.5.sp, color = TextSecondary)

                Spacer(modifier = Modifier.height(12.dp))

                Text("🔒 Encrypted In-App Calling", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
                Spacer(modifier = Modifier.height(3.dp))
                Text("All audio calls are routed securely through TrueLine VoIP servers with end-to-end transport encryption.", fontSize = 12.5.sp, color = TextSecondary)
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.closeSubScreen() },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Understood", color = Color.White)
            }
        }
    )
}

@Composable
fun SupportInfoModal(viewModel: MainPortalViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.closeSubScreen() },
        title = {
            Text("Dedicated Listener Support", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        },
        text = {
            Column {
                Text("Need help with payouts, calls, or your account?", fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                Text("📧 partners@trueline.app", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("💬 WhatsApp Partner Desk: +91 80000 12345", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Support is available 24/7 in Hindi and English.", fontSize = 12.sp, color = TextMuted)
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.closeSubScreen() },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Close", color = Color.White)
            }
        }
    )
}
