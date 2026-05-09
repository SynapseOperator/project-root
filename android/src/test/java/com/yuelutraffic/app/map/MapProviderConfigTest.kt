package com.yuelutraffic.app.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapProviderConfigTest {

    @Test
    fun providerUsesMockWhenKeyIsMissing() {
        val config = mapProviderRuntimeConfig(amapApiKey = " ", forceMockMap = false)

        assertEquals(MapProviderMode.MOCK, config.mode)
        assertTrue(config.statusMessage.contains("模拟地图"))
    }

    @Test
    fun providerUsesAmapWhenKeyIsConfigured() {
        val config = mapProviderRuntimeConfig(amapApiKey = "abc123", forceMockMap = false)

        assertEquals(MapProviderMode.AMAP, config.mode)
        assertEquals("abc123", config.amapApiKey)
        assertTrue(config.statusMessage.contains("真实地图"))
    }

    @Test
    fun forceMockOverridesConfiguredKey() {
        val config = mapProviderRuntimeConfig(amapApiKey = "abc123", forceMockMap = true)

        assertEquals(MapProviderMode.MOCK, config.mode)
        assertTrue(config.statusMessage.contains("强制模拟地图"))
    }
}
