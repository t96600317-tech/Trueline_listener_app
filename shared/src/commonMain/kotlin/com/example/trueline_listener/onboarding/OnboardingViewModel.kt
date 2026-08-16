package com.example.trueline_listener.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.TimeSource
import kotlin.time.Duration.Companion.seconds

enum class OnboardingStep {
    PHONE_INPUT,
    OTP_VERIFICATION,
    PROFILE_SETUP,
    VOICE_INTRO,
    FACE_VERIFICATION,
    KYC_DOCUMENT // Step 5
}

class OnboardingViewModel(private val scope: CoroutineScope) {

    // --- Step 1 State ---
    var phoneNumber by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    // --- Step 2 State ---
    var fullName by mutableStateOf("")
        private set

    var age by mutableStateOf("")
        private set

    var selectedLanguages by mutableStateOf(setOf<String>())
        private set

    var cityState by mutableStateOf("")
        private set

    var bio by mutableStateOf("")
        private set

    // --- Step 3 State ---
    var isRecording by mutableStateOf(false)
        private set

    var recordingDuration by mutableStateOf(0) // in seconds
        private set

    var recordedVoicePath by mutableStateOf<String?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    // --- Step 4 State ---
    enum class FaceVerificationStatus { IDLE, CAPTURING, VERIFYING, SUCCESS, FAILED, MANUAL_REVIEW }
    enum class LivenessPhase { CENTER_FACE, TURN_LEFT, TURN_RIGHT, BLINK, COMPLETE }
    
    var faceVerificationStatus by mutableStateOf(FaceVerificationStatus.IDLE)
        private set

    var livenessPhase by mutableStateOf(LivenessPhase.CENTER_FACE)
        private set

    var livenessPrompt by mutableStateOf("Position your face in the oval frame")
        private set

    var faceRetryCount by mutableStateOf(0)
        private set

    private val _currentStep = MutableStateFlow(OnboardingStep.PHONE_INPUT)
    val currentStep: StateFlow<OnboardingStep> = _currentStep.asStateFlow()

    private val _resendTimer = MutableStateFlow(0)
    val resendTimer: StateFlow<Int> = _resendTimer.asStateFlow()

    private var timerJob: Job? = null

    // --- Step 1 Validation ---
    val isPhoneValid: Boolean
        get() = phoneNumber.length == 10 && phoneNumber.all { it.isDigit() }

    val isOtpValid: Boolean
        get() = otp.length == 6 && otp.all { it.isDigit() }

    // --- Step 2 Validation ---
    val isProfileValid: Boolean
        get() = fullName.isNotBlank() &&
                age.toIntOrNull()?.let { it >= 18 } == true &&
                selectedLanguages.isNotEmpty() &&
                cityState.isNotBlank()

    // --- Step 1 Actions ---
    fun onPhoneNumberChange(newNumber: String) {
        if (newNumber.length <= 10 && newNumber.all { it.isDigit() }) {
            phoneNumber = newNumber
        }
    }

    fun onOtpChange(newOtp: String) {
        if (newOtp.length <= 6 && newOtp.all { it.isDigit() }) {
            otp = newOtp
        }
    }

    // --- Step 2 Actions ---
    fun onFullNameChange(newName: String) {
        fullName = newName
    }

    fun onAgeChange(newAge: String) {
        if (newAge.length <= 2 && (newAge.isEmpty() || newAge.all { it.isDigit() })) {
            age = newAge
        }
    }

    fun toggleLanguage(language: String) {
        selectedLanguages = if (selectedLanguages.contains(language)) {
            selectedLanguages - language
        } else {
            selectedLanguages + language
        }
    }

    fun onCityStateChange(newCityState: String) {
        cityState = newCityState
    }

    fun onBioChange(newBio: String) {
        if (newBio.length <= 200) {
            bio = newBio
        }
    }

    // --- Step 3 Actions ---
    private var recordingJob: Job? = null

    fun startRecording() {
        if (!isRecording) {
            isRecording = true
            recordingDuration = 0
            recordedVoicePath = null
            recordingJob = scope.launch {
                while (isRecording && recordingDuration < 20) {
                    delay(1000)
                    recordingDuration += 1
                }
                if (recordingDuration >= 20) {
                    stopRecording()
                }
            }
        }
    }

    fun stopRecording() {
        if (isRecording) {
            isRecording = false
            recordingJob?.cancel()
            // Mock saved path
            if (recordingDuration >= 10) {
                recordedVoicePath = "mock/path/voice_intro.m4a"
            }
        }
    }

    fun togglePlayback() {
        if (recordedVoicePath != null) {
            isPlaying = !isPlaying
            if (isPlaying) {
                scope.launch {
                    delay(recordingDuration * 1000L)
                    isPlaying = false
                }
            }
        }
    }

    fun deleteRecording() {
        recordedVoicePath = null
        isPlaying = false
        recordingDuration = 0
    }

    fun submitVoiceIntro() {
        if (recordedVoicePath != null) {
            _currentStep.value = OnboardingStep.FACE_VERIFICATION
        }
    }

    // --- Step 4 Actions ---
    private var phaseTimeMark: TimeSource.Monotonic.ValueTimeMark? = null

    fun startFaceVerification() {
        faceVerificationStatus = FaceVerificationStatus.CAPTURING
        livenessPhase = LivenessPhase.CENTER_FACE
        livenessPrompt = "Position your face in the oval frame"
    }

    fun onFaceDetected(
        isCentered: Boolean,
        headEulerY: Float,
        leftEyeOpenProb: Float?,
        rightEyeOpenProb: Float?
    ) {
        if (faceVerificationStatus != FaceVerificationStatus.CAPTURING) return

        when (livenessPhase) {
            LivenessPhase.CENTER_FACE -> {
                if (isCentered) {
                    livenessPhase = LivenessPhase.TURN_LEFT
                    livenessPrompt = "Turn your head slowly to the left"
                    phaseTimeMark = null
                }
            }
            LivenessPhase.TURN_LEFT -> {
                if (headEulerY > 20f) {
                    trackPhaseProgress {
                        livenessPhase = LivenessPhase.TURN_RIGHT
                        livenessPrompt = "Turn your head slowly to the right"
                    }
                } else {
                    resetPhaseProgress()
                }
            }
            LivenessPhase.TURN_RIGHT -> {
                if (headEulerY < -20f) {
                    trackPhaseProgress {
                        livenessPhase = LivenessPhase.BLINK
                        livenessPrompt = "Blink your eyes naturally"
                    }
                } else {
                    resetPhaseProgress()
                }
            }
            LivenessPhase.BLINK -> {
                if (leftEyeOpenProb != null && rightEyeOpenProb != null &&
                    leftEyeOpenProb < 0.25f && rightEyeOpenProb < 0.25f) {
                    livenessPhase = LivenessPhase.COMPLETE
                    livenessPrompt = "Verifying..."
                    completeLiveness()
                }
            }
            LivenessPhase.COMPLETE -> {}
        }
    }

    private fun trackPhaseProgress(onComplete: () -> Unit) {
        val mark = phaseTimeMark
        if (mark == null) {
            phaseTimeMark = TimeSource.Monotonic.markNow()
        } else if (mark.elapsedNow() >= 3.seconds) {
            onComplete()
            phaseTimeMark = null
        }
    }

    private fun resetPhaseProgress() {
        phaseTimeMark = null
    }

    private fun completeLiveness() {
        faceVerificationStatus = FaceVerificationStatus.VERIFYING
        scope.launch {
            delay(2000)
            faceVerificationStatus = FaceVerificationStatus.SUCCESS
            livenessPrompt = "✓ Verification Successful"
        }
    }
    fun retryFaceVerification() {
        if (faceRetryCount < 2) {
            faceRetryCount++
            startFaceVerification()
        } else {
            faceVerificationStatus = FaceVerificationStatus.MANUAL_REVIEW
            livenessPrompt = "Routing to Manual Review"
        }
    }

    fun proceedFromFaceVerification() {
        if (faceVerificationStatus == FaceVerificationStatus.SUCCESS || faceVerificationStatus == FaceVerificationStatus.MANUAL_REVIEW) {
            _currentStep.value = OnboardingStep.KYC_DOCUMENT
        }
    }

    fun sendOtp() {
        if (isPhoneValid) {
            // Mock sending OTP
            _currentStep.value = OnboardingStep.OTP_VERIFICATION
            startResendTimer()
        }
    }

    fun verifyOtp() {
        if (isOtpValid) {
            // Mock verification success
            _currentStep.value = OnboardingStep.PROFILE_SETUP
        }
    }

    fun submitProfile() {
        if (isProfileValid) {
            _currentStep.value = OnboardingStep.VOICE_INTRO
        }
    }

    fun resendOtp() {
        if (_resendTimer.value == 0) {
            // Mock resending OTP
            startResendTimer()
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        _resendTimer.value = 30
        timerJob = scope.launch {
            while (_resendTimer.value > 0) {
                delay(1000)
                _resendTimer.value -= 1
            }
        }
    }

    fun backToPhoneInput() {
        _currentStep.value = OnboardingStep.PHONE_INPUT
        timerJob?.cancel()
        _resendTimer.value = 0
        otp = ""
    }
}
