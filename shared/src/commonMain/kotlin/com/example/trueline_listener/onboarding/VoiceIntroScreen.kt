package com.example.trueline_listener.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.audio.AudioPermissionRequester
import com.example.trueline_listener.ui.OnboardingProgressHeader
import com.example.trueline_listener.ui.TrueLineWaveformLoader
import com.example.trueline_listener.ui.theme.*

@Composable
fun VoiceIntroScreen(viewModel: OnboardingViewModel) {
    val scrollState = rememberScrollState()

    AudioPermissionRequester { requestPermission, hasPermission ->
        // Prompt permission when screen mounts if not yet granted
        LaunchedEffect(Unit) {
            if (!hasPermission) {
                requestPermission()
            }
        }

        // Curated sensible sample phrases for listener voice intros
        val samplePrompts = remember {
            listOf(
                "नमस्ते! कई बार दिल की बात किसी से कह देने से मन बहुत हल्का हो जाता है। मैं यहाँ हूँ आपकी हर बात बिना किसी जजमेंट के सुनने के लिए।",
                "Hello! Sometimes sharing what's on your mind is all it takes to feel lighter. I'm here to listen to you with complete warmth, empathy, and privacy.",
                "नमस्ते! अगर आपका दिन भारी या तनावपूर्ण रहा है, तो आप बेझिझक मुझसे बात कर सकते हैं। आपकी हर बात यहाँ 100% सुरक्षित और गोपनीय रहेगी।",
                "Hi there! Whether you had a hectic day, need to vent, or just want a friendly companion to talk things through, I am always here for you.",
                "नमस्ते, जिंदगी में कई बातें ऐसी होती हैं जो हम अपनों से नहीं कह पाते। आप मुझसे बिना किसी झिझक के दिल खोलकर बात कर सकते हैं।",
                "Hello! I am a friendly and calm listener on TrueLine. Feel free to talk about your daily life, career, relationships, or anything you wish."
            )
        }

        var currentPromptIndex by remember { mutableStateOf(0) }
        val currentPrompt = samplePrompts[currentPromptIndex]

        // Pulsing animation for the recording orb
        val infiniteTransition = rememberInfiniteTransition(label = "OrbPulseTransition")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.14f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PulseScale"
        )
        val ringAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "RingAlpha"
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Light
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OnboardingProgressHeader(
                        currentStep = 3,
                        totalSteps = 7,
                        titleNormal = "Record your",
                        titleHighlight = "voice intro",
                        subtitle = "Callers hear your 10-30s audio clip before connecting. Speak warmly and naturally."
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Teleprompter / Sample Script Helper Card with Vector Refresh Icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(1.2.dp, BorderSubtle, RoundedCornerShape(18.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Primary.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "SUGGESTED SCRIPT",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Primary
                                        )
                                    }
                                }

                                // Shuffle Prompt Button with Vector Refresh Icon
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEFF5F6))
                                        .clickable {
                                            currentPromptIndex = (currentPromptIndex + 1) % samplePrompts.size
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    VectorRefreshIcon(
                                        modifier = Modifier.size(13.dp),
                                        color = Primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "New phrase",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            AnimatedContent(
                                targetState = currentPrompt,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                            slideInVertically(animationSpec = tween(220, delayMillis = 90)) { height -> height / 2 })
                                        .togetherWith(fadeOut(animationSpec = tween(90)) +
                                                slideOutVertically(animationSpec = tween(90)) { height -> -height / 2 })
                                },
                                label = "PromptAnimation"
                            ) { promptText ->
                                Text(
                                    text = "\"$promptText\"",
                                    fontSize = 14.5.sp,
                                    lineHeight = 22.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Studio Recording Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.2.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .padding(vertical = 26.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Interactive Recording Orb with Vector Icon & Ripple Waves
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                if (viewModel.isRecording) {
                                    Box(
                                        modifier = Modifier
                                            .size(160.dp)
                                            .scale(pulseScale)
                                            .clip(CircleShape)
                                            .background(Accent.copy(alpha = ringAlpha))
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (viewModel.isRecording) Accent
                                            else if (viewModel.recordedVoicePath != null) Primary
                                            else Color(0xFFEFF5F6)
                                        )
                                        .border(
                                            width = 4.dp,
                                            color = if (viewModel.isRecording) Color(0xFFFDECDA)
                                            else if (viewModel.recordedVoicePath != null) Color(0xFFD4EAE7)
                                            else BorderSubtle,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            if (viewModel.isRecording) {
                                                viewModel.stopVoiceRecording()
                                            } else {
                                                if (!hasPermission) {
                                                    requestPermission()
                                                } else {
                                                    viewModel.startVoiceRecording()
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        viewModel.isRecording -> {
                                            // Clean stop square icon
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(Color.White)
                                            )
                                        }
                                        viewModel.recordedVoicePath != null -> {
                                            VectorCheckIcon(
                                                modifier = Modifier.size(36.dp),
                                                color = Color.White
                                            )
                                        }
                                        else -> {
                                            VectorMicIcon(
                                                modifier = Modifier.size(38.dp),
                                                color = Primary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Dynamic Timer Display
                            val duration = viewModel.recordingDuration
                            val mins = duration / 60
                            val secs = duration % 60
                            val formattedSecs = if (secs < 10) "0$secs" else "$secs"

                            Text(
                                text = if (viewModel.isRecording) "$mins:$formattedSecs"
                                else if (viewModel.recordedVoicePath != null) "Recorded ($mins:$formattedSecs)"
                                else "00:00",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.isRecording) Accent else TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = when {
                                    viewModel.isRecording -> "Recording... Tap orb to stop (min 10s)"
                                    viewModel.recordedVoicePath != null -> "Voice intro ready! Listen below or re-record"
                                    else -> "Tap microphone to record (10-30s)"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (viewModel.isRecording) Accent else TextSecondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Live Oscillating Waveform or Playback Bar
                            if (viewModel.isRecording) {
                                TrueLineWaveformLoader(
                                    size = 26.dp,
                                    barColor = Accent,
                                    accentColor = Secondary
                                )
                            } else if (viewModel.recordedVoicePath != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                                ) {
                                    // Linear Playback Progress Track
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFE2EBEB))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(viewModel.playbackProgress)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Primary)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFFEFF7F8))
                                            .border(1.dp, Color(0xFFD4EAE7), RoundedCornerShape(20.dp))
                                            .clickable {
                                                viewModel.toggleAudioPlayback()
                                            }
                                            .padding(horizontal = 18.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (viewModel.isPlaying) {
                                            VectorPauseIcon(modifier = Modifier.size(14.dp), color = Primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Pause preview",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Primary
                                            )
                                        } else {
                                            VectorPlayIcon(modifier = Modifier.size(14.dp), color = Primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Play preview",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (viewModel.errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = viewModel.errorMessage ?: "",
                            color = Danger,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Bottom CTA Buttons with Animated Transitions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = when {
                            viewModel.isRecording -> 1
                            viewModel.recordedVoicePath != null -> 2
                            else -> 0
                        },
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                        },
                        label = "ButtonTransition"
                    ) { state ->
                        when (state) {
                            1 -> {
                                Button(
                                    onClick = { viewModel.stopVoiceRecording() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                                ) {
                                    Text(
                                        text = "Stop & Save Recording",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            2 -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (!hasPermission) {
                                                requestPermission()
                                            } else {
                                                viewModel.startVoiceRecording()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                        border = androidx.compose.foundation.BorderStroke(1.2.dp, BorderSubtle)
                                    ) {
                                        Text(
                                            text = "Re-record",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.submitVoiceIntro() },
                                        enabled = !viewModel.isLoading,
                                        modifier = Modifier
                                            .weight(1.4f)
                                            .height(54.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                    ) {
                                        if (viewModel.isLoading) {
                                            TrueLineWaveformLoader(size = 24.dp, barColor = Color.White, accentColor = Accent)
                                        } else {
                                            Text(
                                                text = "Continue",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                Button(
                                    onClick = {
                                        if (!hasPermission) {
                                            requestPermission()
                                        } else {
                                            viewModel.startVoiceRecording()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    VectorMicIcon(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Start Voice Recording",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "You can update your voice sample anytime later from profile settings.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

// --- Custom Vector Icon Composables ---

@Composable
fun VectorRefreshIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = w * 0.38f
        val center = Offset(w * 0.5f, h * 0.5f)

        drawArc(
            color = color,
            startAngle = 40f,
            sweepAngle = 280f,
            useCenter = false,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = w * 0.16f, cap = StrokeCap.Round)
        )

        // Arrow head
        val arrowPath = Path().apply {
            moveTo(w * 0.82f, h * 0.18f)
            lineTo(w * 0.88f, h * 0.42f)
            lineTo(w * 0.64f, h * 0.42f)
            close()
        }
        drawPath(arrowPath, color = color)
    }
}

@Composable
fun VectorMicIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Microphone capsule body
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.32f, h * 0.12f),
            size = Size(w * 0.36f, h * 0.48f),
            cornerRadius = CornerRadius(w * 0.18f, w * 0.18f)
        )

        // Outer U-cup cradle
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.22f, h * 0.30f),
            size = Size(w * 0.56f, h * 0.42f),
            style = Stroke(width = w * 0.1f, cap = StrokeCap.Round)
        )

        // Center stem
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.72f),
            end = Offset(w * 0.5f, h * 0.90f),
            strokeWidth = w * 0.1f,
            cap = StrokeCap.Round
        )

        // Base bar
        drawLine(
            color = color,
            start = Offset(w * 0.32f, h * 0.90f),
            end = Offset(w * 0.68f, h * 0.90f),
            strokeWidth = w * 0.1f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun VectorCheckIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.72f)
            lineTo(w * 0.80f, h * 0.30f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = w * 0.14f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun VectorPlayIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.28f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.5f)
            lineTo(w * 0.28f, h * 0.82f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun VectorPauseIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.25f, h * 0.18f),
            size = Size(w * 0.18f, h * 0.64f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.57f, h * 0.18f),
            size = Size(w * 0.18f, h * 0.64f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )
    }
}
