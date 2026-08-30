@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.example.trueline_listener.call

import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSUserDefaults
import platform.darwin.NSObjectProtocol

private const val START_INCOMING_CALL_NOTIFICATION = "trueline.listener.incoming.start"
private const val ACCEPT_INCOMING_CALL_NOTIFICATION = "trueline.listener.incoming.accept"
private const val STOP_INCOMING_CALL_NOTIFICATION = "trueline.listener.incoming.stop"
private const val CALLKIT_ACCEPTED_NOTIFICATION = "trueline.listener.incoming.accepted"
private const val CALLKIT_DECLINED_NOTIFICATION = "trueline.listener.incoming.declined"
private const val PUSH_TOKEN_UPDATED_NOTIFICATION = "trueline.listener.voip-token.updated"
private const val PUSH_TOKEN_KEY = "trueline.listener.voip-token"
private const val ACTIVE_SESSION_KEY = "trueline.listener.incoming-session-id"
private const val ACCEPTED_SESSION_KEY = "trueline.listener.incoming-accepted-session-id"
private const val DECLINED_SESSION_KEY = "trueline.listener.incoming-declined-session-id"

actual object IncomingCallAlert {
    private val notificationCenter = NSNotificationCenter.defaultCenter
    private var activeSessionId: String? = null
    private var acceptedObserver: NSObjectProtocol? = null
    private var declinedObserver: NSObjectProtocol? = null
    private var pushTokenObserver: NSObjectProtocol? = null
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun start(sessionId: String, callerName: String) {
        if (activeSessionId == sessionId) return
        activeSessionId = sessionId
        defaults.setObject(sessionId, forKey = ACTIVE_SESSION_KEY)
        notificationCenter.postNotificationName(
            START_INCOMING_CALL_NOTIFICATION,
            null,
            mapOf(
                "sessionId" to sessionId,
                "callerName" to callerName.ifBlank { "Customer" }
            )
        )
    }

    actual fun accept(sessionId: String) {
        if (currentSessionId() != sessionId) return
        defaults.removeObjectForKey(ACCEPTED_SESSION_KEY)
        notificationCenter.postNotificationName(
            ACCEPT_INCOMING_CALL_NOTIFICATION,
            null,
            mapOf("sessionId" to sessionId)
        )
    }

    actual fun stop(sessionId: String?) {
        val sessionToStop = currentSessionId() ?: return
        if (sessionId != null && sessionId != sessionToStop) return
        activeSessionId = null
        defaults.removeObjectForKey(ACTIVE_SESSION_KEY)
        notificationCenter.postNotificationName(
            STOP_INCOMING_CALL_NOTIFICATION,
            null,
            mapOf("sessionId" to sessionToStop)
        )
    }

    actual fun setActionHandlers(onAccept: () -> Unit, onDecline: () -> Unit) {
        acceptedObserver?.let(notificationCenter::removeObserver)
        declinedObserver?.let(notificationCenter::removeObserver)

        acceptedObserver = notificationCenter.addObserverForName(
            CALLKIT_ACCEPTED_NOTIFICATION,
            null,
            NSOperationQueue.mainQueue
        ) {
            defaults.removeObjectForKey(ACCEPTED_SESSION_KEY)
            onAccept()
        }
        declinedObserver = notificationCenter.addObserverForName(
            CALLKIT_DECLINED_NOTIFICATION,
            null,
            NSOperationQueue.mainQueue
        ) {
            defaults.removeObjectForKey(DECLINED_SESSION_KEY)
            onDecline()
        }

        // PushKit can wake the process before Compose creates this object.
        // Preserve and replay the CallKit action once shared state is ready.
        val acceptedSessionId = defaults.stringForKey(ACCEPTED_SESSION_KEY)
        if (!acceptedSessionId.isNullOrBlank()) {
            activeSessionId = acceptedSessionId
            defaults.removeObjectForKey(ACCEPTED_SESSION_KEY)
            onAccept()
        }
        val declinedSessionId = defaults.stringForKey(DECLINED_SESSION_KEY)
        if (!declinedSessionId.isNullOrBlank()) {
            activeSessionId = declinedSessionId
            defaults.removeObjectForKey(DECLINED_SESSION_KEY)
            onDecline()
        }
    }

    actual fun setPushTokenUpdatedHandler(onTokenUpdated: () -> Unit) {
        pushTokenObserver?.let(notificationCenter::removeObserver)
        pushTokenObserver = notificationCenter.addObserverForName(
            PUSH_TOKEN_UPDATED_NOTIFICATION,
            null,
            NSOperationQueue.mainQueue
        ) {
            onTokenUpdated()
        }
    }

    actual fun getPushToken(): String? = NSUserDefaults.standardUserDefaults
        .stringForKey(PUSH_TOKEN_KEY)

    actual fun getPushPlatform(): String? = "ios-voip"

    private fun currentSessionId(): String? = activeSessionId ?: defaults.stringForKey(ACTIVE_SESSION_KEY)
}
