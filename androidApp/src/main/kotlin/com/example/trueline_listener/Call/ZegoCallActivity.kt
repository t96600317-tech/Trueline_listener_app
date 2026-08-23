package com.example.trueline_listener.call

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trueline_listener.R
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

class ZegoCallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zego_call)

        val appID: Long = 628007464
        val appSign = "e7dffb8a9cb6a89f1fc2afddcc16f4ce4df9cd1e8ca346076161caf69cbd465e"// Paste your AppSign from the ZEGOCLOUD dashboard

        val callID = intent.getStringExtra("callID") ?: "test_call_101"
        val userID = intent.getStringExtra("userID") ?: "user_${System.currentTimeMillis()}"
        val userName = intent.getStringExtra("userName") ?: "Listener User"

        // 1-on-1 Voice Call setup
        val config = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()

        val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            appID,
            appSign,
            userID,
            userName,
            callID,
            config
        )

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitNow()
    }
}