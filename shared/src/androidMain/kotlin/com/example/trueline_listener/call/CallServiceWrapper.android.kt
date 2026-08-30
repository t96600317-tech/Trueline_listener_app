package com.example.trueline_listener.call

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.zegocloud.uikit.plugin.common.PluginCallbackListener
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import com.zegocloud.uikit.service.defines.ZegoUIKitUser

private var globalCallService: CallServiceWrapper? = null
private var currentActivityRef: Activity? = null

fun initCallService(activity: Activity) {
    currentActivityRef = activity
    globalCallService = CallServiceWrapper(activity)
}

actual fun getCallService(): CallServiceWrapper {
    return globalCallService ?: error("CallServiceWrapper not initialized. Call initCallService(activity) in MainActivity.")
}

actual class CallServiceWrapper(private val context: Context) {

    private var currentAppId: Long = 628007464L
    private var currentAppSign: String = "e7dffb8a9cb6a89f1fc2afddcc16f4ce4df9cd1e8ca346076161caf69cbd465e"
    private var currentUserId: String = ""
    private var currentUserName: String = ""
    private var isInvitationServiceInit = false

    actual fun initialize(appId: Long, appSign: String, userId: String, userName: String) {
        currentAppId = appId
        currentAppSign = appSign
        currentUserId = userId.replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "listener_${System.currentTimeMillis()}" }.take(64)
        currentUserName = userName.trim().ifBlank { "Listener" }.take(64)

        try {
            val app = (context as? Activity)?.application 
                ?: (context.applicationContext as? Application)
            if (app != null && !isInvitationServiceInit) {
                val config = ZegoUIKitPrebuiltCallInvitationConfig()
                ZegoUIKitPrebuiltCallInvitationService.init(
                    app,
                    currentAppId,
                    currentAppSign,
                    currentUserId,
                    currentUserName,
                    config
                )
                isInvitationServiceInit = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun startAudioCall(
        roomId: String,
        targetUserId: String,
        targetUserName: String,
        token: String,
        onCallEnd: () -> Unit
    ) {
        ZegoCallActivity.onCallEndCallback = onCallEnd

        val safeTargetName = targetUserName.trim().ifBlank { "User" }.take(64)

        // Ensure invitation service is active
        if (!isInvitationServiceInit) {
            val uid = currentUserId.ifBlank { "listener_" + System.currentTimeMillis() }
            val uname = currentUserName.ifBlank { "Listener" }
            initialize(currentAppId, currentAppSign, uid, uname)
        }

        launchDirectCall(roomId, safeTargetName)
    }

    private fun launchDirectCall(roomId: String, safeTargetName: String) {
        val intent = Intent(context, ZegoCallActivity::class.java).apply {
            putExtra("APP_ID", currentAppId)
            putExtra("APP_SIGN", currentAppSign)
            putExtra("USER_ID", currentUserId.ifBlank { "listener_" + System.currentTimeMillis() })
            putExtra("USER_NAME", currentUserName.ifBlank { "Listener" })
            putExtra("CALL_ID", roomId)
            putExtra("TARGET_USER_NAME", safeTargetName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    actual fun endCall() {
        try {
            ZegoUIKitPrebuiltCallInvitationService.endCall()
            ZegoUIKitPrebuiltCallService.endCall()
        } catch (e: Exception) {}
    }
}
