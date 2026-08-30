package com.example.trueline_listener.call

expect class CallServiceWrapper {
    fun initialize(appId: Long, userId: String, userName: String)
    fun startAudioCall(
        roomId: String,
        targetUserId: String,
        targetUserName: String,
        token: String = "",
        signedUserId: String = "",
        onCallEnd: () -> Unit = {},
        onCallStartFailed: (message: String) -> Unit = {}
    )
    fun endCall()
}

expect fun getCallService(): CallServiceWrapper
