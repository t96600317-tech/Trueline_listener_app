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

actual object IncomingCallAlert {
    private val notificationCenter = NSNotificationCenter.defaultCenter
    private var activeSessionId: String? = null
    private var acceptedObserver: NSObjectProtocol? = null
    private var declinedObserver: NSObjectProtocol? = null
    private var pushTokenObserver: NSObjectProtocol? = null

    actual fun start(sessionId: String, callerName: String) {
        if (activeSessionId == sessionId) return
        activeSessionId = sessionId
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
        if (activeSessionId != sessionId) return
        notificationCenter.postNotificationName(
            ACCEPT_INCOMING_CALL_NOTIFICATION,
            null,
            mapOf("sessionId" to sessionId)
        )
    }

    actual fun stop(sessionId: String?) {
        if (sessionId != null && activeSessionId != sessionId) return
        val sessionToStop = activeSessionId ?: return
        activeSessionId = null
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
            onAccept()
        }
        declinedObserver = notificationCenter.addObserverForName(
            CALLKIT_DECLINED_NOTIFICATION,
            null,
            NSOperationQueue.mainQueue
        ) {
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
}
