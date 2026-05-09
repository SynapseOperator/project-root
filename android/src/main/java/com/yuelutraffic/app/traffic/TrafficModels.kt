package com.yuelutraffic.app.traffic

import java.time.Instant
import java.util.UUID

enum class TrafficReportType(
    val label: String,
    val defaultExpiryMinutes: Long,
    val baseConfidence: Int,
) {
    TRAFFIC_MANAGEMENT("交通管理提示", 360, 45),
    CONSTRUCTION("施工占道", 720, 50),
    CONGESTION("道路拥堵", 30, 45),
    ROAD_CONTROL("道路管制", 720, 55),
    ACCIDENT_OR_HAZARD("事故或异常路况", 240, 55),
}

enum class TrafficReportStatus {
    ACTIVE,
    EXPIRED,
    HIDDEN,
    UNDER_REVIEW,
}

enum class FeedbackChoice {
    CONFIRM_VALID,
    MARK_EXPIRED,
}

data class TrafficReportUi(
    val id: String = UUID.randomUUID().toString(),
    val type: TrafficReportType,
    val latitude: Double,
    val longitude: Double,
    val locationLabel: String,
    val description: String,
    val submittedAt: Instant,
    val defaultExpiresAt: Instant,
    val status: TrafficReportStatus,
    val confidenceScore: Int,
    val confirmCount: Int = 0,
    val expiredCount: Int = 0,
    val submitterId: String? = null,
    val submitterPublicCode: String? = null,
)

fun createTrafficReport(
    type: TrafficReportType,
    locationLabel: String,
    description: String,
    now: Instant = Instant.now(),
): TrafficReportUi {
    return TrafficReportUi(
        type = type,
        latitude = 28.1703,
        longitude = 112.9388,
        locationLabel = locationLabel.ifBlank { "中南大学 / 麓山南路" },
        description = description.trim(),
        submittedAt = now,
        defaultExpiresAt = now.plusSeconds(type.defaultExpiryMinutes * 60),
        status = TrafficReportStatus.ACTIVE,
        confidenceScore = type.baseConfidence,
    )
}

fun TrafficReportUi.applyFeedback(choice: FeedbackChoice): TrafficReportUi {
    return when (choice) {
        FeedbackChoice.CONFIRM_VALID -> copy(
            confirmCount = confirmCount + 1,
            confidenceScore = (confidenceScore + 8).coerceAtMost(100),
        )
        FeedbackChoice.MARK_EXPIRED -> {
            val newExpiredCount = expiredCount + 1
            copy(
                expiredCount = newExpiredCount,
                confidenceScore = (confidenceScore - 12).coerceAtLeast(0),
                status = if (newExpiredCount >= 2) TrafficReportStatus.EXPIRED else status,
            )
        }
    }
}

fun sampleTrafficReports(now: Instant = Instant.now()): List<TrafficReportUi> = listOf(
    createTrafficReport(
        type = TrafficReportType.CONGESTION,
        locationLabel = "麓山南路中南大学门口",
        description = "校门口附近车流缓慢，建议耐心通行。",
        now = now,
    ),
    createTrafficReport(
        type = TrafficReportType.CONSTRUCTION,
        locationLabel = "后湖路口往东",
        description = "施工围挡占用一条车道，骑行请注意避让。",
        now = now,
    ).copy(confidenceScore = 58),
)
