package com.yuelutraffic.app.config

import com.yuelutraffic.app.BuildConfig

object AppConfig {
    val DEFAULT_API_BASE_URL: String = BuildConfig.API_BASE_URL
}

fun normalizeBackendBaseUrl(rawValue: String?): String {
    val value = rawValue?.trim().orEmpty()
    val fallback = AppConfig.DEFAULT_API_BASE_URL.trim().trimEnd('/')
    if (value.isBlank()) {
        return fallback
    }
    return value.trimEnd('/')
}

fun isSupportedBackendBaseUrl(value: String): Boolean {
    return value.startsWith("http://") || value.startsWith("https://")
}
