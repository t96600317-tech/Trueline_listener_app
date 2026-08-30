package com.example.trueline_listener.otp

/**
 * Android implementations use MSG91's SendOTP SDK. Other platforms retain the
 * existing backend OTP flow until a native MSG91 SDK is available for them.
 */
data class Msg91OtpResult(
    val success: Boolean,
    val requestId: String? = null,
    val accessToken: String? = null,
    val errorMessage: String? = null
)

interface Msg91OtpGateway {
    val isConfigured: Boolean

    suspend fun sendOtp(identifier: String): Msg91OtpResult

    suspend fun retryOtp(requestId: String, channel: Number? = null): Msg91OtpResult

    /** Verifies the entered code with MSG91 and returns its signed access token. */
    suspend fun verifyOtp(requestId: String, otp: String): Msg91OtpResult
}

expect fun initMsg91Otp(widgetId: String, authToken: String)

expect fun getMsg91OtpGateway(): Msg91OtpGateway
