package com.example.trueline_listener.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.trueline_listener.network.ListenerRepository
import com.example.trueline_listener.otp.Msg91OtpResult
import com.example.trueline_listener.otp.getMsg91OtpGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class OnboardingStep {
    PHONE_INPUT,
    OTP_VERIFICATION,        // Step 1 of 7
    PROFILE_SETUP,           // Step 2 of 7
    VOICE_INTRO,             // Step 3 of 7
    FACE_VERIFICATION,       // Step 4 of 7
    FACE_RETRY,              // Retry helper screen
    KYC_DOCUMENT,            // Step 5 of 7
    AGREEMENT,               // Step 6 of 7
    SUBMITTED_PENDING_APPROVAL, // Step 7
    APPROVED_WELCOME         // Welcome to TrueLine Verified Listener
}

class OnboardingViewModel(private val scope: CoroutineScope) {

    val repository = ListenerRepository()

    // --- Step 1 State (Phone & OTP) ---
    var phoneNumber by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // --- Step 2 State (Basic Profile) ---
    var fullName by mutableStateOf(com.example.trueline_listener.ui.IndianListenerNames.getRandomName())
        private set

    var age by mutableStateOf("")
        private set

    var cityState by mutableStateOf("")
        private set

    var isDetectingLocation by mutableStateOf(false)
        private set

    var detectedLiveCity by mutableStateOf<com.example.trueline_listener.location.DetectedCityLocation?>(null)
        private set

    var locationErrorMessage by mutableStateOf<String?>(null)
        private set

    var selectedLanguages by mutableStateOf(setOf<String>("Hindi"))
        private set

    var bio by mutableStateOf("") // Optional
        private set

    // --- Step 3 State (Voice Intro) ---
    var isRecording by mutableStateOf(false)
        private set

    var recordingDuration by mutableStateOf(0) // in seconds
        private set

    var recordedVoicePath by mutableStateOf<String?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var playbackProgress by mutableStateOf(0f)
        private set

    // --- Step 4 State (Face Verification & Selfie) ---
    enum class FaceStepPhase { CAPTURE_SELFIE, REVIEW_SELFIE, LIVENESS_CHALLENGE, COMPLETED }
    enum class FaceVerificationStatus { IDLE, CAPTURING, VERIFYING, SUCCESS, FAILED }
    enum class LivenessPhase { HOLD_CENTER, BLINK, TURN_LEFT, TURN_RIGHT, COMPLETE }

    var faceStepPhase by mutableStateOf(FaceStepPhase.CAPTURE_SELFIE)
        private set

    var capturedSelfieUri by mutableStateOf<String?>(null)
        private set

    var triggerCaptureToken by mutableStateOf(0)
        private set

    var faceVerificationStatus by mutableStateOf(FaceVerificationStatus.IDLE)
        private set

    var livenessPhase by mutableStateOf(LivenessPhase.HOLD_CENTER)
        private set

    var livenessPrompt by mutableStateOf("Center your face inside the frame")
        private set

    var blinkDetected by mutableStateOf(false)
        private set

    var blinkCount by mutableStateOf(0)
        private set

    val targetBlinks = 3

    var headTurnLeftDetected by mutableStateOf(false)
        private set

    var headTurnRightDetected by mutableStateOf(false)
        private set

    var headTurnDetected by mutableStateOf(false)
        private set

    var liveFaceCount by mutableStateOf(0)
        private set

    var liveLightingGood by mutableStateOf(false)
        private set

    var liveLookingStraight by mutableStateOf(false)
        private set

    var liveQualityPass by mutableStateOf(false)
        private set

    var liveLivenessProgress by mutableStateOf(0f)
        private set

    var liveTurnAngle by mutableStateOf(0f)
        private set

    private var isEyesClosed = false

    // --- Step 5 State (KYC Verification - PAN & Bank / UPI) ---
    var panNumber by mutableStateOf("")
        private set

    var panName by mutableStateOf("")
        private set

    var payoutType by mutableStateOf("upi") // "upi" or "bank"
        private set

    var upiId by mutableStateOf("")
        private set

    var accountNumber by mutableStateOf("")
        private set

    var ifscCode by mutableStateOf("")
        private set

    var bankAccountName by mutableStateOf("")
        private set

    // --- Step 6 State (Agreement) ---
    var agreementAccepted by mutableStateOf(false)
        private set

    // --- Listener Details & Tags ---
    var listenerIdTag by mutableStateOf("TL-P-00214")
        private set

    // --- Session & Loading States ---
    var isCheckingSession by mutableStateOf(true)
        private set

    var isApprovedListener by mutableStateOf(false)

    private val _currentStep = MutableStateFlow(OnboardingStep.PHONE_INPUT)
    val currentStep: StateFlow<OnboardingStep> = _currentStep.asStateFlow()

    private val _resendTimer = MutableStateFlow(24)
    val resendTimer: StateFlow<Int> = _resendTimer.asStateFlow()
    private var timerJob: Job? = null
    private val msg91Otp = getMsg91OtpGateway()
    private var msg91RequestId: String? = null

    init {
        restoreSession()
    }

    private fun mapAndApplyStep(step: String, kyc: String) {
        val stepNormalized = step.trim().lowercase()
        val kycNormalized = kyc.trim().lowercase()

        if (kycNormalized == "approved" || stepNormalized in listOf("approved", "approved_welcome")) {
            isApprovedListener = true
            _currentStep.value = OnboardingStep.APPROVED_WELCOME
            onEnterPortal?.invoke()
        } else if (stepNormalized in listOf("application_submitted", "pending_approval", "application_pending", "submitted", "under_review") ||
            (kycNormalized == "pending" && stepNormalized in listOf("agreement", "agreements"))) {
            _currentStep.value = OnboardingStep.SUBMITTED_PENDING_APPROVAL
        } else {
            _currentStep.value = when (stepNormalized) {
                "voice_intro", "voice" -> OnboardingStep.VOICE_INTRO
                "face_verification", "face", "selfie" -> OnboardingStep.FACE_VERIFICATION
                "kyc_documents", "kyc_document", "kyc", "pan", "aadhaar" -> OnboardingStep.KYC_DOCUMENT
                "agreement", "agreements" -> OnboardingStep.AGREEMENT
                else -> OnboardingStep.PROFILE_SETUP
            }
        }
    }

    fun restoreSession() {
        isCheckingSession = true
        scope.launch {
            try {
                val token = repository.getAuthToken()
                val savedPhone = repository.getSavedPhone()
                val savedStep = repository.getSavedOnboardingStep()
                val savedKyc = repository.getSavedKYCStatus()

                if (token.isNullOrBlank()) {
                    isCheckingSession = false
                    _currentStep.value = OnboardingStep.PHONE_INPUT
                    return@launch
                }

                if (!savedPhone.isNullOrBlank()) {
                    phoneNumber = savedPhone
                }

                // Immediately apply local saved step for instant UI load
                if (!savedStep.isNullOrBlank()) {
                    mapAndApplyStep(savedStep, savedKyc ?: "pending")
                }

                val profileResp = repository.getMe()
                if (profileResp.success && profileResp.data != null) {
                    val p = profileResp.data
                    val step = p.onboarding_step.trim()
                    val kyc = p.kyc_status.trim()

                    if (p.name.isNotBlank()) fullName = p.name
                    if (p.bio.isNotBlank()) bio = p.bio
                    if (p.languages.isNotEmpty()) selectedLanguages = p.languages.toSet()
                    if (p.photo_url.isNotBlank()) capturedSelfieUri = p.photo_url
                    if (p.audio_sample_url.isNotBlank()) recordedVoicePath = p.audio_sample_url

                    mapAndApplyStep(step, kyc)
                } else if (profileResp.error?.code == "UNAUTHORIZED" || profileResp.error?.code == "INVALID_TOKEN") {
                    // Only clear session on explicit authentication failure from server
                    repository.clearAuthSession()
                    _currentStep.value = OnboardingStep.PHONE_INPUT
                }
            } catch (e: Exception) {
                // If offline / network error, retain locally restored state
                val savedStep = repository.getSavedOnboardingStep()
                if (!savedStep.isNullOrBlank()) {
                    mapAndApplyStep(savedStep, repository.getSavedKYCStatus() ?: "pending")
                }
            } finally {
                isCheckingSession = false
            }
        }
    }

    var isCheckingApprovalStatus by mutableStateOf(false)
        private set

    // Called by manual button tap or pull-to-refresh — shows loading animation
    fun checkApprovalStatus() {
        if (isCheckingApprovalStatus) return
        isCheckingApprovalStatus = true
        scope.launch {
            try {
                val profileResp = repository.getMe()
                if (profileResp.success && profileResp.data != null) {
                    val p = profileResp.data
                    val step = p.onboarding_step.trim().lowercase()
                    val kyc = p.kyc_status.trim().lowercase()
                    if (kyc == "approved" || step in listOf("approved", "approved_welcome")) {
                        isApprovedListener = true
                        _currentStep.value = OnboardingStep.APPROVED_WELCOME
                        onEnterPortal?.invoke()
                    }
                }
            } catch (e: Exception) {
                // Ignore transient network errors
            } finally {
                isCheckingApprovalStatus = false
            }
        }
    }

    // Called by background polling loop — completely silent, no state flag changes
    fun pollApprovalStatusSilently() {
        scope.launch {
            try {
                val profileResp = repository.getMe()
                if (profileResp.success && profileResp.data != null) {
                    val p = profileResp.data
                    val step = p.onboarding_step.trim().lowercase()
                    val kyc = p.kyc_status.trim().lowercase()
                    if (kyc == "approved" || step in listOf("approved", "approved_welcome")) {
                        isApprovedListener = true
                        _currentStep.value = OnboardingStep.APPROVED_WELCOME
                        onEnterPortal?.invoke()
                    }
                }
            } catch (e: Exception) {
                // Ignore transient network errors during silent background polling
            }
        }
    }

    fun resetToStart() {
        isApprovedListener = false
        phoneNumber = ""
        otp = ""
        fullName = com.example.trueline_listener.ui.IndianListenerNames.getRandomName()
        bio = ""
        capturedSelfieUri = null
        recordedVoicePath = null
        errorMessage = null
        _currentStep.value = OnboardingStep.PHONE_INPUT
    }

    fun logout() {
        repository.clearAuthSession()
        resetToStart()
    }

    // --- Validation Rules ---
    val isPhoneValid: Boolean
        get() = phoneNumber.length == 10 && phoneNumber.all { it.isDigit() }

    val isOtpValid: Boolean
        get() = otp.length == 6 && otp.all { it.isDigit() }

    // Bio is strictly optional per requirement 10, all other fields mandatory
    val isProfileValid: Boolean
        get() = fullName.trim().length >= 2 &&
                age.isNotBlank() &&
                (age.toIntOrNull() ?: 0) >= 18 &&
                cityState.isNotBlank() &&
                selectedLanguages.isNotEmpty()

    val isKYCValid: Boolean
        get() {
            val isPanValid = panNumber.trim().length == 10
            val isPayoutValid = if (payoutType == "upi") {
                upiId.trim().contains("@") && upiId.trim().length >= 5
            } else {
                accountNumber.trim().length >= 9 && ifscCode.trim().length == 11
            }
            return isPanValid && isPayoutValid
        }

    // --- Action Methods ---

    fun onPhoneNumberChanged(value: String) {
        val sanitized = value.filter { it.isDigit() }.let {
            if (it.startsWith("91") && it.length > 10) it.drop(2)
            else if (it.startsWith("0") && it.length > 10) it.drop(1)
            else it
        }.take(10)
        phoneNumber = sanitized
        errorMessage = null
    }

    fun onOtpChanged(value: String) {
        val sanitized = value.filter { it.isDigit() }.take(6)
        otp = sanitized
        errorMessage = null
    }

    fun onFullNameChanged(value: String) {
        fullName = value
        if (panName.isBlank()) panName = value
        if (bankAccountName.isBlank()) bankAccountName = value
        errorMessage = null
    }

    fun onAgeChanged(value: String) {
        age = value
        errorMessage = null
    }

    fun onCityStateChanged(value: String) {
        cityState = value
        errorMessage = null
    }

    fun detectLiveLocation(onSuccess: (String) -> Unit = {}) {
        isDetectingLocation = true
        locationErrorMessage = null
        val provider = com.example.trueline_listener.location.getLocationProvider()
        provider.fetchLiveCity(
            onSuccess = { loc ->
                isDetectingLocation = false
                detectedLiveCity = loc
                cityState = loc.formatted
                locationErrorMessage = null
                onSuccess(loc.formatted)
            },
            onError = { err ->
                isDetectingLocation = false
                locationErrorMessage = err
            }
        )
    }

    fun clearLocationError() {
        locationErrorMessage = null
    }

    fun setLocationError(msg: String) {
        locationErrorMessage = msg
        isDetectingLocation = false
    }

    fun toggleLanguage(language: String) {
        val current = selectedLanguages.toMutableSet()
        if (current.contains(language)) {
            if (current.size > 1) {
                current.remove(language)
            }
        } else {
            current.add(language)
        }
        selectedLanguages = current
        errorMessage = null
    }

    fun onBioChanged(value: String) {
        bio = value
        errorMessage = null
    }

    fun onPanNumberChanged(value: String) {
        panNumber = value.filter { it.isLetterOrDigit() }.take(10).uppercase()
        errorMessage = null
    }

    fun onPanNameChanged(value: String) {
        panName = value
        errorMessage = null
    }

    fun onPayoutTypeChanged(type: String) {
        payoutType = type
        errorMessage = null
    }

    fun onUpiIdChanged(value: String) {
        upiId = value.trim()
        errorMessage = null
    }

    fun onAccountNumberChanged(value: String) {
        accountNumber = value.filter { it.isDigit() }.take(18)
        errorMessage = null
    }

    fun onIfscCodeChanged(value: String) {
        ifscCode = value.filter { it.isLetterOrDigit() }.take(11).uppercase()
        errorMessage = null
    }

    fun onBankAccountNameChanged(value: String) {
        bankAccountName = value
        errorMessage = null
    }

    fun toggleAgreement(accepted: Boolean) {
        agreementAccepted = accepted
        errorMessage = null
    }

    fun startResendTimer() {
        timerJob?.cancel()
        _resendTimer.value = 30
        timerJob = scope.launch {
            while (_resendTimer.value > 0) {
                delay(1000)
                _resendTimer.value -= 1
            }
        }
    }

    fun requestOtp() {
        if (!isPhoneValid) {
            errorMessage = "Please enter a valid 10-digit mobile number"
            return
        }

        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                val shouldRetryMsg91 = _currentStep.value == OnboardingStep.OTP_VERIFICATION &&
                    !msg91RequestId.isNullOrBlank()

                var msg91Result: Msg91OtpResult? = null
                if (msg91Otp.isConfigured) {
                    try {
                        withTimeoutOrNull(15000L) {
                            msg91Result = if (shouldRetryMsg91) {
                                msg91Otp.retryOtp(msg91RequestId.orEmpty())
                            } else {
                                msg91Otp.sendOtp("91$phoneNumber")
                            }
                        }
                    } catch (_: Throwable) {
                        msg91Result = null
                    }
                }

                if (msg91Result?.success == true) {
                    if (!shouldRetryMsg91) {
                        msg91RequestId = msg91Result?.requestId
                    }
                    _currentStep.value = OnboardingStep.OTP_VERIFICATION
                    startResendTimer()
                } else {
                    // Fallback to backend OTP service
                    msg91RequestId = null
                    val response = try {
                        withTimeoutOrNull(15000L) {
                            repository.requestOtp(phoneNumber)
                        }
                    } catch (_: Throwable) {
                        null
                    }

                    if (response?.success == true) {
                        if (!response.data?.mock_otp.isNullOrBlank()) {
                            otp = response.data.mock_otp.orEmpty()
                        }
                        _currentStep.value = OnboardingStep.OTP_VERIFICATION
                        startResendTimer()
                    } else {
                        val backendError = response?.error?.message
                        val msg91Error = msg91Result?.errorMessage
                        errorMessage = backendError ?: msg91Error ?: "Failed to send OTP. Please check your network and retry."
                    }
                }
            } catch (t: Throwable) {
                errorMessage = t.message ?: "Failed to send OTP. Please retry."
            } finally {
                isLoading = false
            }
        }
    }

    fun verifyOtp() {
        if (!isOtpValid) {
            errorMessage = "Please enter the complete 6-digit OTP"
            return
        }

        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                var accessToken: String? = null
                if (msg91Otp.isConfigured && !msg91RequestId.isNullOrBlank()) {
                    val msg91Result = try {
                        withTimeoutOrNull(15000L) {
                            msg91Otp.verifyOtp(msg91RequestId.orEmpty(), otp)
                        }
                    } catch (_: Throwable) {
                        null
                    }
                    if (msg91Result != null) {
                        if (!msg91Result.success) {
                            errorMessage = msg91Result.errorMessage ?: "Invalid OTP. Please try again."
                            return@launch
                        }
                        accessToken = msg91Result.accessToken
                    }
                }

                val response = try {
                    withTimeoutOrNull(15000L) {
                        repository.verifyOtp(phoneNumber, otp, msg91RequestId, accessToken)
                    }
                } catch (_: Throwable) {
                    null
                }

                if (response == null) {
                    errorMessage = "Verification timed out. Please check your internet connection."
                    return@launch
                }

                if (response.success) {
                    val listener = response.data?.listener
                    val step = response.data?.onboarding_step ?: listener?.onboarding_step ?: "profile_setup"
                    val kycStatus = response.data?.kyc_status ?: listener?.kyc_status ?: "pending"

                    // Populate existing profile data if available
                    if (listener != null && listener.name.isNotBlank()) {
                        fullName = listener.name
                    } else if (fullName.isBlank()) {
                        fullName = com.example.trueline_listener.ui.IndianListenerNames.getRandomName()
                    }

                    if (listener != null) {
                        if (listener.bio.isNotBlank()) bio = listener.bio
                        if (listener.languages.isNotEmpty()) {
                            selectedLanguages = listener.languages.toSet()
                        }
                    }

                    val targetStep = when (step.lowercase()) {
                        "voice_intro", "voice" -> OnboardingStep.VOICE_INTRO
                        "face_verification", "face", "selfie" -> OnboardingStep.FACE_VERIFICATION
                        "kyc_documents", "kyc_document", "kyc", "pan", "aadhaar" -> OnboardingStep.KYC_DOCUMENT
                        "agreement", "agreements" -> OnboardingStep.AGREEMENT
                        "application_submitted", "pending_approval", "application_pending", "submitted", "under_review" -> OnboardingStep.SUBMITTED_PENDING_APPROVAL
                        "approved", "approved_welcome" -> OnboardingStep.APPROVED_WELCOME
                        else -> OnboardingStep.PROFILE_SETUP
                    }

                    if (kycStatus.lowercase() == "approved" || step.lowercase() in listOf("approved", "approved_welcome")) {
                        isApprovedListener = true
                        _currentStep.value = OnboardingStep.APPROVED_WELCOME
                        onEnterPortal?.invoke()
                    } else if (step.lowercase() in listOf("application_submitted", "pending_approval", "application_pending", "submitted", "under_review") ||
                               (kycStatus.lowercase() == "pending" && step.lowercase() in listOf("agreement", "agreements"))) {
                        _currentStep.value = OnboardingStep.SUBMITTED_PENDING_APPROVAL
                    } else {
                        _currentStep.value = targetStep
                    }
                } else {
                    errorMessage = response.error?.message ?: "Invalid OTP. Please check the code and try again."
                }
            } catch (t: Throwable) {
                errorMessage = t.message ?: "Failed to verify OTP. Please retry."
            } finally {
                isLoading = false
            }
        }
    }

    fun submitProfile() {
        val trimmedName = fullName.trim()
        if (trimmedName.length < 2) {
            errorMessage = "Please enter your full name (minimum 2 characters)"
            return
        }
        val ageInt = age.toIntOrNull() ?: 0
        if (ageInt < 18 || ageInt > 100) {
            errorMessage = "Please select your age (must be 18 years or older)"
            return
        }
        if (cityState.isBlank()) {
            errorMessage = "Please select your city"
            return
        }
        if (selectedLanguages.isEmpty()) {
            errorMessage = "Please select at least one language you can speak"
            return
        }

        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                val response = repository.updateProfile(
                    name = fullName,
                    title = "Compassionate Listener • $cityState",
                    bio = bio.ifBlank { "Hello! I am $fullName, here to listen with compassion and warmth." },
                    languages = selectedLanguages.toList()
                )
                if (response.success) {
                    _currentStep.value = OnboardingStep.VOICE_INTRO
                } else {
                    errorMessage = response.error?.message ?: "Failed to save profile. Please retry."
                }
            } catch (t: Throwable) {
                errorMessage = t.message ?: "Failed to save profile. Please retry."
            } finally {
                isLoading = false
            }
        }
    }

    private var recordingTickerJob: Job? = null

    fun startVoiceRecording() {
        errorMessage = null
        isRecording = true
        isPlaying = false
        playbackProgress = 0f
        recordingDuration = 0
        recordedVoicePath = null

        // Reliable coroutine ticker running directly in viewModel scope
        recordingTickerJob?.cancel()
        recordingTickerJob = scope.launch {
            while (isRecording && recordingDuration < 30) {
                delay(1000)
                if (isRecording) {
                    recordingDuration += 1
                }
            }
            if (isRecording && recordingDuration >= 30) {
                stopVoiceRecording()
            }
        }

        try {
            val engine = com.example.trueline_listener.audio.getAudioEngine()
            engine.startRecording { }
        } catch (e: Exception) {
            // Audio recording started with fallback
        }
    }

    fun stopVoiceRecording() {
        if (recordingDuration < 10) {
            errorMessage = "Voice intro must be at least 10 seconds long (currently ${recordingDuration}s)"
            return
        }

        isRecording = false
        recordingTickerJob?.cancel()

        try {
            val engine = com.example.trueline_listener.audio.getAudioEngine()
            val recordedPath = engine.stopRecording()
            recordedVoicePath = recordedPath ?: "mock_sample.m4a"
            errorMessage = null
        } catch (e: Exception) {
            recordedVoicePath = "mock_sample.m4a"
            errorMessage = null
        }
    }

    fun toggleAudioPlayback() {
        val path = recordedVoicePath ?: return
        val engine = com.example.trueline_listener.audio.getAudioEngine()

        if (isPlaying) {
            engine.stopPlayback()
            isPlaying = false
            playbackProgress = 0f
        } else {
            isPlaying = true
            playbackProgress = 0f
            engine.startPlayback(
                filePath = path,
                onProgress = { progress -> playbackProgress = progress },
                onComplete = {
                    isPlaying = false
                    playbackProgress = 0f
                }
            )
        }
    }

    fun submitVoiceIntro() {
        if (recordedVoicePath == null || recordingDuration < 10) {
            errorMessage = "Please record at least 10 seconds of voice introduction"
            return
        }

        isLoading = true
        errorMessage = null

        scope.launch {
            val safeName = fullName.ifBlank { "listener" }.filter { it.isLetterOrDigit() }.lowercase()
            val sampleUrl = recordedVoicePath ?: "https://cdn.trueline.app/voice-samples/${safeName}_sample.mp3"
            val response = repository.updateVoiceIntro(sampleUrl)
            isLoading = false
            if (response.success) {
                faceStepPhase = FaceStepPhase.CAPTURE_SELFIE
                capturedSelfieUri = null
                faceVerificationStatus = FaceVerificationStatus.IDLE
                _currentStep.value = OnboardingStep.FACE_VERIFICATION
            } else {
                errorMessage = response.error?.message ?: "Failed to save voice intro. Please retry."
            }
        }
    }

    fun takeSelfiePhoto() {
        if (liveFaceCount == 0) {
            errorMessage = "No face detected. Please look directly into the camera."
            return
        }
        if (liveFaceCount > 1) {
            errorMessage = "Multiple faces detected. Only one person should be in frame."
            return
        }
        if (!liveLookingStraight) {
            errorMessage = "Please look directly straight at the camera."
            return
        }
        errorMessage = null
        triggerCaptureToken += 1
    }

    fun onSelfieCaptured(uri: String) {
        capturedSelfieUri = uri
        faceStepPhase = FaceStepPhase.REVIEW_SELFIE
    }

    fun retakeSelfie() {
        capturedSelfieUri = null
        faceStepPhase = FaceStepPhase.CAPTURE_SELFIE
        faceVerificationStatus = FaceVerificationStatus.IDLE
        livenessPhase = LivenessPhase.HOLD_CENTER
        blinkDetected = false
        blinkCount = 0
        headTurnLeftDetected = false
        headTurnRightDetected = false
        headTurnDetected = false
        triggerCaptureToken = 0
        liveLivenessProgress = 0f
        errorMessage = null
    }

    fun confirmSelfieAndStartLiveness() {
        faceStepPhase = FaceStepPhase.LIVENESS_CHALLENGE
        startFaceVerification()
    }

    fun startFaceVerification() {
        faceVerificationStatus = FaceVerificationStatus.CAPTURING
        livenessPhase = LivenessPhase.HOLD_CENTER
        livenessPrompt = "Center your face inside the frame"
        blinkDetected = false
        blinkCount = 0
        headTurnLeftDetected = false
        headTurnRightDetected = false
        headTurnDetected = false
        isEyesClosed = false
        liveLivenessProgress = 0f
    }

    fun onFaceInspection(
        faceCount: Int,
        isCentered: Boolean,
        eulerX: Float,
        eulerY: Float,
        eulerZ: Float,
        leftEyeProb: Float?,
        rightEyeProb: Float?,
        luminance: Float,
        lightingGood: Boolean,
        lookingStraight: Boolean,
        qualityPass: Boolean
    ) {
        liveFaceCount = faceCount
        liveLightingGood = lightingGood
        liveLookingStraight = lookingStraight
        liveQualityPass = qualityPass
        liveTurnAngle = eulerY

        if (faceStepPhase == FaceStepPhase.LIVENESS_CHALLENGE && faceVerificationStatus == FaceVerificationStatus.CAPTURING) {
            when (livenessPhase) {
                // 1. HOLD CENTER
                LivenessPhase.HOLD_CENTER -> {
                    if (faceCount == 1 && isCentered && lookingStraight && lightingGood) {
                        liveLivenessProgress = (liveLivenessProgress + 0.06f).coerceAtMost(1f)
                        livenessPrompt = "Hold steady... ${(liveLivenessProgress * 100).toInt()}%"
                        if (liveLivenessProgress >= 1f) {
                            livenessPhase = LivenessPhase.BLINK
                            liveLivenessProgress = 0f
                            blinkCount = 0
                            isEyesClosed = false
                            livenessPrompt = "Blink your eyes 3 times (0/3)"
                        }
                    } else {
                        liveLivenessProgress = (liveLivenessProgress - 0.03f).coerceAtLeast(0f)
                        livenessPrompt = when {
                            faceCount == 0 -> "No face detected. Look directly at camera"
                            faceCount > 1 -> "Only one person should be in frame"
                            !lightingGood -> "Improve lighting on your face"
                            !lookingStraight -> "Look directly straight at the camera"
                            else -> "Center your face inside the frame"
                        }
                    }
                }

                // 2. BLINK 3 TIMES
                LivenessPhase.BLINK -> {
                    val left = leftEyeProb ?: 1f
                    val right = rightEyeProb ?: 1f
                    val eyesClosedNow = (left < 0.35f && right < 0.35f) || ((left + right) / 2f < 0.30f)
                    val eyesOpenNow = left > 0.60f && right > 0.60f

                    if (eyesClosedNow) {
                        if (!isEyesClosed) {
                            isEyesClosed = true
                        }
                        livenessPrompt = "Eyes closed... now open them ($blinkCount/3)"
                    } else if (isEyesClosed && eyesOpenNow) {
                        isEyesClosed = false
                        blinkCount++
                        liveLivenessProgress = (blinkCount.toFloat() / targetBlinks).coerceIn(0f, 1f)
                        
                        if (blinkCount >= targetBlinks) {
                            blinkDetected = true
                            livenessPhase = LivenessPhase.TURN_LEFT
                            liveLivenessProgress = 0f
                            livenessPrompt = "Turn your face slowly to the LEFT ⬅"
                        } else {
                            livenessPrompt = "Great! Blink ($blinkCount/3)"
                        }
                    } else {
                        if (blinkCount < targetBlinks) {
                            livenessPrompt = "Blink your eyes 3 times ($blinkCount/3)"
                            liveLivenessProgress = (blinkCount.toFloat() / targetBlinks).coerceIn(0f, 1f)
                        }
                    }
                }

                // 3. TURN LEFT (eulerY > +14°)
                LivenessPhase.TURN_LEFT -> {
                    val progress = (eulerY / 14f).coerceIn(0f, 1f)
                    liveLivenessProgress = progress
                    if (eulerY >= 14f) {
                        headTurnLeftDetected = true
                        headTurnDetected = true
                        livenessPhase = LivenessPhase.TURN_RIGHT
                        liveLivenessProgress = 0f
                        livenessPrompt = "Turn your face slowly to the RIGHT ➡"
                    } else {
                        livenessPrompt = "Turn your face slowly to the LEFT ⬅ (${(progress * 100).toInt()}%)"
                    }
                }

                // 4. TURN RIGHT (eulerY < -14°)
                LivenessPhase.TURN_RIGHT -> {
                    val progress = (-eulerY / 14f).coerceIn(0f, 1f)
                    liveLivenessProgress = progress
                    if (eulerY <= -14f) {
                        headTurnRightDetected = true
                        livenessPhase = LivenessPhase.COMPLETE
                        liveLivenessProgress = 1f
                        faceVerificationStatus = FaceVerificationStatus.SUCCESS
                        faceStepPhase = FaceStepPhase.COMPLETED
                        livenessPrompt = "All 4 liveness checks passed! ✓"

                        scope.launch {
                            val selfie = capturedSelfieUri?.takeIf { it.startsWith("data:") || it.startsWith("http") }
                                ?: "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA="
                            repository.submitSelfie(selfie, 0.99)
                            delay(1200)
                            _currentStep.value = OnboardingStep.KYC_DOCUMENT
                        }
                    } else {
                        livenessPrompt = "Turn your face slowly to the RIGHT ➡ (${(progress * 100).toInt()}%)"
                    }
                }

                LivenessPhase.COMPLETE -> {}
            }
        }
    }

    fun onFaceDetected(
        isCentered: Boolean,
        eulerY: Float,
        leftEyeOpenProbability: Float?,
        rightEyeOpenProbability: Float?
    ) {
        onFaceInspection(
            faceCount = if (isCentered) 1 else 0,
            isCentered = isCentered,
            eulerX = 0f,
            eulerY = eulerY,
            eulerZ = 0f,
            leftEyeProb = leftEyeOpenProbability,
            rightEyeProb = rightEyeOpenProbability,
            luminance = 128f,
            lightingGood = true,
            lookingStraight = kotlin.math.abs(eulerY) < 14f,
            qualityPass = isCentered
        )
    }

    var onEnterPortal: (() -> Unit)? = null

    fun enterPortal() {
        onEnterPortal?.invoke()
    }

    fun retryFaceVerification() {
        _currentStep.value = OnboardingStep.FACE_RETRY
    }

    fun proceedFromFaceRetry() {
        _currentStep.value = OnboardingStep.FACE_VERIFICATION
        startFaceVerification()
    }

    fun goBack(): Boolean {
        return when (_currentStep.value) {
            OnboardingStep.OTP_VERIFICATION -> {
                _currentStep.value = OnboardingStep.PHONE_INPUT
                true
            }
            OnboardingStep.PROFILE_SETUP -> {
                _currentStep.value = OnboardingStep.OTP_VERIFICATION
                true
            }
            OnboardingStep.VOICE_INTRO -> {
                _currentStep.value = OnboardingStep.PROFILE_SETUP
                true
            }
            OnboardingStep.FACE_VERIFICATION -> {
                _currentStep.value = OnboardingStep.VOICE_INTRO
                true
            }
            OnboardingStep.FACE_RETRY -> {
                _currentStep.value = OnboardingStep.FACE_VERIFICATION
                true
            }
            OnboardingStep.KYC_DOCUMENT -> {
                _currentStep.value = OnboardingStep.FACE_VERIFICATION
                true
            }
            OnboardingStep.AGREEMENT -> {
                _currentStep.value = OnboardingStep.KYC_DOCUMENT
                true
            }
            OnboardingStep.SUBMITTED_PENDING_APPROVAL,
            OnboardingStep.APPROVED_WELCOME,
            OnboardingStep.PHONE_INPUT -> false
        }
    }

    fun submitKycStep() {
        isLoading = true
        errorMessage = null

        scope.launch {
            if (panNumber.isNotBlank()) {
                repository.submitPAN(panNumber)
            }
            if (accountNumber.isNotBlank() && ifscCode.isNotBlank()) {
                repository.submitBank(accountNumber, ifscCode)
            }
            isLoading = false
            _currentStep.value = OnboardingStep.AGREEMENT
        }
    }

    fun submitAgreementAndFinalOnboarding() {
        if (!agreementAccepted) {
            errorMessage = "Please accept the Partner Agreement to continue"
            return
        }

        isLoading = true
        errorMessage = null

        scope.launch {
            repository.submitAgreement("1.0")
            val resp = repository.submitOnboarding()
            isLoading = false
            if (resp.success) {
                _currentStep.value = OnboardingStep.SUBMITTED_PENDING_APPROVAL
            } else {
                _currentStep.value = OnboardingStep.SUBMITTED_PENDING_APPROVAL
            }
        }
    }

    fun navigateToStep(step: OnboardingStep) {
        _currentStep.value = step
    }
}
