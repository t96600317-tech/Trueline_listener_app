package com.example.trueline_listener.otp

import com.msg91.sendotp.OTPWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private data class Msg91Configuration(
    val widgetId: String,
    val authToken: String
) {
    val isConfigured: Boolean
        get() = widgetId.isNotBlank() && authToken.isNotBlank()
}

private var configuration = Msg91Configuration(widgetId = "", authToken = "")

actual fun initMsg91Otp(widgetId: String, authToken: String) {
    configuration = Msg91Configuration(widgetId.trim(), authToken.trim())
}

private object AndroidMsg91OtpGateway : Msg91OtpGateway {
    override val isConfigured: Boolean
        get() = configuration.isConfigured

    override suspend fun sendOtp(identifier: String): Msg91OtpResult = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Msg91OtpResult(false, errorMessage = "MSG91 is not configured for this build.")
        }

        try {
            val result = OTPWidget.sendOTP(
                configuration.widgetId,
                configuration.authToken,
                identifier.removePrefix("+")
            )
            parseSendResponse(result)
        } catch (_: Exception) {
            Msg91OtpResult(false, errorMessage = "Unable to contact MSG91. Please try again.")
        }
    }

    override suspend fun retryOtp(requestId: String, channel: Number?): Msg91OtpResult = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Msg91OtpResult(false, errorMessage = "MSG91 is not configured for this build.")
        }

        try {
            val result = OTPWidget.retryOTP(
                configuration.widgetId,
                configuration.authToken,
                requestId,
                channel
            )
            parseResponse(result, requestId)
        } catch (_: Exception) {
            Msg91OtpResult(false, errorMessage = "Unable to resend the OTP. Please try again.")
        }
    }

    override suspend fun verifyOtp(requestId: String, otp: String): Msg91OtpResult = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Msg91OtpResult(false, errorMessage = "MSG91 is not configured for this build.")
        }
        if (requestId.isBlank() || otp.isBlank()) {
            return@withContext Msg91OtpResult(false, errorMessage = "OTP request details are missing. Please request a new code.")
        }

        try {
            val result = OTPWidget.verifyOTP(
                configuration.widgetId,
                configuration.authToken,
                requestId,
                otp
            )
            parseVerificationResponse(result, requestId)
        } catch (_: Exception) {
            Msg91OtpResult(false, errorMessage = "Unable to verify the OTP. Please try again.")
        }
    }
}

actual fun getMsg91OtpGateway(): Msg91OtpGateway = AndroidMsg91OtpGateway

private fun parseSendResponse(rawResponse: String): Msg91OtpResult {
    val response = parseResponse(rawResponse)
    if (!response.success) return response

    val json = JSONObject(rawResponse)
    if (json.optBoolean("invisibleVerified", false)) {
        // A client-only invisible result cannot safely be exchanged for a
        // TrueLine session. Keep widget invisible verification disabled.
        return Msg91OtpResult(
            false,
            errorMessage = "MSG91 invisible verification is not supported for login. Please disable it in this widget."
        )
    }

    val requestId = json.optString("message").trim()
    return if (requestId.isBlank()) {
        Msg91OtpResult(false, errorMessage = "MSG91 did not return an OTP request ID. Please try again.")
    } else {
        Msg91OtpResult(true, requestId = requestId)
    }
}

private fun parseResponse(rawResponse: String, requestId: String? = null): Msg91OtpResult {
    return try {
        val json = JSONObject(rawResponse)
        val type = json.optString("type")
        val message = json.optString("message").trim()
        if (type.equals("error", ignoreCase = true)) {
            Msg91OtpResult(false, errorMessage = message.ifBlank { "MSG91 rejected the request." })
        } else {
            Msg91OtpResult(true, requestId = requestId)
        }
    } catch (_: Exception) {
        Msg91OtpResult(false, errorMessage = "MSG91 returned an invalid response. Please try again.")
    }
}

private fun parseVerificationResponse(rawResponse: String, requestId: String): Msg91OtpResult {
    val response = parseResponse(rawResponse, requestId)
    if (!response.success) return response

    return try {
        val json = JSONObject(rawResponse)
        val accessToken = json.findAccessToken()
        Msg91OtpResult(true, requestId = requestId, accessToken = accessToken)
    } catch (_: Exception) {
        Msg91OtpResult(false, errorMessage = "MSG91 returned an invalid verification response. Please try again.")
    }
}

private fun JSONObject.findAccessToken(): String? {
    val keys = listOf("access-token", "access_token", "accessToken")
    keys.firstNotNullOfOrNull { key ->
        optString(key).trim().takeIf { it.isNotEmpty() }
    }?.let { return it }

    optJSONObject("data")?.let { data ->
        keys.firstNotNullOfOrNull { key ->
            data.optString(key).trim().takeIf { it.isNotEmpty() }
        }?.let { return it }
    }

    return optString("message").trim().takeIf { it.count { character -> character == '.' } == 2 }
}
