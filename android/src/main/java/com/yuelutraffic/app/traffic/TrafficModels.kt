package com.yuelutraffic.app.traffic

import java.time.Instant
import java.util.UUID

enum class TrafficReportType(
    val label: String,
    val defaultExpiryMinutes: Long,
    val baseConfidence: Int,
) {
    TRAFFIC_MANAGEMENT("Traffic management presence", 360, 45),
    CONSTRUCTION("Construction", 720, 50),
    CONGESTION("Congestion", 30, 45),
    ROAD_CONTROL("Road closure / traffic control", 720, 55),
    ACCIDENT_OR_HAZARD("Accident or abnormal road condition", 240, 55),
}

enum class TrafficReportStatus {
    ACTIVE,
    EXPIRED,
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
        locationLabel = locationLabel.ifBlank { "Central South University / Lushan South Road" },
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
        locationLabel = "Lushan South Road northbound",
        description = "Slow traffic near the campus gate.",
        now = now,
    ),
    createTrafficReport(
        type = TrafficReportType.CONSTRUCTION,
        locationLabel = "Near Central South University",
        description = "Road work occupies one lane.",
        now = now,
    ).copy(confidenceScore = 58),
)
