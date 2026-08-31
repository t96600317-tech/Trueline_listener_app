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
    val role: String
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
    val role: String,
    val request_id: String? = null,
    val msg91_access_token: String? = null
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
    val listener_token: String = "",
    val room_id: String = "",
    val zego_user_id: String = ""
)

@Serializable
data class CallSessionData(
    val id: String = "",
    val room_id: String = "",
    val caller_id: String = "",
    val caller_name: String = "Caller",
    val status: String = "pending",
    val rate_per_min: Double = 9.0,
    val earning_per_min: Double = 4.5
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

@Serializable
data class ChatConversationData(
    val partner_id: String = "",
    val partner_name: String = "",
    val partner_title: String = "",
    val partner_photo_url: String = "",
    val partner_availability: String = "online",
    val listener_id: String = "",
    val listener_name: String = "",
    val listener_title: String = "",
    val listener_photo_url: String = "",
    val listener_availability: String = "online",
    val user_id: String = "",
    val user_name: String = "",
    val user_title: String = "",
    val user_photo_url: String = "",
    val user_availability: String = "online",
    val last_message: String = "",
    val last_message_sender: String = "",
    val last_message_time: String = "",
    val unread_count: Int = 0,
    val is_regular: Boolean = false
) {
    val displayUserId: String
        get() = user_id.ifBlank { partner_id.ifBlank { listener_id } }

    val displayUserName: String
        get() = user_name.ifBlank { partner_name.ifBlank { listener_name.ifBlank { "user${displayUserId.takeLast(6)}" } } }
}

@Serializable
data class ChatMessageData(
    val id: String = "",
    val user_id: String = "",
    val partner_id: String = "",
    val sender_type: String = "",
    val content: String = "",
    val created_at: String = "",
    val read_at: String? = null
)

@Serializable
data class SendMessageRequest(
    val content: String
)

@Serializable
data class CallLogHistoryItem(
    val id: String = "",
    val avatar_text: String = "US",
    val caller_name: String = "",
    val is_missed: Boolean = false,
    val timestamp_details: String = "",
    val amount_str: String = "₹0",
    val is_negative: Boolean = false,
    val is_peach_avatar: Boolean = false,
    val section: String = "TODAY",
    val created_at: String = ""
)

@Serializable
data class CallHistoryResponse(
    val total_answered: Int = 0,
    val avg_duration_min: Double = 0.0,
    val avg_rating: Double = 0.0,
    val rating_count: Int = 0,
    val calls: List<CallLogHistoryItem> = emptyList()
)

@Serializable
data class TransactionItemData(
    val id: String = "",
    val title: String = "",
    val timestamp: String = "",
    val amount: String = "₹0.00",
    val status: String = "Cleared",
    val status_color: String = "gray",
    val is_positive: Boolean = true,
    val filter_type: String = "CALLS", // "CALLS", "BONUS", "PAYOUT", "PENALTY"
    val month_group: String = "AUG 2026",
    val created_at: String = ""
)

@Serializable
data class TransactionsResponse(
    val transactions: List<TransactionItemData> = emptyList()
)
@Serializable
data class NotificationItemData(
    val id: String = "",
    val type: String = "SYSTEM", // "PAYOUT", "BONUS", "RATING", "MISSED_CALL", "KYC"
    val title: String = "",
    val message: String = "",
    val timestamp: String = "",
    val is_read: Boolean = false,
    val icon_type: String = "bell",
    val created_at: String = ""
)

@Serializable
data class NotificationsResponse(
    val unread_count: Int = 0,
    val notifications: List<NotificationItemData> = emptyList()
)
