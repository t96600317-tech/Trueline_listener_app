package com.example.trueline_listener

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.trueline_listener.call.handleFCMIncomingCall
import com.example.trueline_listener.call.updateFCMToken
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TrueLineFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHAT_CHANNEL_ID = "true_line_chats"
        const val CHAT_CHANNEL_NAME = "TrueLine Chats"
    }

    override fun onNewToken(token: String) {
        updateFCMToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val msgType = data["type"] ?: ""

        if (msgType == "incoming_call") {
            handleFCMIncomingCall(
                context = applicationContext,
                sessionId = data["session_id"].orEmpty(),
                callerName = data["caller_name"].orEmpty()
            )
            return
        }

        // Handle chat notification
        val senderName = message.notification?.title 
            ?: data["sender_name"] 
            ?: "New Message"
        val content = message.notification?.body 
            ?: data["content"] 
            ?: "You have received a new message"
        val partnerId = data["partner_id"] ?: ""

        showChatNotification(
            context = applicationContext,
            title = senderName,
            message = content,
            partnerId = partnerId
        )
    }

    private fun showChatNotification(
        context: Context,
        title: String,
        message: String,
        partnerId: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("partner_id", partnerId)
            putExtra("caller_name", title)
            putExtra("is_chat", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            partnerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHAT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                partnerId.hashCode().let { if (it == 0) (1000..9999).random() else it },
                notification
            )
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHAT_CHANNEL_ID,
                CHAT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming caller messages"
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
