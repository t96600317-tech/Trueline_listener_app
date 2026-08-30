package com.example.trueline_listener

import com.example.trueline_listener.call.handleFCMIncomingCall
import com.example.trueline_listener.call.updateFCMToken
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TrueLineFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        updateFCMToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data["type"] != "incoming_call") return
        handleFCMIncomingCall(
            context = applicationContext,
            sessionId = data["session_id"].orEmpty(),
            callerName = data["caller_name"].orEmpty()
        )
    }
}
