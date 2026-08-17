package com.example.trueline_listener.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.trueline_listener.network.EarningsSummaryResponse
import com.example.trueline_listener.network.ListenerProfile
import com.example.trueline_listener.network.ListenerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreenState {
    HOME,
    INCOMING_CALL,
    ACTIVE_CALL
}

class HomeViewModel(
    private val scope: CoroutineScope,
    private val repository: ListenerRepository
) {
    var profile by mutableStateOf<ListenerProfile?>(null)
        private set

    var isOnline by mutableStateOf(false)
        private set

    var earnings by mutableStateOf(EarningsSummaryResponse())
        private set

    var screenState by mutableStateOf(AppScreenState.HOME)
        private set

    var activeSessionId by mutableStateOf<String?>(null)
        private set

    var activeRoomId by mutableStateOf<String?>(null)
        private set

    var listenerToken by mutableStateOf<String?>(null)
        private set

    var callDurationSeconds by mutableStateOf(0)
        private set

    var isMuted by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Payout modal state
    var showPayoutModal by mutableStateOf(false)
        private set
    var payoutAmountINR by mutableStateOf("")
        private set
    var payoutUPI by mutableStateOf("")
        private set

    private var callTimerJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        isLoading = true
        scope.launch {
            val profRes = repository.getMe()
            if (profRes.success && profRes.data != null) {
                profile = profRes.data
                isOnline = profRes.data.availability == "online"
            }

            val earnRes = repository.getEarnings()
            if (earnRes.success && earnRes.data != null) {
                earnings = earnRes.data
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
            } else {
                errorMessage = res.error?.message ?: "Failed to change availability"
            }
        }
    }

    fun openPayoutModal() {
        payoutAmountINR = ""
        payoutUPI = ""
        showPayoutModal = true
    }

    fun closePayoutModal() {
        showPayoutModal = false
    }

    fun onPayoutAmountChange(amount: String) {
        if (amount.all { it.isDigit() }) payoutAmountINR = amount
    }

    fun onPayoutUPIChange(upi: String) {
        payoutUPI = upi
    }

    fun submitPayout() {
        val inr = payoutAmountINR.toLongOrNull() ?: 0
        if (inr <= 0 || payoutUPI.isBlank()) {
            errorMessage = "Please enter valid amount and UPI ID"
            return
        }

        isLoading = true
        errorMessage = null
        scope.launch {
            val res = repository.requestWithdrawal(inr.toDouble(), payoutUPI)
            isLoading = false
            if (res.success || res.error?.code == "NETWORK_ERROR") {
                showPayoutModal = false
                loadData()
            } else {
                errorMessage = res.error?.message ?: "Payout request failed"
            }
        }
    }

    // Call Actions
    fun onIncomingCallReceived(sessionId: String, roomId: String) {
        activeSessionId = sessionId
        activeRoomId = roomId
        screenState = AppScreenState.INCOMING_CALL
    }

    fun acceptCall() {
        val sid = activeSessionId ?: "session-demo-001"
        isLoading = true
        scope.launch {
            val res = repository.acceptCall(sid)
            isLoading = false
            if (res.success && res.data != null) {
                listenerToken = res.data.listener_token
            }
            screenState = AppScreenState.ACTIVE_CALL
            startCallTimer()
        }
    }

    fun declineCall() {
        val sid = activeSessionId ?: "session-demo-001"
        scope.launch {
            repository.endCall(sid, "listener_decline")
            screenState = AppScreenState.HOME
            activeSessionId = null
        }
    }

    fun endActiveCall() {
        val sid = activeSessionId ?: "session-demo-001"
        scope.launch {
            repository.endCall(sid, "listener_hangup")
            stopCallTimer()
            screenState = AppScreenState.HOME
            activeSessionId = null
            loadData() // Refresh earnings
        }
    }

    fun toggleMute() {
        isMuted = !isMuted
    }

    private fun startCallTimer() {
        callDurationSeconds = 0
        callTimerJob?.cancel()
        callTimerJob = scope.launch {
            while (screenState == AppScreenState.ACTIVE_CALL) {
                delay(1000)
                callDurationSeconds += 1
            }
        }
    }

    private fun stopCallTimer() {
        callTimerJob?.cancel()
        callDurationSeconds = 0
    }
}
