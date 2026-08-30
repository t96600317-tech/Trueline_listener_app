package com.example.trueline_listener.call

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build

private const val INCOMING_CALL_CHANNEL_ID = "incoming_calls"
private const val INCOMING_CALL_NOTIFICATION_ID = 72_001

private var incomingCallContext: Context? = null
private var activeSessionId: String? = null
private var activeRingtone: Ringtone? = null

fun initIncomingCallAlert(context: Context) {
    incomingCallContext = context.applicationContext
    ensureIncomingCallChannel(context.applicationContext)
}

actual object IncomingCallAlert {
    actual fun start(sessionId: String, callerName: String) {
        val context = incomingCallContext ?: return
        if (activeSessionId == sessionId) return

        stop()
        activeSessionId = sessionId
        playRingtone(context)
        showNotification(context, callerName.ifBlank { "Customer" })
    }

    actual fun stop(sessionId: String?) {
        if (sessionId != null && activeSessionId != sessionId) return

        activeRingtone?.stop()
        activeRingtone = null
        activeSessionId = null
        incomingCallContext?.getSystemService(NotificationManager::class.java)
            ?.cancel(INCOMING_CALL_NOTIFICATION_ID)
    }
}

private fun ensureIncomingCallChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
        .build()
    val channel = NotificationChannel(
        INCOMING_CALL_CHANNEL_ID,
        "Incoming calls",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Rings for incoming TrueLine voice calls"
        setSound(ringtoneUri, attributes)
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 500, 700)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
}

private fun playRingtone(context: Context) {
    val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    activeRingtone = RingtoneManager.getRingtone(context, ringtoneUri)?.also { ringtone ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.isLooping = true
        }
        ringtone.play()
    }
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
        .setAutoCancel(true)
        .setOngoing(true)
        .build()

    context.getSystemService(NotificationManager::class.java)
        .notify(INCOMING_CALL_NOTIFICATION_ID, notification)
}
