package com.example.trueline_listener.call

enum class PendingIncomingCallAction {
    OPEN,
    ACCEPT,
    DECLINE
}

data class DeferredIncomingCallAction(
    val sessionId: String,
    val action: PendingIncomingCallAction
)

expect object IncomingCallAlert {
    fun start(sessionId: String, callerName: String)
    fun accept(sessionId: String)
    fun stop(sessionId: String? = null)
    fun setActionHandlers(onAccept: () -> Unit, onDecline: () -> Unit)
    fun setPushTokenUpdatedHandler(onTokenUpdated: () -> Unit)
    fun getPushToken(): String?
    fun getPushPlatform(): String?
    fun pendingSessionId(): String?
    fun consumeDeferredAction(): DeferredIncomingCallAction?
}
