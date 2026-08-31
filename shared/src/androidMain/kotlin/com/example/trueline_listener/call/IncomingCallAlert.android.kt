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
private const val ACTIVE_SESSION_KEY = "active_incoming_session"
private const val PENDING_ACTION_KEY = "pending_incoming_action"

const val EXTRA_INCOMING_CALL_SESSION_ID = "com.example.trueline_listener.extra.INCOMING_CALL_SESSION_ID"
const val EXTRA_INCOMING_CALL_ACTION = "com.example.trueline_listener.extra.INCOMING_CALL_ACTION"
private const val ACTION_OPEN = "open"
private const val ACTION_ACCEPT = "accept"
private const val ACTION_DECLINE = "decline"

private var incomingCallContext: Context? = null
private var activeSessionId: String? = null
private var pushTokenUpdatedHandler: (() -> Unit)? = null
private var acceptActionHandler: (() -> Unit)? = null
private var declineActionHandler: (() -> Unit)? = null

fun initIncomingCallAlert(context: Context) {
    incomingCallContext = context.applicationContext
    activeSessionId = preferences(context).getString(ACTIVE_SESSION_KEY, null)
    ensureIncomingCallChannel(context.applicationContext)
    refreshFCMToken(context.applicationContext)
}

// Notification action PendingIntents deliberately launch the app instead of
// a background receiver. Android permits this user-initiated launch even when
// the process was killed, and the action is persisted until Compose is ready.
fun handleIncomingCallNotificationIntent(intent: Intent) {
    val context = incomingCallContext ?: return
    val sessionId = intent.getStringExtra(EXTRA_INCOMING_CALL_SESSION_ID).orEmpty()
    if (sessionId.isBlank()) return

    val action = intent.getStringExtra(EXTRA_INCOMING_CALL_ACTION).orEmpty()
    activeSessionId = sessionId
    preferences(context).edit().putString(ACTIVE_SESSION_KEY, sessionId).apply()

    when (action) {
        ACTION_ACCEPT -> {
            val handler = acceptActionHandler
            if (handler == null) {
                saveDeferredAction(context, sessionId, PendingIncomingCallAction.ACCEPT)
            } else {
                handler()
            }
        }
        ACTION_DECLINE -> {
            val handler = declineActionHandler
            if (handler == null) {
                saveDeferredAction(context, sessionId, PendingIncomingCallAction.DECLINE)
            } else {
                handler()
            }
        }
        else -> saveDeferredAction(context, sessionId, PendingIncomingCallAction.OPEN)
    }
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
        preferences(context).edit().putString(ACTIVE_SESSION_KEY, sessionId).apply()
        showNotification(context, sessionId, callerName.ifBlank { "Customer" })
    }

    actual fun stop(sessionId: String?) {
        if (sessionId != null && activeSessionId != sessionId) return

        activeSessionId = null
        preferences(incomingCallContext ?: return).edit()
            .remove(ACTIVE_SESSION_KEY)
            .remove(PENDING_ACTION_KEY)
            .apply()
        incomingCallContext?.getSystemService(NotificationManager::class.java)
            ?.cancel(INCOMING_CALL_NOTIFICATION_ID)
    }

    actual fun accept(sessionId: String) {
        stop(sessionId)
    }

    actual fun setActionHandlers(onAccept: () -> Unit, onDecline: () -> Unit) {
        acceptActionHandler = onAccept
        declineActionHandler = onDecline
    }

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

    actual fun pendingSessionId(): String? = activeSessionId
        ?: incomingCallContext?.let { preferences(it).getString(ACTIVE_SESSION_KEY, null) }

    actual fun consumeDeferredAction(): DeferredIncomingCallAction? {
        val context = incomingCallContext ?: return null
        val prefs = preferences(context)
        val serialized = prefs.getString(PENDING_ACTION_KEY, null) ?: return null
        prefs.edit().remove(PENDING_ACTION_KEY).apply()
        val separator = serialized.indexOf('|')
        if (separator <= 0) return null
        val sessionId = serialized.substring(0, separator)
        val action = when (serialized.substring(separator + 1)) {
            ACTION_ACCEPT -> PendingIncomingCallAction.ACCEPT
            ACTION_DECLINE -> PendingIncomingCallAction.DECLINE
            else -> PendingIncomingCallAction.OPEN
        }
        return DeferredIncomingCallAction(sessionId, action)
    }
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

private fun showNotification(context: Context, sessionId: String, callerName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    ensureIncomingCallChannel(context)
    val pendingIntent = callActionPendingIntent(context, sessionId, ACTION_OPEN, 1) ?: return
    val acceptPendingIntent = callActionPendingIntent(context, sessionId, ACTION_ACCEPT, 2) ?: return
    val declinePendingIntent = callActionPendingIntent(context, sessionId, ACTION_DECLINE, 3) ?: return
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
        .addAction(Notification.Action.Builder(android.R.drawable.ic_menu_call, "Accept", acceptPendingIntent).build())
        .addAction(Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "Decline", declinePendingIntent).build())
        .setOnlyAlertOnce(true)
        .setAutoCancel(true)
        .setOngoing(true)
        .build()

    context.getSystemService(NotificationManager::class.java)
        .notify(INCOMING_CALL_NOTIFICATION_ID, notification)
}

private fun callActionPendingIntent(context: Context, sessionId: String, action: String, actionCode: Int): PendingIntent? {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_INCOMING_CALL_SESSION_ID, sessionId)
            putExtra(EXTRA_INCOMING_CALL_ACTION, action)
        }
        ?: return null
    return PendingIntent.getActivity(
        context,
        100_000 + sessionId.hashCode() + actionCode,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun preferences(context: Context) = context.applicationContext
    .getSharedPreferences(PUSH_PREFERENCES, Context.MODE_PRIVATE)

private fun saveDeferredAction(context: Context, sessionId: String, action: PendingIncomingCallAction) {
    val serializedAction = when (action) {
        PendingIncomingCallAction.ACCEPT -> ACTION_ACCEPT
        PendingIncomingCallAction.DECLINE -> ACTION_DECLINE
        PendingIncomingCallAction.OPEN -> ACTION_OPEN
    }
    preferences(context).edit().putString(PENDING_ACTION_KEY, "$sessionId|$serializedAction").apply()
}
