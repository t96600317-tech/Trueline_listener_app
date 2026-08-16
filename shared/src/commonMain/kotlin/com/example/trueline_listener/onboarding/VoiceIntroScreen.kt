package com.example.trueline_listener.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.theme.*

@Composable
fun VoiceIntroScreen(
    viewModel: OnboardingViewModel
) {
    val isRecording = viewModel.isRecording
    val duration = viewModel.recordingDuration
    val recordedPath = viewModel.recordedVoicePath
    val isPlaying = viewModel.isPlaying

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Light)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Voice Intro",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Dark
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Record a 10-20 second introduction",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = TextMutedGrey
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This will be used on your profile instead of a photo.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                color = TextMutedGrey.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Recording Visualizer / Timer
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isRecording) {
                RecordingAnimation()
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (recordedPath != null) "Recorded" else if (isRecording) "Recording..." else "Ready",
                    style = MaterialTheme.typography.labelLarge.copy(color = Primary)
                )
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (recordedPath == null) {
            // Record Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color.Red.copy(alpha = 0.1f) else Primary.copy(alpha = 0.1f))
                    .border(2.dp, if (isRecording) Color.Red else Primary, CircleShape)
                    .clickable {
                        if (isRecording) viewModel.stopRecording() else viewModel.startRecording()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Using text instead of icons to avoid dependency issues
                Text(
                    text = if (isRecording) "■" else "●",
                    color = if (isRecording) Color.Red else Primary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isRecording) "Tap to stop" else "Tap to record",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedGrey)
            )
        } else {
            // Post-recording controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete / Re-record
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .clickable { viewModel.deleteRecording() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Delete", style = MaterialTheme.typography.labelSmall, color = TextResendMuted)
                }

                // Play / Pause
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .clickable { viewModel.togglePlayback() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlaying) "■" else "▶",
                        color = Color.White,
                        fontSize = 24.sp
                    )
                }

                // Placeholder for balance
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { viewModel.submitVoiceIntro() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = recordedPath != null && duration >= 10,
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                disabledContainerColor = AccentDisabled
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Submit Intro",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun RecordingAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .size(150.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Primary.copy(alpha = alpha))
    )
}

fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
