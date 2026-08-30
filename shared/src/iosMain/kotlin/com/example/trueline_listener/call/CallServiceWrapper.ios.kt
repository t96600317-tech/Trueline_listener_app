@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.example.trueline_listener.call

import com.example.trueline_listener.currentPlatformTimeMillis
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue

private const val START_CALL_NOTIFICATION = "trueline.listener.zego.start"
private const val END_CALL_NOTIFICATION = "trueline.listener.zego.end"
private const val CALL_ENDED_NOTIFICATION = "trueline.listener.zego.ended"
private const val CALL_FAILED_NOTIFICATION = "trueline.listener.zego.failed"

private val notificationCenter = NSNotificationCenter.defaultCenter
private val iosCallService = CallServiceWrapper()

actual fun getCallService(): CallServiceWrapper = iosCallService

actual class CallServiceWrapper {
    private var appId = 1939552281L
    private var userId = ""
    private var userName = ""
    private var onCallEnd: (() -> Unit)? = null
    private var onCallStartFailed: ((String) -> Unit)? = null

    private val callEndedObserver = notificationCenter.addObserverForName(
        CALL_ENDED_NOTIFICATION,
        null,
        NSOperationQueue.mainQueue
    ) {
        onCallEnd?.invoke()
        clearCallbacks()
    }

    private val callFailedObserver = notificationCenter.addObserverForName(
        CALL_FAILED_NOTIFICATION,
        null,
        NSOperationQueue.mainQueue
    ) { notification ->
        val message = notification?.userInfo?.get("message") as? String
            ?: "We couldn't connect the voice call. Please try again."
        onCallStartFailed?.invoke(message)
        clearCallbacks()
    }

    actual fun initialize(appId: Long, userId: String, userName: String) {
        this.appId = appId
        this.userId = sanitizeId(userId).ifBlank { "listener_${currentPlatformTimeMillis()}" }
        this.userName = userName.trim().ifBlank { "Listener" }.take(64)
    }

    actual fun startAudioCall(
        roomId: String,
        targetUserId: String,
        targetUserName: String,
        token: String,
        signedUserId: String,
        onCallEnd: () -> Unit,
        onCallStartFailed: (message: String) -> Unit
    ) {
        if (token.isBlank()) {
            onCallStartFailed("A Zego token is required to start a voice call")
            return
        }

        this.onCallEnd = onCallEnd
        this.onCallStartFailed = onCallStartFailed
        if (userId.isBlank()) {
            initialize(appId, "listener_${currentPlatformTimeMillis()}", userName)
        }
        if (signedUserId.isNotBlank()) {
            userId = sanitizeId(signedUserId).ifBlank { userId }
        }

        notificationCenter.postNotificationName(
            START_CALL_NOTIFICATION,
            null,
            mapOf(
                "appId" to appId,
                "roomId" to sanitizeId(roomId),
                "token" to token,
                "userId" to userId,
                "userName" to userName,
                "targetUserId" to sanitizeId(targetUserId),
                "targetUserName" to targetUserName.trim().ifBlank { "User" }.take(64)
            )
        )
    }

    actual fun endCall() {
        notificationCenter.postNotificationName(END_CALL_NOTIFICATION, null, null)
    }

    private fun clearCallbacks() {
        onCallEnd = null
        onCallStartFailed = null
    }

    private fun sanitizeId(value: String): String =
        value.replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }.take(64)
}
