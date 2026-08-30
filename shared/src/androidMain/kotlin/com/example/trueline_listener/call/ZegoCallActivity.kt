package com.example.trueline_listener.call

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zegocloud.uikit.ZegoUIKit
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import com.zegocloud.uikit.prebuilt.call.config.ZegoCallDurationConfig
import com.zegocloud.uikit.service.defines.RoomStateChangedListener
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason

class ZegoCallActivity : AppCompatActivity() {

    companion object {
        var onCallEndCallback: (() -> Unit)? = null
        var onCallStartFailedCallback: ((message: String) -> Unit)? = null
        private const val PERMISSION_REQ_CODE = 102
    }

    private var appId: Long = 1939552281L
    private var zegoToken: String = ""
    private var userId: String = ""
    private var userName: String = ""
    private var callId: String = ""
    private var containerLayoutId: Int = 0
    private var isFragmentAttached = false
    private var callEndReported = false
    private var callStartFailureReported = false
    private var callConnected = false

    private var targetUserName: String = ""

    private val roomStateListener = RoomStateChangedListener { roomID, reason, errorCode, _ ->
        if (roomID != callId) return@RoomStateChangedListener

        when (reason) {
            ZegoRoomStateChangedReason.LOGINED,
            ZegoRoomStateChangedReason.RECONNECTED -> callConnected = true

            ZegoRoomStateChangedReason.LOGIN_FAILED,
            ZegoRoomStateChangedReason.RECONNECT_FAILED -> {
                reportConnectionFailure("Zego room login failed (reason=$reason, code=$errorCode)")
            }

            else -> Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frameLayout = FrameLayout(this).apply {
            id = android.view.View.generateViewId()
        }
        containerLayoutId = frameLayout.id
        setContentView(frameLayout)

        appId = intent.getLongExtra("APP_ID", 1939552281L)
        zegoToken = intent.getStringExtra("ZEGO_TOKEN").orEmpty()
        
        val rawUserId = intent.getStringExtra("USER_ID") ?: ("listener_" + System.currentTimeMillis())
        userId = rawUserId.replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "listener_${System.currentTimeMillis()}" }.take(64)
        
        userName = (intent.getStringExtra("USER_NAME") ?: "Listener").trim().ifBlank { "Listener" }.take(64)
        targetUserName = (intent.getStringExtra("TARGET_USER_NAME") ?: "User").trim().ifBlank { "User" }.take(64)
        
        val rawCallId = intent.getStringExtra("CALL_ID") ?: ("call_" + System.currentTimeMillis())
        callId = rawCallId.replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "call_${System.currentTimeMillis()}" }.take(64)

        ZegoUIKit.addRoomStateChangedListener(roomStateListener)

        if (zegoToken.isBlank()) {
            reportConnectionFailure("The backend returned an empty Zego token")
            return
        }

        val requiredPermissions = mutableListOf(Manifest.permission.RECORD_AUDIO)

        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSION_REQ_CODE)
        } else {
            startCallFragment(containerLayoutId)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_CODE && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startCallFragment(containerLayoutId)
        } else {
            reportConnectionFailure("Microphone permission was denied")
        }
    }

    private fun startCallFragment(containerId: Int) {
        if (isFragmentAttached || isFinishing || isDestroyed) return
        isFragmentAttached = true

        try {
            val config = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall().apply {
                turnOnCameraWhenJoining = false
                turnOnMicrophoneWhenJoining = true
                useSpeakerWhenJoining = true
                topMenuBarConfig.isVisible = true
                topMenuBarConfig.title = targetUserName
                durationConfig = ZegoCallDurationConfig().apply {
                    isVisible = true
                }
                leaveCallListener = ZegoUIKitPrebuiltCallFragment.LeaveCallListener {
                    finishCall()
                }
            }

            val fragment = ZegoUIKitPrebuiltCallFragment.newInstanceWithToken(
                appId,
                zegoToken,
                userId,
                userName,
                callId,
                config
            )

            supportFragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commitAllowingStateLoss()
        } catch (e: Exception) {
            val detail = buildString {
                append("Unable to create the Zego call screen: ${e.javaClass.simpleName}")
                e.message?.takeIf { it.isNotBlank() }?.let { append(" ($it)") }
            }
            reportConnectionFailure(detail)
        }
    }

    private fun finishCall() {
        if (!callConnected) {
            reportConnectionFailure("The Zego room closed before connecting")
            return
        }
        reportCallEnded()
        finish()
    }

    private fun reportCallEnded() {
        if (callEndReported || callStartFailureReported) return
        callEndReported = true
        try {
            onCallEndCallback?.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onCallEndCallback = null
        onCallStartFailedCallback = null
    }

    private fun reportConnectionFailure(detail: String) {
        if (callEndReported || callStartFailureReported) return
        callStartFailureReported = true

        val isDebugBuild = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val message = if (isDebugBuild) {
            "Voice connection failed: $detail"
        } else {
            "We couldn't connect the voice call. Please try again."
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        try {
            onCallStartFailedCallback?.invoke(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onCallStartFailedCallback = null
        onCallEndCallback = null
        if (!isFinishing) {
            finish()
        }
    }

    override fun onDestroy() {
        ZegoUIKit.removeRoomStateChangedListener(roomStateListener)
        if (isFinishing && !callConnected) {
            reportConnectionFailure("The call activity closed before connection completed")
        } else if (isFinishing) {
            reportCallEnded()
        }
        super.onDestroy()
    }
}
