package com.example.trueline_listener.call

expect class CallServiceWrapper {
    fun initialize(appId: Long, appSign: String, userId: String, userName: String)
    fun startAudioCall(
        roomId: String,
        targetUserId: String,
        targetUserName: String,
        token: String = "",
        onCallEnd: () -> Unit = {}
    )
    fun endCall()
}

expect fun getCallService(): CallServiceWrapper
