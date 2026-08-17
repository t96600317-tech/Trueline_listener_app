package com.example.trueline_listener

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.trueline_listener.home.*
import com.example.trueline_listener.network.ListenerRepository
import com.example.trueline_listener.onboarding.*
import com.example.trueline_listener.ui.theme.*

@Composable
@Preview
fun App(
    onboardingViewModel: OnboardingViewModel? = null
) {
    val scope = rememberCoroutineScope()
    val repository = remember { ListenerRepository() }
    val vm = onboardingViewModel ?: remember { OnboardingViewModel(scope) }
    val portalViewModel = remember { MainPortalViewModel(scope, repository) }

    val currentStep by vm.currentStep.collectAsState()
    var showApprovedHome by remember { mutableStateOf(false) }

    LaunchedEffect(vm.isApprovedListener, currentStep) {
        if (vm.isApprovedListener || currentStep == OnboardingStep.APPROVED_WELCOME) {
            showApprovedHome = true
            portalViewModel.refreshAllData()
        }
    }

    LaunchedEffect(Unit) {
        vm.onEnterPortal = {
            showApprovedHome = true
            portalViewModel.refreshAllData()
        }
        portalViewModel.onLogout = {
            showApprovedHome = false
            vm.resetToStart()
        }
    }

    TrueLineTheme(useDarkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .fillMaxSize()
            ) {
                if (vm.isCheckingSession) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            com.example.trueline_listener.ui.TrueLineLogo(size = 64.dp)
                            Spacer(modifier = Modifier.height(20.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Primary,
                                strokeWidth = 2.5.dp
                            )
                        }
                    }
                } else if (showApprovedHome) {
                    MainPortalScreen(portalViewModel)
                } else {
                    when (currentStep) {
                        OnboardingStep.PHONE_INPUT -> {
                            PhoneInputScreen(vm)
                        }
                        OnboardingStep.OTP_VERIFICATION -> {
                            OtpVerificationScreen(vm)
                        }
                        OnboardingStep.PROFILE_SETUP -> {
                            BasicProfileScreen(vm)
                        }
                        OnboardingStep.VOICE_INTRO -> {
                            VoiceIntroScreen(vm)
                        }
                        OnboardingStep.FACE_VERIFICATION -> {
                            FaceVerificationScreen(vm)
                        }
                        OnboardingStep.FACE_RETRY -> {
                            FaceRetryScreen(vm)
                        }
                        OnboardingStep.KYC_DOCUMENT -> {
                            KycDocumentScreen(vm)
                        }
                        OnboardingStep.AGREEMENT -> {
                            AgreementScreen(vm)
                        }
                        OnboardingStep.SUBMITTED_PENDING_APPROVAL -> {
                            ApplicationPendingScreen(vm)
                        }
                        OnboardingStep.APPROVED_WELCOME -> {
                            WelcomeApprovedScreen(vm)
                        }
                    }
                }
            }
        }
    }
}
