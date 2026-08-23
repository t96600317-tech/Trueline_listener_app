package com.example.trueline_listener.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Headset
import com.example.trueline_listener.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    when (viewModel.screenState) {
        AppScreenState.HOME -> HomeMainView(viewModel)
        AppScreenState.INCOMING_CALL -> IncomingCallView(viewModel)
        AppScreenState.ACTIVE_CALL -> ActiveCallView(viewModel)
    }
}

@Composable
fun HomeMainView(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Light)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top Header with Profile Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedGrey)
                )
                Text(
                    text = viewModel.profile?.name ?: "Listener Partner",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark,
                        fontSize = 22.sp
                    )
                )
            }

            // Rating Pill
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(14.dp)
                    )
                    Text("4.9", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB45309))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (viewModel.errorMessage != null) {
            Surface(
                color = Color(0xFFFEE2E2),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = viewModel.errorMessage ?: "",
                    color = Color(0xFFDC2626),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Availability Toggle Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (viewModel.isOnline) Color(0xFFECFDF5) else SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (viewModel.isOnline) Color(0xFF10B981) else BorderSubtle
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (viewModel.isOnline) Color(0xFF10B981) else TextMutedGrey)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (viewModel.isOnline) "You are Online" else "You are Offline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (viewModel.isOnline) Color(0xFF047857) else Dark
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (viewModel.isOnline) "Ready to receive incoming user calls" else "Go online to start earning 3 coins/min",
                        fontSize = 12.sp,
                        color = TextMutedGrey
                    )
                }

                Switch(
                    checked = viewModel.isOnline,
                    onCheckedChange = { viewModel.toggleAvailability() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Earnings Summary Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Primary,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Total Available Balance",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "₹${(viewModel.earnings.available_balance_coins).toInt()}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { viewModel.openPayoutModal() },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Withdraw", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Lifetime Earned", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text("₹${viewModel.earnings.total_earned_coins.toInt()}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                    Column {
                        Text("Withdrawn Paid", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text("₹${viewModel.earnings.total_paid_coins.toInt()}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                    Column {
                        Text("Earning Rate", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text("3 coins / min", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Call Test Helper / Simulator Button (For Demoing Call Receiver)
        if (viewModel.isOnline) {
            OutlinedButton(
                onClick = { viewModel.onIncomingCallReceived("session_sim_1", "room_sim_1") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simulate Incoming Call Notification", color = Primary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Payout Request Modal
    if (viewModel.showPayoutModal) {
        AlertDialog(
            onDismissRequest = { viewModel.closePayoutModal() },
            title = { Text("Request Bank / UPI Payout", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Payouts are disbursed weekly via Cashfree Payouts v2 (10% standard TDS applicable).",
                        fontSize = 12.sp,
                        color = TextMutedGrey
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.payoutAmountINR,
                        onValueChange = { viewModel.onPayoutAmountChange(it) },
                        label = { Text("Amount (INR)") },
                        placeholder = { Text("e.g. 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = viewModel.payoutUPI,
                        onValueChange = { viewModel.onPayoutUPIChange(it) },
                        label = { Text("Payout UPI ID") },
                        placeholder = { Text("yourname@okbank") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitPayout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text("Submit Request", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closePayoutModal() }) {
                    Text("Cancel", color = TextMutedGrey)
                }
            }
        )
    }
}

@Composable
fun IncomingCallView(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Headset,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Incoming Audio Call", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(viewModel.incomingCallerName, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Rate: ₹4.5/min earning (Server Metered)", color = Color(0xFF10B981), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.declineCall() },
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                contentPadding = PaddingValues(0.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "Decline",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Button(
                onClick = { viewModel.acceptCall() },
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                contentPadding = PaddingValues(0.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Accept",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun ActiveCallView(viewModel: HomeViewModel) {
    val minutes = viewModel.callDurationSeconds / 60
    val seconds = viewModel.callDurationSeconds % 60
    val timerStr = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Headset,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("In Call with Caller", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(timerStr, color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Earning: +₹4.5/min", color = Color(0xFF10B981), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isMuted) Color(0xFFEF4444) else Color.White.copy(alpha = 0.2f)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = if (viewModel.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = "Mute",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Button(
                onClick = { viewModel.endActiveCall() },
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                contentPadding = PaddingValues(0.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "Hang Up",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
