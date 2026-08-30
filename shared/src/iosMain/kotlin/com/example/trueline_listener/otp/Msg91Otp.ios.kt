package com.example.trueline_listener.otp

private object UnsupportedMsg91OtpGateway : Msg91OtpGateway {
    override val isConfigured: Boolean = false

    override suspend fun sendOtp(identifier: String): Msg91OtpResult =
        Msg91OtpResult(false, errorMessage = "MSG91's Android SDK is unavailable on iOS.")

    override suspend fun retryOtp(requestId: String, channel: Number?): Msg91OtpResult =
        Msg91OtpResult(false, errorMessage = "MSG91's Android SDK is unavailable on iOS.")

    override suspend fun verifyOtp(requestId: String, otp: String): Msg91OtpResult =
        Msg91OtpResult(false, errorMessage = "MSG91's Android SDK is unavailable on iOS.")
}

actual fun initMsg91Otp(widgetId: String, authToken: String) = Unit

actual fun getMsg91OtpGateway(): Msg91OtpGateway = UnsupportedMsg91OtpGateway
