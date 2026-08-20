package com.example.trueline_listener

import com.example.trueline_listener.network.OtpVerifyRequest
import com.example.trueline_listener.network.ListenerRepository
import com.example.trueline_listener.storage.SessionStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun listenerOtpVerificationSerializesItsRole() {
        val payload = Json.encodeToString(OtpVerifyRequest("9308063500", "123456", "listener"))

        assertTrue(payload.contains("\"role\":\"listener\""))
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
