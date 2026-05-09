package com.yuelutraffic.app.storage

import android.content.Context
import com.yuelutraffic.app.config.AppConfig
import com.yuelutraffic.app.config.normalizeBackendBaseUrl
import com.yuelutraffic.app.ui.StudentSessionUi

class SessionStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("yuelu_session", Context.MODE_PRIVATE)

    fun loadBackendBaseUrl(): String {
        return normalizeBackendBaseUrl(preferences.getString(KEY_API_BASE_URL, AppConfig.DEFAULT_API_BASE_URL))
    }

    fun saveBackendBaseUrl(value: String) {
        preferences.edit()
            .putString(KEY_API_BASE_URL, normalizeBackendBaseUrl(value))
            .apply()
    }

    fun loadSession(): StudentSessionUi? {
        val token = preferences.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val publicCode = preferences.getString(KEY_PUBLIC_CODE, null) ?: return null
        return StudentSessionUi(
            publicCode = publicCode,
            accessToken = token,
            role = preferences.getString(KEY_ROLE, "USER") ?: "USER",
            reputationScore = preferences.getInt(KEY_REPUTATION, 0),
            points = preferences.getInt(KEY_POINTS, 0),
            titleCode = preferences.getString(KEY_TITLE_CODE, "ROAD_HELPER") ?: "ROAD_HELPER",
            postingBanUntil = preferences.getString(KEY_POSTING_BAN_UNTIL, null),
            connectionMessage = "已从本机恢复登录状态，正在校验后端会话。",
            isDemoMode = false,
        )
    }

    fun saveSession(session: StudentSessionUi) {
        val token = session.accessToken ?: return
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_PUBLIC_CODE, session.publicCode)
            .putString(KEY_ROLE, session.role)
            .putInt(KEY_REPUTATION, session.reputationScore)
            .putInt(KEY_POINTS, session.points)
            .putString(KEY_TITLE_CODE, session.titleCode)
            .putString(KEY_POSTING_BAN_UNTIL, session.postingBanUntil)
            .apply()
    }

    fun clearSession() {
        preferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_PUBLIC_CODE)
            .remove(KEY_ROLE)
            .remove(KEY_REPUTATION)
            .remove(KEY_POINTS)
            .remove(KEY_TITLE_CODE)
            .remove(KEY_POSTING_BAN_UNTIL)
            .apply()
    }

    private companion object {
        const val KEY_API_BASE_URL = "apiBaseUrl"
        const val KEY_ACCESS_TOKEN = "accessToken"
        const val KEY_PUBLIC_CODE = "publicCode"
        const val KEY_ROLE = "role"
        const val KEY_REPUTATION = "reputationScore"
        const val KEY_POINTS = "points"
        const val KEY_TITLE_CODE = "titleCode"
        const val KEY_POSTING_BAN_UNTIL = "postingBanUntil"
    }
}
