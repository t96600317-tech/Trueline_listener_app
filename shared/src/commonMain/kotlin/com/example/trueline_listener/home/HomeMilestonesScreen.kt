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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.theme.*

data class TrainingLessonItem(
    val number: Int,
    val title: String,
    val durationText: String,
    val isCompleted: Boolean,
    val isRequired: Boolean = false
)

@Composable
fun HomeMilestonesScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()

    val lessons = listOf(
        TrainingLessonItem(
            number = 1,
            title = "Your first call",
            durationText = "Completed · 7 min",
            isCompleted = true
        ),
        TrainingLessonItem(
            number = 2,
            title = "Safety and boundaries",
            durationText = "Completed · 11 min",
            isCompleted = true
        ),
        TrainingLessonItem(
            number = 4,
            title = "When a caller is in crisis",
            durationText = "12 min · required",
            isCompleted = false,
            isRequired = true
        ),
        TrainingLessonItem(
            number = 5,
            title = "Ending a call kindly",
            durationText = "8 min",
            isCompleted = false
        ),
        TrainingLessonItem(
            number = 6,
            title = "Building repeat callers",
            durationText = "9 min",
            isCompleted = false
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFB))
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header Section: Title & Subtitle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Column {
                Text(
                    text = "Listen better, earn more",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Six short lessons. Ten minutes each.",
                    fontSize = 13.5.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Hero Active Lesson Card (Dark Forest Green)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E4D43) // Dark Forest Teal
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Label
                Text(
                    text = "CONTINUE · LESSON 3",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7DD3C7),
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Lesson Title
                Text(
                    text = "Holding silence without filling it",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar (Custom Track & Amber Fill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF336359))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.44f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(Color(0xFFE58B58))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Row: Duration & Resume Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "4 of 9 minutes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFE58B58),
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { }
                    ) {
                        Text(
                            text = "Resume",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E4D43),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Grouped Lessons List Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column {
                lessons.forEachIndexed { index, lesson ->
                    LessonRowItem(lesson = lesson)
                    if (index < lessons.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Bottom Notice Banner (Warm Cream/Peach)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFDF0E7),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF7D7C4))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Finish all six by 31 Aug",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C4124)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Trained listeners hold a 4.8+ rating twice as often.",
                    fontSize = 12.sp,
                    color = Color(0xFF9A644D),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun LessonRowItem(lesson: TrainingLessonItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Circle (Checkmark for completed, Number for upcoming)
        if (lesson.isCompleted) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = Color(0xFF1E4D43)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${lesson.number}",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Lesson Title & Duration / Status
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lesson.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = lesson.durationText,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
