package com.example.trueline_listener.call

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging

// Notification-channel sound settings are immutable after creation. This new
// channel owns the ringtone, so the app never plays a second overlapping tone.
private const val INCOMING_CALL_CHANNEL_ID = "incoming_calls_v3"
private const val INCOMING_CALL_NOTIFICATION_ID = 72_001
private const val PUSH_PREFERENCES = "trueline_push"
private const val FCM_TOKEN_KEY = "fcm_token"

private var incomingCallContext: Context? = null
private var activeSessionId: String? = null
private var pushTokenUpdatedHandler: (() -> Unit)? = null

fun initIncomingCallAlert(context: Context) {
    incomingCallContext = context.applicationContext
    ensureIncomingCallChannel(context.applicationContext)
    refreshFCMToken(context.applicationContext)
}

fun updateFCMToken(context: Context, token: String) {
    if (token.isBlank()) return
    context.applicationContext
        .getSharedPreferences(PUSH_PREFERENCES, Context.MODE_PRIVATE)
        .edit()
        .putString(FCM_TOKEN_KEY, token)
        .apply()
    pushTokenUpdatedHandler?.invoke()
}

fun handleFCMIncomingCall(context: Context, sessionId: String, callerName: String) {
    if (sessionId.isBlank()) return
    initIncomingCallAlert(context.applicationContext)
    IncomingCallAlert.start(sessionId, callerName)
}

actual object IncomingCallAlert {
    actual fun start(sessionId: String, callerName: String) {
        val context = incomingCallContext ?: return
        if (activeSessionId == sessionId) return

        stop()
        activeSessionId = sessionId
        showNotification(context, callerName.ifBlank { "Customer" })
    }

    actual fun stop(sessionId: String?) {
        if (sessionId != null && activeSessionId != sessionId) return

        activeSessionId = null
        incomingCallContext?.getSystemService(NotificationManager::class.java)
            ?.cancel(INCOMING_CALL_NOTIFICATION_ID)
    }

    actual fun accept(sessionId: String) {
        stop(sessionId)
    }

    actual fun setActionHandlers(onAccept: () -> Unit, onDecline: () -> Unit) = Unit

    actual fun setPushTokenUpdatedHandler(onTokenUpdated: () -> Unit) {
        pushTokenUpdatedHandler = onTokenUpdated
        if (!getPushToken().isNullOrBlank()) {
            onTokenUpdated()
        } else {
            incomingCallContext?.let(::refreshFCMToken)
        }
    }

    actual fun getPushToken(): String? = incomingCallContext
        ?.getSharedPreferences(PUSH_PREFERENCES, Context.MODE_PRIVATE)
        ?.getString(FCM_TOKEN_KEY, null)

    actual fun getPushPlatform(): String? = "android-fcm"
}

private fun refreshFCMToken(context: Context) {
    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
        updateFCMToken(context, token)
    }
}

private fun ensureIncomingCallChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val channel = NotificationChannel(
        INCOMING_CALL_CHANNEL_ID,
        "Incoming calls",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Rings for incoming TrueLine voice calls"
        setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
        )
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 500, 700)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
}

private fun showNotification(context: Context, callerName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    ensureIncomingCallChannel(context)
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        ?: return
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Notification.Builder(context, INCOMING_CALL_CHANNEL_ID)
    } else {
        Notification.Builder(context)
    }

    val notification = builder
        .setSmallIcon(context.applicationInfo.icon)
        .setContentTitle("Incoming voice call")
        .setContentText("$callerName is calling you")
        .setCategory(Notification.CATEGORY_CALL)
        .setPriority(Notification.PRIORITY_MAX)
        .setContentIntent(pendingIntent)
        .setOnlyAlertOnce(true)
        .setAutoCancel(true)
        .setOngoing(true)
        .build()

    context.getSystemService(NotificationManager::class.java)
        .notify(INCOMING_CALL_NOTIFICATION_ID, notification)
}
