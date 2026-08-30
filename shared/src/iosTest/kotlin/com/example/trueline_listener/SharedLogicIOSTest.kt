package com.example.trueline_listener

import com.example.trueline_listener.otp.getMsg91OtpGateway
import com.example.trueline_listener.call.getCallService
import com.example.trueline_listener.storage.getSessionStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SharedLogicIOSTest {

    @Test
    fun iosUsesTheBackendOtpFallbackUntilTheNativeMsg91BridgeExists() {
        assertFalse(getMsg91OtpGateway().isConfigured)
    }

    @Test
    fun iosPlatformIdentificationIsAvailableToSharedCode() {
        assertTrue(getPlatform().name.contains("iOS"))
    }

    @Test
    fun iosProvidesAPlatformTimestampForOptimisticMessageIds() {
        assertTrue(currentPlatformTimeMillis() > 0)
    }

    @Test
    fun iosSessionStoragePersistsAndClearsListenerAuthentication() {
        val storage = getSessionStorage()
        storage.clearSession()

        storage.saveAuthToken("listener-token")
        storage.savePhone("919876543210")
        storage.saveOnboardingStep("profile_setup")

        assertEquals("listener-token", storage.getAuthToken())
        assertEquals("919876543210", storage.getPhone())
        assertEquals("profile_setup", storage.getOnboardingStep())

        storage.clearSession()

        assertNull(storage.getAuthToken())
        assertNull(storage.getPhone())
        assertNull(storage.getOnboardingStep())
    }

    @Test
    fun iosCallBridgeRejectsAnEmptyServerIssuedToken() {
        var error: String? = null

        getCallService().startAudioCall(
            roomId = "room-42",
            targetUserId = "customer-42",
            targetUserName = "Customer",
            token = "",
            onCallStartFailed = { error = it }
        )

        assertEquals("A Zego token is required to start a voice call", error)
    }
}
