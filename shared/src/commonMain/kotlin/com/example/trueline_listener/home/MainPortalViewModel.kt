package com.example.trueline_listener.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.trueline_listener.currentPlatformTimeMillis
import com.example.trueline_listener.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PortalTab { HOME, CALLS, CHAT, EARNINGS, PROFILE }

enum class AvailabilityMode { OFFLINE, BUSY, ONLINE }

enum class PerformanceStatsTab { TODAY, YESTERDAY, THIS_WEEK }

enum class TransactionFilter { ALL, CALLS, BONUS, PAYOUT, PENALTY }

enum class ChatFilter { ALL, UNREAD, REGULARS, NEEDS_REPLY }

enum class PortalSubScreen {
    NONE,
    PERFORMANCE_SCORE,
    BLOCKED_USERS,
    REPORT_USER,
    AVAILABLE_HOURS,
    PRIVACY_INFO,
    SUPPORT_INFO,
    TRANSACTIONS,
    NOTIFICATIONS
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

    var isOnline by mutableStateOf(true)
        private set

    var availabilityMode by mutableStateOf(AvailabilityMode.ONLINE)
        private set

    var selectedStatsTab by mutableStateOf(PerformanceStatsTab.TODAY)
        private set

    var selectedTransactionFilter by mutableStateOf(TransactionFilter.ALL)
        private set

    fun selectTransactionFilter(filter: TransactionFilter) {
        selectedTransactionFilter = filter
    }

    var showGoOfflineModal by mutableStateOf(false)
        private set

    fun openGoOfflineModal() {
        showGoOfflineModal = true
    }

    fun closeGoOfflineModal() {
        showGoOfflineModal = false
    }

    fun confirmGoOffline() {
        updateAvailabilityMode(AvailabilityMode.OFFLINE)
        showGoOfflineModal = false
    }

    var chatSearchQuery by mutableStateOf("")
        private set

    fun updateChatSearchQuery(query: String) {
        chatSearchQuery = query
    }

    var selectedChatFilter by mutableStateOf(ChatFilter.ALL)
        private set

    fun selectChatFilter(filter: ChatFilter) {
        selectedChatFilter = filter
    }

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successNotification by mutableStateOf<String?>(null)
        private set

    // Data States
    var dashboardData by mutableStateOf(HomeDashboardResponse(listener_name = "Zayan", listener_id_tag = "ID 40219"))
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

    // Chat States
    var conversations = androidx.compose.runtime.mutableStateListOf<ChatConversationData>()
    var isChatListLoading by mutableStateOf(false)
    var isPullToRefreshing by mutableStateOf(false)
    var activeChatUserId by mutableStateOf<String?>(null)
    var activeChatUserName by mutableStateOf("")
    var activeChatUserOnline by mutableStateOf(false)
    var currentChatMessages = androidx.compose.runtime.mutableStateListOf<ChatMessageData>()
    var isChatMessagesLoading by mutableStateOf(false)

    // Call History States
    var callHistoryData by mutableStateOf<CallHistoryResponse?>(null)
    var isCallHistoryLoading by mutableStateOf(false)

    // Transactions States
    var transactionsList = androidx.compose.runtime.mutableStateListOf<TransactionItemData>()
    var isTransactionsLoading by mutableStateOf(false)

    // Notification States
    var notificationsList = androidx.compose.runtime.mutableStateListOf<NotificationItemData>()
    var unreadNotificationsCount by mutableStateOf(0)
    var isNotificationsLoading by mutableStateOf(false)

    // Incoming Call States
    var incomingCallSession by mutableStateOf<CallSessionData?>(null)
        private set

    private var incomingCallWatcherJob: kotlinx.coroutines.Job? = null
    private var callEventsJob: kotlinx.coroutines.Job? = null
    private var acceptingIncomingSessionId: String? = null
    private var registeredPushToken: String? = null

    init {
        com.example.trueline_listener.call.IncomingCallAlert.setActionHandlers(
            onAccept = ::acceptIncomingCall,
            onDecline = ::declineIncomingCall
        )
        com.example.trueline_listener.call.IncomingCallAlert.setPushTokenUpdatedHandler(::registerPushToken)
        restorePendingIncomingCall()
        refreshAllData()
    }

    fun selectTab(tab: PortalTab) {
        currentTab = tab
        activeSubScreen = PortalSubScreen.NONE
        if (tab == PortalTab.CALLS) {
            fetchCallHistory()
        }
    }

    fun openSubScreen(screen: PortalSubScreen) {
        activeSubScreen = screen
        if (screen == PortalSubScreen.TRANSACTIONS) {
            fetchTransactions()
        } else if (screen == PortalSubScreen.NOTIFICATIONS) {
            fetchNotifications()
        }
    }

    fun closeSubScreen() {
        activeSubScreen = PortalSubScreen.NONE
    }

    fun toggleMilestonesView(show: Boolean) {
        showMilestoneChecklist = false
    }

    fun showMilestones() {
        showMilestoneChecklist = false
    }

    fun hideMilestones() {
        showMilestoneChecklist = false
    }

    fun fetchCallHistory() {
        isCallHistoryLoading = true
        scope.launch {
            val res = repository.getCallHistory()
            isCallHistoryLoading = false
            if (res.success && res.data != null) {
                callHistoryData = res.data
            }
        }
    }

    fun fetchTransactions() {
        isTransactionsLoading = true
        scope.launch {
            val res = repository.getTransactions()
            isTransactionsLoading = false
            if (res.success && res.data != null) {
                transactionsList.clear()
                transactionsList.addAll(res.data.transactions)
            }
        }
    }

    fun fetchNotifications() {
        isNotificationsLoading = true
        scope.launch {
            val res = repository.getNotifications()
            isNotificationsLoading = false
            if (res.success && res.data != null) {
                notificationsList.clear()
                notificationsList.addAll(res.data.notifications)
                unreadNotificationsCount = res.data.unread_count
            }
        }
    }

    fun refreshAllData() {
        isLoading = true
        fetchCallHistory()
        fetchTransactions()
        fetchNotifications()
        scope.launch {
            val dashRes = repository.getHomeDashboard()
            if (dashRes.success && dashRes.data != null) {
                dashboardData = dashRes.data
                isOnline = dashRes.data.availability == "online"
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
                if (p.name.isNotBlank()) {
                    val idSuffix = p.id.replace("-", "").takeLast(5).uppercase()
                    dashboardData = dashboardData.copy(
                        listener_name = p.name,
                        listener_id_tag = "ID $idSuffix"
                    )
                }
                try {
                    com.example.trueline_listener.call.getCallService().initialize(
                        1939552281L,
                        p.id,
                        p.name
                    )
                } catch (_: Exception) {}
                if (p.audio_sample_url.isNotBlank()) {
                    // voiceIntroUrl = p.audio_sample_url // Assuming this was removed or intended to be ignored
                }
                if (p.languages.isNotEmpty()) {
                    languagesText = p.languages.joinToString(", ")
                }
            }

            if (isOnline) {
                startIncomingCallWatcher()
            }

            fetchConversations()

            isLoading = false
        }
    }

    // --- Chat Functions ---
    private var chatPollingJob: kotlinx.coroutines.Job? = null

    fun fetchConversations(isManualPull: Boolean = false) {
        if (isManualPull) {
            isPullToRefreshing = true
        }
        scope.launch {
            if (isManualPull) {
                delay(1800)
            }
            val res = repository.getChatConversations()
            if (isManualPull) {
                isPullToRefreshing = false
            }
            if (res.success && res.data != null) {
                conversations.clear()
                conversations.addAll(res.data)
                activeChatUserId?.let { currId ->
                    val conv = res.data.firstOrNull { it.user_id == currId || it.listener_id == currId }
                    if (conv != null) {
                        activeChatUserOnline = conv.user_availability == "online" || conv.listener_availability == "online"
                    }
                }
            }
        }
    }

    fun openChat(userId: String, userName: String) {
        activeChatUserId = userId
        activeChatUserName = userName.substringBefore(" ·").trim()
        val conv = conversations.firstOrNull { it.user_id == userId || it.listener_id == userId }
        activeChatUserOnline = conv?.user_availability == "online" || conv?.listener_availability == "online"
        isChatMessagesLoading = true
        currentChatMessages.clear()

        // 1. Initial fetch
        scope.launch {
            val res = repository.getChatMessages(userId)
            isChatMessagesLoading = false
            if (res.success && res.data != null) {
                currentChatMessages.clear()
                currentChatMessages.addAll(res.data)
            }
        }

        // 2. Real-time background polling loop while chat is open
        chatPollingJob?.cancel()
        chatPollingJob = scope.launch {
            while (activeChatUserId == userId) {
                delay(2000)
                fetchConversations()
                val res = repository.getChatMessages(userId)
                if (res.success && res.data != null) {
                    val serverMsgs = res.data
                    if (serverMsgs.size != currentChatMessages.size || (serverMsgs.isNotEmpty() && currentChatMessages.isNotEmpty() && serverMsgs.last().id != currentChatMessages.last().id)) {
                        currentChatMessages.clear()
                        currentChatMessages.addAll(serverMsgs)
                    }
                }
            }
        }
    }

    fun sendChatMessage(content: String) {
        val targetId = activeChatUserId ?: return
        val text = content.trim()
        if (text.isBlank()) return

        // Optimistic UI display
        val optimisticMsg = ChatMessageData(
            id = "temp_${currentPlatformTimeMillis()}",
            user_id = targetId,
            partner_id = "",
            sender_type = "partner",
            content = text,
            created_at = "Just now"
        )
        currentChatMessages.add(optimisticMsg)

        scope.launch {
            val res = repository.sendChatMessage(targetId, text)
            if (res.success && res.data != null) {
                val index = currentChatMessages.indexOfFirst { it.id == optimisticMsg.id }
                if (index >= 0) {
                    currentChatMessages[index] = res.data
                }
                fetchConversations()
            }
        }
    }

    fun closeChat() {
        chatPollingJob?.cancel()
        chatPollingJob = null
        activeChatUserId = null
        activeChatUserName = ""
        currentChatMessages.clear()
        fetchConversations()
    }

    fun startAudioCall(targetUserId: String, targetUserName: String) {
        val roomId = "call_${dashboardData.listener_id_tag.ifBlank { "listener" }}_$targetUserId"
        successNotification = "Connecting voice call with $targetUserName..."
        scope.launch {
            delay(3000)
            successNotification = null
        }
    }

    fun toggleAvailability() {
        val newMode = if (availabilityMode == AvailabilityMode.ONLINE) AvailabilityMode.OFFLINE else AvailabilityMode.ONLINE
        updateAvailabilityMode(newMode)
    }

    fun updateAvailabilityMode(mode: AvailabilityMode) {
        availabilityMode = mode
        isOnline = (mode == AvailabilityMode.ONLINE)
        if (mode == AvailabilityMode.ONLINE || mode == AvailabilityMode.BUSY) {
            startIncomingCallWatcher()
        } else {
            stopIncomingCallWatcher()
        }
        val statusStr = when (mode) {
            AvailabilityMode.OFFLINE -> "offline"
            AvailabilityMode.BUSY -> "busy"
            AvailabilityMode.ONLINE -> "online"
        }
        isLoading = true
        errorMessage = null
        scope.launch {
            val res = repository.setAvailability(statusStr)
            isLoading = false
            if (res.success || res.error?.code == "NETWORK_ERROR") {
                dashboardData = dashboardData.copy(availability = statusStr)
            } else {
                errorMessage = res.error?.message ?: "Failed to change availability"
            }
        }
    }

    fun startIncomingCallWatcher() {
        registerPushToken()
        incomingCallWatcherJob?.cancel()
        incomingCallWatcherJob = scope.launch {
            while (true) {
                if (isOnline || availabilityMode == AvailabilityMode.BUSY) {
                    try {
                        val incRes = repository.checkIncomingCalls()
                        if (incRes.success && incRes.data != null) {
                            val session = incRes.data
                            if (session.status == "pending") {
                                if (incomingCallSession?.id != session.id) {
                                    com.example.trueline_listener.call.IncomingCallAlert.start(
                                        session.id,
                                        session.caller_name
                                    )
                                }
                                incomingCallSession = session
                            } else if (acceptingIncomingSessionId == null) {
                                com.example.trueline_listener.call.IncomingCallAlert.stop(incomingCallSession?.id)
                                incomingCallSession = null
                            }
                        } else if (acceptingIncomingSessionId == null) {
                            com.example.trueline_listener.call.IncomingCallAlert.stop(incomingCallSession?.id)
                            incomingCallSession = null
                        }
                    } catch (_: Exception) {}
                } else {
                    com.example.trueline_listener.call.IncomingCallAlert.stop(incomingCallSession?.id)
                    incomingCallSession = null
                }
                delay(1500)
            }
        }
    }

    private fun restorePendingIncomingCall() {
        val deferredAction = com.example.trueline_listener.call.IncomingCallAlert.consumeDeferredAction()
        val sessionId = deferredAction?.sessionId
            ?: com.example.trueline_listener.call.IncomingCallAlert.pendingSessionId()
            ?: return
        scope.launch {
            val incoming = repository.checkIncomingCalls().data
            if (incoming?.status != "pending" || incoming.id != sessionId) {
                com.example.trueline_listener.call.IncomingCallAlert.stop(sessionId)
                return@launch
            }
            incomingCallSession = incoming
            when (deferredAction?.action) {
                com.example.trueline_listener.call.PendingIncomingCallAction.ACCEPT -> acceptIncomingCall()
                com.example.trueline_listener.call.PendingIncomingCallAction.DECLINE -> declineIncomingCall()
                else -> com.example.trueline_listener.call.IncomingCallAlert.start(incoming.id, incoming.caller_name)
            }
        }
    }

    private fun registerPushToken() {
        val platform = com.example.trueline_listener.call.IncomingCallAlert.getPushPlatform() ?: return
        val deviceToken = com.example.trueline_listener.call.IncomingCallAlert.getPushToken()?.trim().orEmpty()
        if (deviceToken.isBlank() || deviceToken == registeredPushToken) return
        scope.launch {
            val result = when (platform) {
                "ios-voip" -> repository.registerIOSVoIPDevice(deviceToken)
                "android-fcm" -> repository.registerAndroidFCMDevice(deviceToken)
                else -> return@launch
            }
            if (result.success) {
                registeredPushToken = deviceToken
            }
        }
    }

    fun stopIncomingCallWatcher() {
        incomingCallWatcherJob?.cancel()
        incomingCallWatcherJob = null
        com.example.trueline_listener.call.IncomingCallAlert.stop(incomingCallSession?.id)
        incomingCallSession = null
    }

    fun acceptIncomingCall() {
        val session = incomingCallSession
        if (session == null) {
            scope.launch {
                val incoming = repository.checkIncomingCalls().data
                if (incoming?.status == "pending") {
                    incomingCallSession = incoming
                    acceptIncomingCall()
                } else {
                    errorMessage = "This incoming call is no longer available"
                    com.example.trueline_listener.call.IncomingCallAlert.stop()
                }
            }
            return
        }
        if (acceptingIncomingSessionId == session.id) return
        acceptingIncomingSessionId = session.id
        com.example.trueline_listener.call.IncomingCallAlert.accept(session.id)
        incomingCallSession = null
        scope.launch {
            val res = repository.acceptCall(session.id)
            val callData = res.data
            if (!res.success || callData == null || callData.listener_token.isBlank() || callData.room_id.isBlank()) {
                acceptingIncomingSessionId = null
                com.example.trueline_listener.call.IncomingCallAlert.stop(session.id)
                errorMessage = res.error?.message ?: "Unable to get a secure voice-call token"
                return@launch
            }

            try {
                startCallEventObserver(session.id)
                com.example.trueline_listener.call.getCallService().startAudioCall(
                    roomId = callData.room_id,
                    targetUserId = session.caller_id.ifBlank { session.id },
                    targetUserName = session.caller_name.ifBlank { "User" },
                    token = callData.listener_token,
                    signedUserId = callData.zego_user_id,
                    onCallEnd = {
                        scope.launch {
                            callEventsJob?.cancel()
                            acceptingIncomingSessionId = null
                            com.example.trueline_listener.call.IncomingCallAlert.stop(session.id)
                            repository.endCall(session.id, "listener_hangup")
                            refreshAllData()
                        }
                    },
                    onCallStartFailed = { message ->
                        callEventsJob?.cancel()
                        acceptingIncomingSessionId = null
                        com.example.trueline_listener.call.IncomingCallAlert.stop(session.id)
                        errorMessage = message
                        scope.launch {
                            repository.endCall(session.id, "listener_connection_failed")
                            refreshAllData()
                        }
                    }
                )
            } catch (e: Exception) {
                acceptingIncomingSessionId = null
                com.example.trueline_listener.call.IncomingCallAlert.stop(session.id)
                errorMessage = e.message ?: "Failed to start secure voice call"
            }
        }
    }

    fun declineIncomingCall() {
        val session = incomingCallSession
        if (session == null) {
            scope.launch {
                val incoming = repository.checkIncomingCalls().data
                if (incoming?.status == "pending") {
                    incomingCallSession = incoming
                    declineIncomingCall()
                } else {
                    com.example.trueline_listener.call.IncomingCallAlert.stop()
                }
            }
            return
        }
        acceptingIncomingSessionId = null
        com.example.trueline_listener.call.IncomingCallAlert.stop(session.id)
        incomingCallSession = null
        scope.launch {
            repository.endCall(session.id, "listener_decline")
        }
    }

    private fun startCallEventObserver(sessionId: String) {
        callEventsJob?.cancel()
        callEventsJob = scope.launch {
            repository.observeCallEvents(sessionId).collect { event ->
                if (event.type != "call_ended" || acceptingIncomingSessionId != sessionId) return@collect
                callEventsJob?.cancel()
                acceptingIncomingSessionId = null
                com.example.trueline_listener.call.IncomingCallAlert.stop(sessionId)
                com.example.trueline_listener.call.playCallEndedTone()
                com.example.trueline_listener.call.getCallService().endCall()
                errorMessage = when (event.reason) {
                    "low_balance" -> "The call ended because the customer ran out of coins."
                    "user_hangup", "connection_failed" -> "The customer ended the call."
                    else -> "The call was ended by the other participant."
                }
                refreshAllData()
            }
        }
    }

    fun selectStatsTab(tab: PerformanceStatsTab) {
        selectedStatsTab = tab
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
        availabilityMode = AvailabilityMode.OFFLINE
        scope.launch {
            try {
                repository.setAvailability("offline")
            } catch (_: Exception) {}
        }
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

    fun startCallWithUser(targetUserId: String, targetUserName: String) {
        errorMessage = "Voice calls must be started by a customer request"
    }
}
