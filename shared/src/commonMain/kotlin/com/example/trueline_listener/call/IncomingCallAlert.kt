package com.example.trueline_listener.call

expect object IncomingCallAlert {
    fun start(sessionId: String, callerName: String)
    fun stop(sessionId: String? = null)
}
