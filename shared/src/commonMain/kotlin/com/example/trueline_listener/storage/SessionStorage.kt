package com.example.trueline_listener.storage

interface SessionStorage {
    fun saveAuthToken(token: String)
    fun getAuthToken(): String?
    fun savePhone(phone: String)
    fun getPhone(): String?
    fun saveOnboardingStep(step: String)
    fun getOnboardingStep(): String?
    fun saveKYCStatus(status: String)
    fun getKYCStatus(): String?
    fun clearSession()
}

expect fun getSessionStorage(): SessionStorage
