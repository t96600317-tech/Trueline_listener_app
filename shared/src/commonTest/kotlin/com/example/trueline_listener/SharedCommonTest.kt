package com.example.trueline_listener

import com.example.trueline_listener.network.CallAcceptResponse
import com.example.trueline_listener.network.ListenerRepository
import com.example.trueline_listener.network.OtpVerifyRequest
import com.example.trueline_listener.storage.SessionStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun listenerOtpVerificationPassesTheMsg91AccessTokenToTheBackend() {
        val payload = Json.encodeToString(
            OtpVerifyRequest(
                phone = "919876543210",
                otp = "123456",
                role = "listener",
                request_id = "msg91-request-id",
                msg91_access_token = "msg91-success-token"
            )
        )

        assertTrue(payload.contains("\"role\":\"listener\""))
        assertTrue(payload.contains("\"request_id\":\"msg91-request-id\""))
        assertTrue(payload.contains("\"msg91_access_token\":\"msg91-success-token\""))
    }

    @Test
    fun callAcceptResponseRetainsTheServerSignedZegoIdentity() {
        val response = Json.decodeFromString<CallAcceptResponse>(
            """{"listener_token":"signed-token","room_id":"room-42","zego_user_id":"listener-42"}"""
        )

        assertEquals("signed-token", response.listener_token)
        assertEquals("room-42", response.room_id)
        assertEquals("listener-42", response.zego_user_id)
    }

    @Test
    fun callAcceptResponseSupportsServersThatDoNotSendTheOptionalZegoUserId() {
        val response = Json.decodeFromString<CallAcceptResponse>(
            """{"listener_token":"signed-token","room_id":"room-42"}"""
        )

        assertEquals("", response.zego_user_id)
    }

    @Test
    fun repositoryRefreshesTheTokenWhenAnotherRepositorySignsIn() {
        val storage = InMemorySessionStorage(token = "customer-token")
        val repository = ListenerRepository(storage = storage)

        storage.token = "listener-token"

        assertEquals("listener-token", repository.getAuthToken())
    }

    private class InMemorySessionStorage(var token: String? = null) : SessionStorage {
        override fun saveAuthToken(token: String) { this.token = token }
        override fun getAuthToken(): String? = token
        override fun savePhone(phone: String) = Unit
        override fun getPhone(): String? = null
        override fun saveOnboardingStep(step: String) = Unit
        override fun getOnboardingStep(): String? = null
        override fun saveKYCStatus(status: String) = Unit
        override fun getKYCStatus(): String? = null
        override fun clearSession() { token = null }
    }
}
