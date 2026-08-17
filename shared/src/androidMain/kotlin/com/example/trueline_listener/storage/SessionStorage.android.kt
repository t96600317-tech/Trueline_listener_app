package com.example.trueline_listener.storage

import android.content.Context
import android.content.SharedPreferences

class AndroidSessionStorage(private val context: Context) : SessionStorage {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("trueline_listener_session", Context.MODE_PRIVATE)
    }

    override fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    override fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    override fun savePhone(phone: String) {
        prefs.edit().putString("saved_phone", phone).apply()
    }

    override fun getPhone(): String? {
        return prefs.getString("saved_phone", null)
    }

    override fun saveOnboardingStep(step: String) {
        prefs.edit().putString("onboarding_step", step).apply()
    }

    override fun getOnboardingStep(): String? {
        return prefs.getString("onboarding_step", null)
    }

    override fun saveKYCStatus(status: String) {
        prefs.edit().putString("kyc_status", status).apply()
    }

    override fun getKYCStatus(): String? {
        return prefs.getString("kyc_status", null)
    }

    override fun clearSession() {
        prefs.edit().clear().apply()
    }
}

private var globalSessionStorage: SessionStorage? = null

fun initSessionStorage(context: Context) {
    globalSessionStorage = AndroidSessionStorage(context.applicationContext)
}

actual fun getSessionStorage(): SessionStorage {
    return globalSessionStorage ?: object : SessionStorage {
        private val inMemory = mutableMapOf<String, String>()
        override fun saveAuthToken(token: String) { inMemory["auth_token"] = token }
        override fun getAuthToken(): String? = inMemory["auth_token"]
        override fun savePhone(phone: String) { inMemory["saved_phone"] = phone }
        override fun getPhone(): String? = inMemory["saved_phone"]
        override fun saveOnboardingStep(step: String) { inMemory["onboarding_step"] = step }
        override fun getOnboardingStep(): String? = inMemory["onboarding_step"]
        override fun saveKYCStatus(status: String) { inMemory["kyc_status"] = status }
        override fun getKYCStatus(): String? = inMemory["kyc_status"]
        override fun clearSession() { inMemory.clear() }
    }
}
