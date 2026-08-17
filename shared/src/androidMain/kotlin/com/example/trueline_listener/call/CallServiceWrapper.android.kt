package com.example.trueline_listener.call

import android.content.Context
import android.content.Intent
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService

actual class CallServiceWrapper(private val context: Context) {

    private var currentAppId: Long = 123456789L
    private var currentAppSign: String = "zegocloud_secret_32_bytes_long!"
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
        ZegoUIKitPrebuiltCallService.endCall()
    }
}
