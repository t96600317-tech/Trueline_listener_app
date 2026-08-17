package com.example.trueline_listener.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null
)

@Serializable
data class ApiError(
    val code: String,
    val message: String
)

@Serializable
data class OtpRequest(
    val phone: String,
    val role: String = "listener"
)

@Serializable
data class OtpResponse(
    val message: String = "",
    val phone: String = "",
    val expires_in_seconds: Int = 300,
    val mock_otp: String? = null
)

@Serializable
data class OtpVerifyRequest(
    val phone: String,
    val otp: String,
    val role: String = "listener"
)

@Serializable
data class AuthResponse(
    val token: String,
    val role: String,
    val is_new_user: Boolean,
    val listener: ListenerProfile? = null,
    val onboarding_step: String? = null,
    val kyc_status: String? = null
)

@Serializable
data class ListenerProfile(
    val id: String = "",
    val name: String = "",
    val title: String = "",
    val bio: String = "",
    val languages: List<String> = emptyList(),
    val onboarding_step: String = "profile_setup",
    val kyc_status: String = "pending",
    val availability: String = "offline",
    val photo_url: String = "",
    val audio_sample_url: String = "",
    val rating_avg: Double = 4.8,
    val rating_count: Int = 0,
    val rate_per_min_micros: Long = 9000000,
    val earning_per_min_micros: Long = 3000000
)

@Serializable
data class UpdateProfileRequest(
    val name: String,
    val title: String,
    val bio: String,
    val languages: List<String>
)

@Serializable
data class EarningsSummaryResponse(
    val total_earned_micros: Long = 0,
    val total_earned_coins: Double = 0.0,
    val total_paid_micros: Long = 0,
    val total_paid_coins: Double = 0.0,
    val available_balance_micros: Long = 0,
    val available_balance_coins: Double = 0.0,
    val rate_per_minute_coins: Double = 3.0
)

@Serializable
data class PayoutRequestPayload(
    val amount_micros: Long,
    val upi_id: String
)

@Serializable
data class CallAcceptResponse(
    val listener_token: String
)

@Serializable
data class CallEvent(
    val type: String,
    val session_id: String? = null,
    val reason: String? = null
)

@Serializable
data class SimpleMessageResponse(
    val message: String? = null,
    val status: String? = null
)
