package com.ms.dm.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_AUTH_METHOD = "auth_method"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"

        // 7 days in milliseconds = 7 * 24 * 60 * 60 * 1000 = 604,800,000 ms
        const val SEVEN_DAYS_MS = 7L * 24L * 60L * 60L * 1000L
    }

    fun saveSession(email: String, authMethod: String = "EMAIL") {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_AUTH_METHOD, authMethod)
            .putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun isSessionValid(): Boolean {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return false

        val loginTime = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0L)
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - loginTime

        val isValid = elapsed in 0..<SEVEN_DAYS_MS
        if (!isValid) {
            clearSession()
        }
        return isValid
    }

    fun getRemainingDays(): Int {
        if (!isSessionValid()) return 0
        val loginTime = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0L)
        val elapsed = System.currentTimeMillis() - loginTime
        val remainingMs = SEVEN_DAYS_MS - elapsed
        return (remainingMs / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(1)
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun getAuthMethod(): String {
        return prefs.getString(KEY_AUTH_METHOD, "EMAIL") ?: "EMAIL"
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
