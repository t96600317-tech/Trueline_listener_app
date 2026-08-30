package com.example.trueline_listener.call

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService

private var globalCallService: CallServiceWrapper? = null

fun initCallService(activity: Activity) {
    globalCallService = CallServiceWrapper(activity)
}

actual fun getCallService(): CallServiceWrapper {
    return globalCallService ?: error("CallServiceWrapper not initialized. Call initCallService(activity) in MainActivity.")
}

actual class CallServiceWrapper(private val context: Context) {

    private var currentAppId: Long = 1939552281L
    private var currentUserId: String = ""
    private var currentUserName: String = ""

    actual fun initialize(appId: Long, userId: String, userName: String) {
        currentAppId = appId
        currentUserId = userId.replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "listener_${System.currentTimeMillis()}" }.take(64)
        currentUserName = userName.trim().ifBlank { "Listener" }.take(64)
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
        require(token.isNotBlank()) { "A Zego token is required to start a voice call" }
        ZegoCallActivity.onCallEndCallback = onCallEnd
        ZegoCallActivity.onCallStartFailedCallback = onCallStartFailed

        val safeTargetName = targetUserName.trim().ifBlank { "User" }.take(64)

        if (currentUserId.isBlank()) {
            initialize(currentAppId, "listener_" + System.currentTimeMillis(), "Listener")
        }

        if (signedUserId.isNotBlank()) {
            currentUserId = signedUserId.replace("-", "_")
                .filter { it.isLetterOrDigit() || it == '_' }
                .take(64)
        }

        launchDirectCall(roomId, safeTargetName, token)
    }

    private fun launchDirectCall(roomId: String, safeTargetName: String, token: String) {
        val intent = Intent(context, ZegoCallActivity::class.java).apply {
            putExtra("APP_ID", currentAppId)
            putExtra("USER_ID", currentUserId.ifBlank { "listener_" + System.currentTimeMillis() })
            putExtra("USER_NAME", currentUserName.ifBlank { "Listener" })
            putExtra("CALL_ID", roomId)
            putExtra("ZEGO_TOKEN", token)
            putExtra("TARGET_USER_NAME", safeTargetName)
        }
        context.startActivity(intent)
    }

    actual fun endCall() {
        try {
            ZegoUIKitPrebuiltCallService.endCall()
        } catch (e: Exception) {}
    }
}
