package com.example.trueline_listener.call

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

class ZegoCallActivity : AppCompatActivity() {

    companion object {
        var onCallEndCallback: (() -> Unit)? = null
        private const val PERMISSION_REQ_CODE = 102
    }

    private var appId: Long = 628007464L
    private var appSign: String = "e7dffb8a9cb6a89f1fc2afddcc16f4ce4df9cd1e8ca346076161caf69cbd465e"
    private var userId: String = ""
    private var userName: String = ""
    private var callId: String = ""
    private var containerLayoutId: Int = 0
    private var isFragmentAttached = false

    private var targetUserName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frameLayout = FrameLayout(this).apply {
            id = android.view.View.generateViewId()
        }
        containerLayoutId = frameLayout.id
        setContentView(frameLayout)

        appId = intent.getLongExtra("APP_ID", 628007464L)
        appSign = intent.getStringExtra("APP_SIGN") ?: "e7dffb8a9cb6a89f1fc2afddcc16f4ce4df9cd1e8ca346076161caf69cbd465e"
        
        val rawUserId = intent.getStringExtra("USER_ID") ?: ("listener_" + System.currentTimeMillis())
        userId = rawUserId.replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "listener_${System.currentTimeMillis()}" }.take(64)
        
        userName = (intent.getStringExtra("USER_NAME") ?: "Listener").trim().ifBlank { "Listener" }.take(64)
        targetUserName = (intent.getStringExtra("TARGET_USER_NAME") ?: "User").trim().ifBlank { "User" }.take(64)
        
        val rawCallId = intent.getStringExtra("CALL_ID") ?: ("call_" + System.currentTimeMillis())
        callId = rawCallId.replace("-", "_").filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "call_${System.currentTimeMillis()}" }.take(64)

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
        startCallFragment(containerLayoutId)
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
                durationConfig.isVisible = true
            }

            val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
                appId,
                appSign,
                userId,
                userName,
                callId,
                config
            )

            supportFragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            onCallEndCallback?.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onCallEndCallback = null
    }
}
