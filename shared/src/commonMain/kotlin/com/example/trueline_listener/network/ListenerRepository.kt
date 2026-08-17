package com.example.trueline_listener.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.trueline_listener.storage.getSessionStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ListenerRepository(
    private var primaryHost: String = "127.0.0.1:8080"
) {
    private val storage = getSessionStorage()
    private var authToken: String? = storage.getAuthToken()
    private val candidateHosts = listOf(
        "127.0.0.1:8080",
        "localhost:8080",
        "192.168.1.6:8080",
        "10.0.2.2:8080"
    ).distinct()

    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 2500
            socketTimeoutMillis = 15000
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    fun setAuthToken(token: String) {
        authToken = token
        storage.saveAuthToken(token)
    }

    fun getAuthToken(): String? {
        if (authToken == null) {
            authToken = storage.getAuthToken()
        }
        return authToken
    }

    fun clearAuthSession() {
        authToken = null
        storage.clearSession()
    }

    private suspend inline fun <reified T> executeWithFallback(
        crossinline block: suspend (host: String) -> T
    ): T {
        var lastException: Exception? = null
        for (host in candidateHosts) {
            try {
                val result = block(host)
                primaryHost = host // Remember working host
                return result
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw (lastException ?: Exception("Failed to connect to any backend host ($candidateHosts)"))
    }

    suspend fun requestOtp(phone: String): ApiResponse<OtpResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/auth/otp/request") {
                    contentType(ContentType.Application.Json)
                    setBody(OtpRequest(phone, "listener"))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to connect to backend service"))
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): ApiResponse<AuthResponse> {
        val response: ApiResponse<AuthResponse> = try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/auth/otp/verify") {
                    contentType(ContentType.Application.Json)
                    setBody(OtpVerifyRequest(phone, otp, "listener"))
                }.body()
            }
        } catch (e: Exception) {
            return ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to connect to backend service"))
        }

        if (response.success && response.data != null) {
            setAuthToken(response.data.token)
            storage.savePhone(phone)
        }
        return response
    }

    suspend fun getMe(): ApiResponse<ListenerProfile> {
        val token = getAuthToken()
        val response: ApiResponse<ListenerProfile> = try {
            executeWithFallback { host ->
                client.get("http://$host/api/v1/listener/me") {
                    token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to fetch profile"))
        }

        if (response.success && response.data != null) {
            storage.saveOnboardingStep(response.data.onboarding_step)
            storage.saveKYCStatus(response.data.kyc_status)
        }
        return response
    }

    suspend fun acceptCall(sessionId: String): ApiResponse<CallAcceptResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/calls/$sessionId/accept") {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Unknown error"))
        }
    }

    suspend fun endCall(sessionId: String, reason: String = "listener_hangup"): ApiResponse<SimpleMessageResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/calls/$sessionId/end") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    setBody(mapOf("reason" to reason))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Unknown error"))
        }
    }

    fun observeCallEvents(sessionId: String): Flow<CallEvent> = flow {
        val host = primaryHost.split(":")[0]
        val port = primaryHost.split(":").getOrNull(1)?.toInt() ?: 8080
        client.webSocket(
            method = HttpMethod.Get,
            host = host,
            port = port,
            path = "/api/v1/calls/$sessionId/events?token=$authToken"
        ) {
            while (true) {
                try {
                    val event = receiveDeserialized<CallEvent>()
                    emit(event)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    suspend fun updateProfile(name: String, title: String, bio: String, languages: List<String>): ApiResponse<ListenerProfile> {
        return try {
            executeWithFallback { host ->
                client.patch("http://$host/api/v1/listener/onboarding/profile") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    setBody(UpdateProfileRequest(name, title, bio, languages))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to update profile"))
        }
    }

    suspend fun updateVoiceIntro(audioUrl: String): ApiResponse<ListenerProfile> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/listener/onboarding/voice") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    setBody(mapOf("audio_url" to audioUrl))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to upload voice sample"))
        }
    }

    suspend fun submitPAN(pan: String): ApiResponse<SimpleMessageResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/listener/onboarding/kyc/pan") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    setBody(mapOf("pan" to pan))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to submit PAN"))
        }
    }

    suspend fun submitBank(accountNumber: String, ifsc: String): ApiResponse<SimpleMessageResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/listener/onboarding/kyc/bank") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    setBody(mapOf("account_number" to accountNumber, "ifsc" to ifsc))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to submit bank account"))
        }
    }

    suspend fun submitSelfie(selfieUrl: String, livenessScore: Double = 0.98): ApiResponse<SimpleMessageResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/listener/onboarding/kyc/selfie") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    setBody(mapOf("selfie_url" to selfieUrl, "liveness_score" to livenessScore))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to submit selfie"))
        }
    }

    suspend fun submitAgreement(version: String = "1.0"): ApiResponse<SimpleMessageResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/listener/onboarding/kyc/agreement") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    setBody(mapOf("agreement_version" to version))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to submit agreement"))
        }
    }

    suspend fun submitOnboarding(): ApiResponse<SimpleMessageResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/listener/onboarding/submit") {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Submission timed out. Please retry."))
        }
    }

    suspend fun setAvailability(availability: String): ApiResponse<SimpleMessageResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/listener/availability") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    setBody(mapOf("availability" to availability))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Unknown error"))
        }
    }

    suspend fun getEarnings(): ApiResponse<EarningsSummaryResponse> {
        return try {
            executeWithFallback { host ->
                client.get("http://$host/api/v1/listener/earnings") {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Unknown error"))
        }
    }

    suspend fun getHomeDashboard(): ApiResponse<HomeDashboardResponse> {
        return try {
            executeWithFallback { host ->
                client.get("http://$host/api/v1/listener/home") {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to load dashboard"))
        }
    }

    suspend fun getMilestones(): ApiResponse<MilestonesHubResponse> {
        return try {
            executeWithFallback { host ->
                client.get("http://$host/api/v1/listener/milestones") {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to load milestones"))
        }
    }

    suspend fun getPerformanceScore(): ApiResponse<PerformanceScoreResponse> {
        return try {
            executeWithFallback { host ->
                client.get("http://$host/api/v1/listener/score") {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to load performance score"))
        }
    }

    suspend fun getDetailedEarnings(): ApiResponse<DetailedEarningsResponse> {
        return try {
            executeWithFallback { host ->
                client.get("http://$host/api/v1/listener/detailed-earnings") {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to load earnings"))
        }
    }

    suspend fun requestWithdrawal(amountCoins: Double, upiId: String): ApiResponse<WithdrawResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/listener/withdraw") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    setBody(mapOf("amount_coins" to amountCoins, "upi_id" to upiId))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to process withdrawal"))
        }
    }

    suspend fun getBlockedUsers(): ApiResponse<List<BlockedUserItem>> {
        return try {
            executeWithFallback { host ->
                client.get("http://$host/api/v1/listener/blocked-users") {
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to load blocked users"))
        }
    }

    suspend fun submitReport(reason: String, details: String): ApiResponse<SimpleMessageResponse> {
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/listener/reports") {
                    contentType(ContentType.Application.Json)
                    authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    setBody(mapOf("reason" to reason, "details" to details))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to submit report"))
        }
    }

    // --- Chat API ---
    suspend fun getChatConversations(): ApiResponse<List<ChatConversationData>> {
        val token = getAuthToken() ?: return ApiResponse(false, error = ApiError("UNAUTHORIZED", "Not logged in"))
        return try {
            executeWithFallback { host ->
                client.get("http://$host/api/v1/chats") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to fetch conversations"))
        }
    }

    suspend fun getChatMessages(targetUserId: String): ApiResponse<List<ChatMessageData>> {
        val token = getAuthToken() ?: return ApiResponse(false, error = ApiError("UNAUTHORIZED", "Not logged in"))
        return try {
            executeWithFallback { host ->
                client.get("http://$host/api/v1/chats/$targetUserId/messages") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to fetch messages"))
        }
    }

    suspend fun sendChatMessage(targetUserId: String, content: String): ApiResponse<ChatMessageData> {
        val token = getAuthToken() ?: return ApiResponse(false, error = ApiError("UNAUTHORIZED", "Not logged in"))
        return try {
            executeWithFallback { host ->
                client.post("http://$host/api/v1/chats/$targetUserId/messages") {
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $token")
                    setBody(SendMessageRequest(content))
                }.body()
            }
        } catch (e: Exception) {
            ApiResponse(false, error = ApiError("NETWORK_ERROR", e.message ?: "Failed to send message"))
        }
    }
}

@Serializable
data class RecentCallItem(
    val id: String = "",
    val caller_name: String = "",
    val caller_initial: String = "",
    val duration_minutes: Int = 0,
    val time_string: String = "",
    val is_repeat_caller: Boolean = false,
    val gift_received: String = "",
    val earning_coins: Double = 0.0
)

@Serializable
data class HomeDashboardResponse(
    val listener_name: String = "",
    val listener_id_tag: String = "TL-P-00214",
    val kyc_status: String = "approved",
    val availability: String = "offline",
    val today_earnings_coins: Double = 0.0,
    val today_minutes: Int = 0,
    val today_calls: Int = 0,
    val this_week_earnings_coins: Double = 0.0,
    val rating_avg: Double = 4.8,
    val total_calls_count: Int = 0,
    val recent_calls: List<RecentCallItem> = emptyList()
)

@Serializable
data class MilestoneItem(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val reward_coins: Double = 0.0,
    val is_completed: Boolean = false,
    val current_progress: Int = 0,
    val target_progress: Int = 1
)

@Serializable
data class MilestonesHubResponse(
    val listener_name: String = "",
    val week_one_guarantee_amount: Double = 1500.0,
    val milestones: List<MilestoneItem> = emptyList()
)

@Serializable
data class PerformanceScoreResponse(
    val score: Int = 82,
    val tier: String = "GOLD",
    val rank_text: String = "Rank 7 of 54 listeners · updated weekly",
    val repeat_callers_pct: Int = 78,
    val answer_rate_pct: Int = 91,
    val rating_score: Double = 4.8,
    val tips: List<String> = emptyList()
)

@Serializable
data class PastPayoutItem(
    val id: String = "",
    val title: String = "",
    val date_string: String = "",
    val status: String = "completed",
    val amount_coins: Double = 0.0
)

@Serializable
data class DetailedEarningsResponse(
    val available_to_withdraw_coins: Double = 0.0,
    val registered_upi: String = "",
    val call_earnings_coins: Double = 0.0,
    val call_hours_string: String = "",
    val gifts_received_coins: Double = 0.0,
    val gifts_count_string: String = "",
    val gold_tier_bonus_coins: Double = 0.0,
    val tier_bonus_subtitle: String = "",
    val past_payouts: List<PastPayoutItem> = emptyList()
)

@Serializable
data class WithdrawResponse(
    val payout_id: String = "",
    val requested_amount: Double = 0.0,
    val tds_amount: Double = 0.0,
    val net_amount: Double = 0.0,
    val status: String = "pending",
    val message: String = ""
)

@Serializable
data class BlockedUserItem(
    val id: String = "",
    val user_name: String = "",
    val blocked_date: String = "",
    val reason: String = ""
)
