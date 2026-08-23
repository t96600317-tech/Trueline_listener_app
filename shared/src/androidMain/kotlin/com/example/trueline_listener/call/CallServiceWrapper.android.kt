package com.example.trueline_listener.call

import android.content.Context
import android.content.Intent
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService

private var globalCallService: CallServiceWrapper? = null

fun initCallService(context: Context) {
    globalCallService = CallServiceWrapper(context.applicationContext)
}

actual fun getCallService(): CallServiceWrapper {
    return globalCallService ?: error("CallServiceWrapper not initialized. Call initCallService(context) in MainActivity.")
}

actual class CallServiceWrapper(private val context: Context) {

    private var currentAppId: Long = 628007464L
    private var currentAppSign: String = "e7dffb8a9cb6a89f1fc2afddcc16f4ce4df9cd1e8ca346076161caf69cbd465e"
    private var currentUserId: String = ""
    private var currentUserName: String = ""

    actual fun initialize(appId: Long, appSign: String, userId: String, userName: String) {
        currentAppId = appId
        currentAppSign = appSign
        currentUserId = userId
        currentUserName = userName
    }

    actual fun startAudioCall(
        roomId: String,
        targetUserId: String,
        targetUserName: String,
        token: String,
        onCallEnd: () -> Unit
    ) {
        ZegoCallActivity.onCallEndCallback = onCallEnd
        val intent = Intent(context, ZegoCallActivity::class.java).apply {
            putExtra("APP_ID", currentAppId)
            putExtra("APP_SIGN", currentAppSign)
            putExtra("USER_ID", currentUserId.ifBlank { "listener_" + System.currentTimeMillis() })
            putExtra("USER_NAME", currentUserName.ifBlank { "Listener" })
            putExtra("CALL_ID", roomId)
            putExtra("TARGET_USER_NAME", targetUserName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    actual fun endCall() {
        try {
            ZegoUIKitPrebuiltCallService.endCall()
        } catch (e: Exception) {}
    }
}
