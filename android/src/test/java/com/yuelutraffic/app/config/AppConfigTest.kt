package com.yuelutraffic.app.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppConfigTest {

    @Test
    fun backendBaseUrlTrimsTrailingSlash() {
        assertEquals("http://10.0.2.2:8080", normalizeBackendBaseUrl(" http://10.0.2.2:8080/ "))
    }

    @Test
    fun backendBaseUrlFallsBackWhenBlank() {
        assertEquals(AppConfig.DEFAULT_API_BASE_URL.trimEnd('/'), normalizeBackendBaseUrl(" "))
    }

    @Test
    fun supportedBackendUrlsMustUseHttp() {
        assertTrue(isSupportedBackendBaseUrl("http://192.168.1.8:8080"))
        assertTrue(isSupportedBackendBaseUrl("https://traffic.example.com"))
        assertFalse(isSupportedBackendBaseUrl("ftp://traffic.example.com"))
    }
}
