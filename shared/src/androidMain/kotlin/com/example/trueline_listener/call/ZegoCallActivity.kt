package com.example.trueline_listener.call

import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

class ZegoCallActivity : FragmentActivity() {

    companion object {
        var onCallEndCallback: (() -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val containerId = try {
            val resId = resources.getIdentifier("fragment_container", "id", packageName)
            if (resId != 0) {
                val layoutId = resources.getIdentifier("activity_zego_call", "layout", packageName)
                if (layoutId != 0) {
                    setContentView(layoutId)
                    resId
                } else {
                    val frameLayout = FrameLayout(this).apply { id = resId }
                    setContentView(frameLayout)
                    resId
                }
            } else {
                android.R.id.content
            }
        } catch (e: Exception) {
            android.R.id.content
        }

        val appId = intent.getLongExtra("APP_ID", 628007464L)
        val appSign = intent.getStringExtra("APP_SIGN") ?: "e7dffb8a9cb6a89f1fc2afddcc16f4ce4df9cd1e8ca346076161caf69cbd465e"
        val userId = intent.getStringExtra("USER_ID") ?: ("listener_" + System.currentTimeMillis())
        val userName = intent.getStringExtra("USER_NAME") ?: "Listener"
        val callId = intent.getStringExtra("CALL_ID") ?: ("call_" + System.currentTimeMillis())

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
            .commitNow()
    }

    override fun onDestroy() {
        super.onDestroy()
        onCallEndCallback?.invoke()
        onCallEndCallback = null
    }
}
