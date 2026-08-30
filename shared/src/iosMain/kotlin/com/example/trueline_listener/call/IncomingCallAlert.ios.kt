package com.example.trueline_listener.call

actual object IncomingCallAlert {
    actual fun start(sessionId: String, callerName: String) = Unit
    actual fun stop(sessionId: String?) = Unit
}
