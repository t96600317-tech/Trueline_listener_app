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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frameLayout = FrameLayout(this).apply {
            id = android.view.View.generateViewId()
        }
        containerLayoutId = frameLayout.id
        setContentView(frameLayout)

        appId = intent.getLongExtra("APP_ID", 628007464L)
        appSign = intent.getStringExtra("APP_SIGN") ?: "e7dffb8a9cb6a89f1fc2afddcc16f4ce4df9cd1e8ca346076161caf69cbd465e"
        userId = intent.getStringExtra("USER_ID") ?: ("listener_" + System.currentTimeMillis())
        userName = intent.getStringExtra("USER_NAME") ?: "Listener"
        callId = intent.getStringExtra("CALL_ID") ?: ("call_" + System.currentTimeMillis())

        val requiredPermissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

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
        try {
            val config = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall().apply {
                turnOnCameraWhenJoining = false
                turnOnMicrophoneWhenJoining = true
                useSpeakerWhenJoining = true
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
        onCallEndCallback?.invoke()
        onCallEndCallback = null
    }
}
