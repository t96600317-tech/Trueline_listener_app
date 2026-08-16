package com.example.trueline_listener.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trueline_listener.ui.theme.*

@Composable
fun BasicProfileScreen(
    viewModel: OnboardingViewModel
) {
    val scrollState = rememberScrollState()
    val availableLanguages = listOf(
        "Hindi", "English", "Bhojpuri", "Bengali", "Tamil", "Telugu", "Marathi", "Gujarati", "Kannada", "Malayalam"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Light)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Basic Profile",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Dark
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tell us a bit about yourself",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = TextMutedGrey
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Full Name
        ProfileTextField(
            label = "Full Name",
            value = viewModel.fullName,
            onValueChange = viewModel::onFullNameChange,
            placeholder = "Enter your display name"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Age
        ProfileTextField(
            label = "Age",
            value = viewModel.age,
            onValueChange = viewModel::onAgeChange,
            placeholder = "Must be 18+",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        if (viewModel.age.isNotEmpty() && (viewModel.age.toIntOrNull() ?: 0) < 18) {
            Text(
                text = "Must be 18 years or older",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Languages
        Text(
            text = "Languages spoken",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Dark
            ),
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableLanguages.forEach { language ->
                val isSelected = viewModel.selectedLanguages.contains(language)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleLanguage(language) },
                    label = { Text(language) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceWhite,
                        labelColor = Dark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = BorderSubtle,
                        selectedBorderColor = Primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // City/State
        ProfileTextField(
            label = "City / State",
            value = viewModel.cityState,
            onValueChange = viewModel::onCityStateChange,
            placeholder = "e.g. Mumbai, Maharashtra"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bio
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Short Bio (Optional)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark
                    )
                )
                Text(
                    text = "${viewModel.bio.length}/200",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMutedGrey)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.bio,
                onValueChange = viewModel::onBioChange,
                modifier = Modifier.fillMaxWidth().height(100.dp),
                placeholder = { Text("Tell us a little about yourself...", color = TextMutedGrey.copy(alpha = 0.5f)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                )
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { viewModel.submitProfile() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = viewModel.isProfileValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                disabledContainerColor = AccentDisabled
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Next",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Dark
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextMutedGrey.copy(alpha = 0.5f)) },
            keyboardOptions = keyboardOptions,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite
            )
        )
    }
}
