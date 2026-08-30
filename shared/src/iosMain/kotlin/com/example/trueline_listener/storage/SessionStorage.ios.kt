@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.example.trueline_listener.storage

import platform.Foundation.NSUserDefaults

private object IOSListenerSessionStorage : SessionStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun saveAuthToken(token: String) = defaults.setObject(token, forKey = AUTH_TOKEN)
    override fun getAuthToken(): String? = defaults.stringForKey(AUTH_TOKEN)

    override fun savePhone(phone: String) = defaults.setObject(phone, forKey = PHONE)
    override fun getPhone(): String? = defaults.stringForKey(PHONE)

    override fun saveOnboardingStep(step: String) = defaults.setObject(step, forKey = ONBOARDING_STEP)
    override fun getOnboardingStep(): String? = defaults.stringForKey(ONBOARDING_STEP)

    override fun saveKYCStatus(status: String) = defaults.setObject(status, forKey = KYC_STATUS)
    override fun getKYCStatus(): String? = defaults.stringForKey(KYC_STATUS)

    override fun clearSession() {
        listenerSessionKeys.forEach(defaults::removeObjectForKey)
    }
}

actual fun getSessionStorage(): SessionStorage = IOSListenerSessionStorage

private const val AUTH_TOKEN = "auth_token"
private const val PHONE = "saved_phone"
private const val ONBOARDING_STEP = "onboarding_step"
private const val KYC_STATUS = "kyc_status"

private val listenerSessionKeys = listOf(AUTH_TOKEN, PHONE, ONBOARDING_STEP, KYC_STATUS)
