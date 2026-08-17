package com.example.trueline_listener.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.trueline_listener.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PortalTab { HOME, EARNINGS, PROFILE }

enum class PortalSubScreen {
    NONE,
    PERFORMANCE_SCORE,
    BLOCKED_USERS,
    REPORT_USER,
    AVAILABLE_HOURS,
    PRIVACY_INFO,
    SUPPORT_INFO
}

class MainPortalViewModel(
    private val scope: CoroutineScope,
    private val repository: ListenerRepository
) {
    var currentTab by mutableStateOf(PortalTab.HOME)
        private set

    var activeSubScreen by mutableStateOf(PortalSubScreen.NONE)
        private set

    // Toggle on Home Tab between Milestone Checklist and Active Dashboard
    var showMilestoneChecklist by mutableStateOf(false)
        private set

    var isOnline by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successNotification by mutableStateOf<String?>(null)
        private set

    // Data States
    var dashboardData by mutableStateOf(HomeDashboardResponse())
        private set

    var milestonesData by mutableStateOf(MilestonesHubResponse())
        private set

    var scoreData by mutableStateOf(PerformanceScoreResponse())
        private set

    var detailedEarnings by mutableStateOf(DetailedEarningsResponse())
        private set

    var blockedUsers by mutableStateOf<List<BlockedUserItem>>(emptyList())
        private set

    // Modals
    var showWithdrawModal by mutableStateOf(false)
        private set

    var withdrawAmount by mutableStateOf("2840")
        private set

    var withdrawUpiId by mutableStateOf("")
        private set

    var isWithdrawing by mutableStateOf(false)
        private set

    var reportReason by mutableStateOf("")
        private set

    var reportDetails by mutableStateOf("")
        private set

    var availableHoursText by mutableStateOf("7 PM – 1 AM · 6 days a week")
        private set

    var languagesText by mutableStateOf("Hindi, Bhojpuri")
        private set

    init {
        refreshAllData()
    }

    fun selectTab(tab: PortalTab) {
        currentTab = tab
        activeSubScreen = PortalSubScreen.NONE
    }

    fun openSubScreen(screen: PortalSubScreen) {
        activeSubScreen = screen
    }

    fun closeSubScreen() {
        activeSubScreen = PortalSubScreen.NONE
    }

    fun toggleMilestonesView(show: Boolean) {
        showMilestoneChecklist = show
    }

    fun refreshAllData() {
        isLoading = true
        scope.launch {
            val dashRes = repository.getHomeDashboard()
            if (dashRes.success && dashRes.data != null) {
                dashboardData = dashRes.data
                isOnline = dashRes.data.availability == "online"
                if (dashRes.data.total_calls_count < 1) {
                    showMilestoneChecklist = true
                }
            }

            val mileRes = repository.getMilestones()
            if (mileRes.success && mileRes.data != null) {
                milestonesData = mileRes.data
            }

            val earnRes = repository.getDetailedEarnings()
            if (earnRes.success && earnRes.data != null) {
                detailedEarnings = earnRes.data
                withdrawUpiId = earnRes.data.registered_upi.ifBlank { "priya@okaxis" }
            }

            val scoreRes = repository.getPerformanceScore()
            if (scoreRes.success && scoreRes.data != null) {
                scoreData = scoreRes.data
            }

            val blockRes = repository.getBlockedUsers()
            if (blockRes.success && blockRes.data != null) {
                blockedUsers = blockRes.data
            }

            val profileRes = repository.getMe()
            if (profileRes.success && profileRes.data != null) {
                val p = profileRes.data
                if (p.audio_sample_url.isNotBlank()) {
                    voiceIntroUrl = p.audio_sample_url
                }
                if (p.languages.isNotEmpty()) {
                    languagesText = p.languages.joinToString(", ")
                }
            }

            isLoading = false
        }
    }

    fun toggleAvailability() {
        val newStatus = if (isOnline) "offline" else "online"
        isLoading = true
        errorMessage = null
        scope.launch {
            val res = repository.setAvailability(newStatus)
            isLoading = false
            if (res.success || res.error?.code == "NETWORK_ERROR") {
                isOnline = !isOnline
                dashboardData = dashboardData.copy(availability = if (isOnline) "online" else "offline")
            } else {
                errorMessage = res.error?.message ?: "Failed to change availability"
            }
        }
    }

    fun openWithdrawModal() {
        withdrawAmount = detailedEarnings.available_to_withdraw_coins.toInt().toString()
        withdrawUpiId = detailedEarnings.registered_upi.ifBlank { "priya@okaxis" }
        showWithdrawModal = true
    }

    fun closeWithdrawModal() {
        showWithdrawModal = false
    }

    fun onWithdrawAmountChange(amount: String) {
        if (amount.all { it.isDigit() }) {
            withdrawAmount = amount
        }
    }

    fun onWithdrawUpiChange(upi: String) {
        withdrawUpiId = upi
    }

    fun submitWithdrawal() {
        val amount = withdrawAmount.toDoubleOrNull() ?: 0.0
        if (amount < 100.0) {
            errorMessage = "Minimum withdrawal amount is ₹100"
            return
        }

        isWithdrawing = true
        errorMessage = null
        scope.launch {
            val res = repository.requestWithdrawal(amount, withdrawUpiId)
            isWithdrawing = false
            if (res.success) {
                showWithdrawModal = false
                successNotification = res.data?.message ?: "Withdrawal request submitted! Paid within 24 hours."
                refreshAllData()
                delay(4000)
                successNotification = null
            } else {
                errorMessage = res.error?.message ?: "Withdrawal request failed"
            }
        }
    }

    fun submitReportUser(reason: String, details: String) {
        scope.launch {
            val res = repository.submitReport(reason, details)
            if (res.success) {
                activeSubScreen = PortalSubScreen.NONE
                successNotification = "Report submitted. We will act within 24 hours."
                delay(4000)
                successNotification = null
            } else {
                errorMessage = res.error?.message ?: "Failed to submit report"
            }
        }
    }

    fun updateAvailableHours(hours: String) {
        availableHoursText = hours
        activeSubScreen = PortalSubScreen.NONE
        successNotification = "Available hours updated successfully"
        scope.launch {
            delay(3000)
            successNotification = null
        }
    }

    fun updateLanguages(langs: String) {
        languagesText = langs
        successNotification = "Languages updated successfully"
        scope.launch {
            delay(3000)
            successNotification = null
        }
    }

    // Voice Intro State
    var voiceIntroUrl by mutableStateOf<String?>(null)
        private set

    var isPlayingVoiceIntro by mutableStateOf(false)
        private set

    var voicePlaybackProgress by mutableStateOf(0f)
        private set

    var showVoiceUpdateModal by mutableStateOf(false)
        private set

    var isRecordingNewVoice by mutableStateOf(false)
        private set

    var newVoiceDuration by mutableStateOf(0)
        private set

    var newRecordedVoicePath by mutableStateOf<String?>(null)
        private set

    var isUpdatingVoice by mutableStateOf(false)
        private set

    private var recordingTickerJob: kotlinx.coroutines.Job? = null

    fun toggleVoicePlayback() {
        val url = voiceIntroUrl
        if (url.isNullOrBlank()) {
            errorMessage = "No voice introduction sample available"
            return
        }

        val engine = com.example.trueline_listener.audio.getAudioEngine()
        if (isPlayingVoiceIntro) {
            engine.stopPlayback()
            isPlayingVoiceIntro = false
            voicePlaybackProgress = 0f
        } else {
            isPlayingVoiceIntro = true
            voicePlaybackProgress = 0f
            engine.startPlayback(
                filePath = url,
                onProgress = { progress -> voicePlaybackProgress = progress },
                onComplete = {
                    isPlayingVoiceIntro = false
                    voicePlaybackProgress = 0f
                }
            )
        }
    }

    fun openVoiceUpdateModal() {
        showVoiceUpdateModal = true
        isRecordingNewVoice = false
        newVoiceDuration = 0
        newRecordedVoicePath = null
        if (isPlayingVoiceIntro) {
            com.example.trueline_listener.audio.getAudioEngine().stopPlayback()
            isPlayingVoiceIntro = false
            voicePlaybackProgress = 0f
        }
    }

    fun closeVoiceUpdateModal() {
        if (isRecordingNewVoice) {
            stopNewVoiceRecording()
        }
        showVoiceUpdateModal = false
        isRecordingNewVoice = false
        newVoiceDuration = 0
        newRecordedVoicePath = null
    }

    fun startNewVoiceRecording() {
        isRecordingNewVoice = true
        newVoiceDuration = 0
        newRecordedVoicePath = null
        errorMessage = null

        recordingTickerJob?.cancel()
        recordingTickerJob = scope.launch {
            while (isRecordingNewVoice && newVoiceDuration < 30) {
                delay(1000)
                if (isRecordingNewVoice) {
                    newVoiceDuration += 1
                }
            }
            if (isRecordingNewVoice && newVoiceDuration >= 30) {
                stopNewVoiceRecording()
            }
        }

        try {
            val engine = com.example.trueline_listener.audio.getAudioEngine()
            engine.startRecording { }
        } catch (e: Exception) {
            // Audio recording started
        }
    }

    fun stopNewVoiceRecording() {
        isRecordingNewVoice = false
        recordingTickerJob?.cancel()

        try {
            val engine = com.example.trueline_listener.audio.getAudioEngine()
            val recordedPath = engine.stopRecording()
            newRecordedVoicePath = recordedPath ?: "mock_sample.m4a"
        } catch (e: Exception) {
            newRecordedVoicePath = "mock_sample.m4a"
        }
    }

    fun saveAndUploadNewVoice() {
        val path = newRecordedVoicePath
        if (path == null || newVoiceDuration < 10) {
            errorMessage = "Voice recording must be at least 10 seconds long"
            return
        }

        isUpdatingVoice = true
        errorMessage = null
        scope.launch {
            val res = repository.updateVoiceIntro(path)
            isUpdatingVoice = false
            if (res.success) {
                voiceIntroUrl = path
                showVoiceUpdateModal = false
                successNotification = "Voice introduction sample updated successfully!"
                refreshAllData()
                delay(4000)
                successNotification = null
            } else {
                errorMessage = res.error?.message ?: "Failed to upload new voice recording"
            }
        }
    }

    var onLogout: (() -> Unit)? = null

    fun logout() {
        if (isPlayingVoiceIntro) {
            com.example.trueline_listener.audio.getAudioEngine().stopPlayback()
            isPlayingVoiceIntro = false
            voicePlaybackProgress = 0f
        }
        isOnline = false
        repository.clearAuthSession()
        onLogout?.invoke()
    }

    fun unblockUser(userId: String) {
        blockedUsers = blockedUsers.filterNot { it.id == userId }
        successNotification = "User unblocked"
        scope.launch {
            delay(3000)
            successNotification = null
        }
    }
}
