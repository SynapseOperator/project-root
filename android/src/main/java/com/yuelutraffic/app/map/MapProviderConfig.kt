package com.yuelutraffic.app.map

import com.yuelutraffic.app.BuildConfig

enum class MapProviderMode {
    AMAP,
    MOCK,
}

data class MapProviderRuntimeConfig(
    val mode: MapProviderMode,
    val amapApiKey: String,
    val statusMessage: String,
)

fun currentMapProviderRuntimeConfig(): MapProviderRuntimeConfig {
    return mapProviderRuntimeConfig(
        amapApiKey = BuildConfig.AMAP_API_KEY,
        forceMockMap = BuildConfig.FORCE_MOCK_MAP,
    )
}

fun mapProviderRuntimeConfig(amapApiKey: String?, forceMockMap: Boolean): MapProviderRuntimeConfig {
    val normalizedKey = amapApiKey.orEmpty().trim()
    val mode = resolveMapProviderMode(normalizedKey, forceMockMap)
    val message = when {
        forceMockMap -> "已启用强制模拟地图，真实地图 SDK 不会启动。"
        mode == MapProviderMode.AMAP -> "已配置高德地图 SDK Key，地图页优先使用真实地图。"
        else -> "未配置高德地图 SDK Key，当前使用可离线验证的模拟地图。"
    }
    return MapProviderRuntimeConfig(
        mode = mode,
        amapApiKey = normalizedKey,
        statusMessage = message,
    )
}

fun resolveMapProviderMode(amapApiKey: String?, forceMockMap: Boolean): MapProviderMode {
    if (forceMockMap) return MapProviderMode.MOCK
    return if (amapApiKey.orEmpty().trim().isNotEmpty()) MapProviderMode.AMAP else MapProviderMode.MOCK
}
