package com.example.trueline_listener.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import com.example.trueline_listener.ui.IndianCitiesData
import com.example.trueline_listener.ui.OnboardingProgressHeader
import com.example.trueline_listener.ui.TrueLineWaveformLoader
import com.example.trueline_listener.ui.theme.*

@Composable
fun BasicProfileScreen(viewModel: OnboardingViewModel) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showAgeDialog by remember { mutableStateOf(false) }
    var showCitySearchDialog by remember { mutableStateOf(false) }
    var citySearchQuery by remember { mutableStateOf("") }

    val ageList = (18..70).map { it.toString() }

    val availableLanguages = listOf(
        "Hindi", "English", "Bhojpuri", "Bengali", "Marathi",
        "Telugu", "Tamil", "Gujarati", "Kannada", "Malayalam",
        "Punjabi", "Odia", "Assamese"
    )

    val filteredCities = remember(citySearchQuery) {
        if (citySearchQuery.isBlank()) {
            IndianCitiesData.cities.take(50)
        } else {
            IndianCitiesData.cities.filter {
                it.contains(citySearchQuery, ignoreCase = true)
            }.take(50)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            },
        color = Light
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                OnboardingProgressHeader(
                    currentStep = 2,
                    totalSteps = 7,
                    titleNormal = "Tell us",
                    titleHighlight = "about you",
                    subtitle = "This helps us match you with the right callers."
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 1. Display Name (Read-Only Assigned Pseudonym)
                Text(
                    text = "DISPLAY NAME",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.fullName.ifBlank { "Assigned Name" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "ASSIGNED",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "🔒 This name has been assigned to protect your privacy & safety.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Age Dropdown Card (Opens custom Age Picker)
                Text(
                    text = "AGE",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            showAgeDialog = true
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (viewModel.age.isNotBlank()) "${viewModel.age} years" else "Select your age",
                            fontSize = 15.sp,
                            fontWeight = if (viewModel.age.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                            color = if (viewModel.age.isNotBlank()) TextPrimary else TextMuted
                        )
                        Text(
                            text = "▼",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. City / State Searchable Dropdown
                Text(
                    text = "CITY / STATE",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            showCitySearchDialog = true
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.cityState.ifBlank { "Select your city in India" },
                            fontSize = 15.sp,
                            fontWeight = if (viewModel.cityState.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (viewModel.cityState.isNotBlank()) TextPrimary else TextMuted
                        )
                        Text(
                            text = "▼",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Languages You Can Speak
                Text(
                    text = "LANGUAGES YOU CAN SPEAK",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select all that apply. Hindi or English is recommended.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Language chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableLanguages.chunked(3).forEach { rowLangs ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowLangs.forEach { lang ->
                                val isSelected = viewModel.selectedLanguages.contains(lang)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Primary else Color.White)
                                        .border(
                                            width = 1.2.dp,
                                            color = if (isSelected) Primary else BorderSubtle,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.toggleLanguage(lang) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isSelected) "$lang ✓" else lang,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                            if (rowLangs.size < 3) {
                                for (i in 0 until (3 - rowLangs.size)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Short Bio (Strictly Optional)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHORT BIO",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "OPTIONAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = viewModel.bio,
                    onValueChange = { viewModel.onBioChanged(it) },
                    placeholder = {
                        Text(
                            text = "e.g. Friendly listener from Mumbai. Love listening about daily life, relationships, and work stories.",
                            color = TextMuted,
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Primary
                    )
                )

                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = Danger,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.submitProfile()
                    },
                    enabled = viewModel.isProfileValid && !viewModel.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.4f)
                    )
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
    }

    // Horizontal Carousel Age Selection Dialog (Matching Reference Screenshot)
    if (showAgeDialog) {
        var tempAge by remember { mutableStateOf(viewModel.age.toIntOrNull() ?: 24) }
        val ages = (18..70).toList()
        val coroutineScope = rememberCoroutineScope()
        val initialIndex = (tempAge - 18).coerceIn(0, ages.size - 1)
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = (initialIndex - 1).coerceAtLeast(0))

        // Dynamically compute the centered age as the user scrolls
        val centerAge by remember {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) return@derivedStateOf tempAge

                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val closestItem = visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    kotlin.math.abs(itemCenter - viewportCenter)
                }
                val targetIdx = closestItem?.index ?: (tempAge - 18)
                ages.getOrElse(targetIdx) { tempAge }
            }
        }

        LaunchedEffect(centerAge) {
            tempAge = centerAge
        }

        Dialog(onDismissRequest = { showAgeDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Your age",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "We use this to tailor the listener fleet and match with callers.",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(
                            onClick = { showAgeDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("✕", fontSize = 18.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Horizontally Scrollable Number Carousel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LazyRow(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 110.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ages) { ageVal ->
                                val isSelected = ageVal == tempAge
                                val diff = kotlin.math.abs(ageVal - tempAge)

                                val animatedScale by animateFloatAsState(
                                    targetValue = when (diff) {
                                        0 -> 1.0f
                                        1 -> 0.72f
                                        2 -> 0.52f
                                        else -> 0.40f
                                    },
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                                    label = "AgeScale"
                                )
                                val animatedAlpha by animateFloatAsState(
                                    targetValue = when (diff) {
                                        0 -> 1.0f
                                        1 -> 0.65f
                                        2 -> 0.35f
                                        else -> 0.15f
                                    },
                                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                    label = "AgeAlpha"
                                )

                                Box(
                                    modifier = Modifier
                                        .width(68.dp)
                                        .height(80.dp)
                                        .clickable {
                                            tempAge = ageVal
                                            val targetIdx = (ageVal - 18 - 1).coerceAtLeast(0)
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(targetIdx)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$ageVal",
                                        fontSize = 46.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        softWrap = false,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .scale(animatedScale)
                                            .alpha(animatedAlpha)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stepper / Adjuster buttons
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (tempAge > 18) {
                                    tempAge--
                                    val targetIdx = (tempAge - 18 - 1).coerceAtLeast(0)
                                    coroutineScope.launch { listState.animateScrollToItem(targetIdx) }
                                }
                            },
                            enabled = tempAge > 18
                        ) {
                            Text("◀", fontSize = 18.sp, color = if (tempAge > 18) Primary else BorderSubtle)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Scroll or tap arrows to adjust",
                            fontSize = 12.5.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = {
                                if (tempAge < 70) {
                                    tempAge++
                                    val targetIdx = (tempAge - 18 - 1).coerceAtLeast(0)
                                    coroutineScope.launch { listState.animateScrollToItem(targetIdx) }
                                }
                            },
                            enabled = tempAge < 70
                        ) {
                            Text("▶", fontSize = 18.sp, color = if (tempAge < 70) Primary else BorderSubtle)
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Button(
                        onClick = {
                            viewModel.onAgeChanged(tempAge.toString())
                            showAgeDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        )
                    ) {
                        Text(
                            text = "Confirm ($tempAge years)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Redesigned City Search Dialog with Popular Metro Chips & State Badges
    if (showCitySearchDialog) {
        val requestLocationPermission = com.example.trueline_listener.location.rememberLocationPermissionRequester(
            onPermissionGranted = {
                viewModel.detectLiveLocation()
            },
            onPermissionDenied = {
                viewModel.setLocationError("Location permission is required to detect your city.")
            }
        )

        val popularCities = remember {
            listOf(
                "Mumbai, Maharashtra",
                "Delhi NCR, Delhi",
                "Bengaluru, Karnataka",
                "Hyderabad, Telangana",
                "Pune, Maharashtra",
                "Kolkata, West Bengal",
                "Patna, Bihar",
                "Lucknow, Uttar Pradesh",
                "Jaipur, Rajasthan",
                "Ahmedabad, Gujarat",
                "Chandigarh, Punjab",
                "Indore, Madhya Pradesh",
                "Chennai, Tamil Nadu",
                "Bhopal, Madhya Pradesh"
            )
        }

        Dialog(onDismissRequest = { showCitySearchDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select your city",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Connects you with callers from your region",
                                fontSize = 12.5.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(
                            onClick = { showCitySearchDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("✕", fontSize = 18.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live GPS Location Auto-Detection Card
                    val detectedLoc = viewModel.detectedLiveCity
                    val isDetecting = viewModel.isDetectingLocation

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !isDetecting) {
                                viewModel.clearLocationError()
                                requestLocationPermission()
                            },
                        color = if (detectedLoc != null) Color(0xFFF0FDF4) else Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            if (detectedLoc != null) OnlineSuccess else Color(0xFF93C5FD)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (detectedLoc != null) OnlineSuccess.copy(alpha = 0.16f) else Color(0xFF2563EB).copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isDetecting) "⏳" else "📍",
                                        fontSize = 17.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isDetecting) "Detecting live GPS location..." 
                                               else if (detectedLoc != null) "Detected: ${detectedLoc.formatted}" 
                                               else "Use Live Location",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (detectedLoc != null) Color(0xFF15803D) else Color(0xFF1E40AF)
                                    )
                                    Text(
                                        text = if (isDetecting) "Fetching city from GPS satellites..." 
                                               else if (detectedLoc != null) "Verified real location via GPS" 
                                               else "Auto-detect verified city to avoid fake info",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (detectedLoc != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = OnlineSuccess.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "GPS ✓",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnlineSuccess,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (viewModel.locationErrorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ " + (viewModel.locationErrorMessage ?: ""),
                            fontSize = 11.5.sp,
                            color = Accent,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search input
                    TextField(
                        value = citySearchQuery,
                        onValueChange = { citySearchQuery = it },
                        placeholder = { Text("Search city or state...", color = TextMuted, fontSize = 14.sp) },
                        leadingIcon = { Text("🔍", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp)) },
                        trailingIcon = {
                            if (citySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { citySearchQuery = "" }) {
                                    Text("✕", fontSize = 14.sp, color = TextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.2.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Light,
                            unfocusedContainerColor = Light,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Regional Suggestions when Live Location is detected
                    if (citySearchQuery.isBlank() && detectedLoc != null) {
                        val stateFilter = detectedLoc.state
                        val regionalCities = remember(detectedLoc) {
                            IndianCitiesData.cities.filter { it.contains(stateFilter, ignoreCase = true) }.take(10)
                        }

                        if (regionalCities.isNotEmpty()) {
                            Text(
                                text = "SUGGESTED CITIES (${stateFilter.uppercase()})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(regionalCities) { regCity ->
                                    val cityName = regCity.split(",")[0]
                                    val isSelected = viewModel.cityState == regCity
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Primary else Color(0xFFF0FDF4))
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Primary else OnlineSuccess.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                viewModel.onCityStateChanged(regCity)
                                                showCitySearchDialog = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = cityName,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFF15803D)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = BorderSubtle)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Quick-pick Metro Chips
                    if (citySearchQuery.isBlank()) {
                        Text(
                            text = "POPULAR CITIES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(popularCities) { metro ->
                                val cityName = metro.split(",")[0]
                                val isSelected = viewModel.cityState == metro
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Primary else Color(0xFFEFF7F8))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Primary else BorderSubtle,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            viewModel.onCityStateChanged(metro)
                                            showCitySearchDialog = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = cityName,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                    }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Filtered City List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredCities) { city ->
                            val parts = city.split(",")
                            val cityName = parts.getOrNull(0)?.trim() ?: city
                            val stateName = parts.getOrNull(1)?.trim() ?: ""
                            val isSelected = viewModel.cityState == city

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Primary.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable {
                                        viewModel.onCityStateChanged(city)
                                        showCitySearchDialog = false
                                        citySearchQuery = ""
                                    }
                                    .padding(vertical = 10.dp, horizontal = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cityName,
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Primary else TextPrimary
                                        )
                                        if (stateName.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFEFF5F6))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = stateName,
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Text("✓", fontSize = 16.sp, color = Primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }
}
