package com.yuelutraffic.app.traffic

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrafficModelsTest {

    @Test
    fun congestionExpiresAfterThirtyMinutesByDefault() {
        val now = Instant.parse("2026-05-09T00:00:00Z")
        val report = createTrafficReport(
            type = TrafficReportType.CONGESTION,
            locationLabel = "Lushan South Road",
            description = "Slow traffic",
            now = now,
        )

        assertEquals(now.plusSeconds(30 * 60), report.defaultExpiresAt)
    }

    @Test
    fun trafficManagementExpiresAfterSixHoursByDefault() {
        val now = Instant.parse("2026-05-09T00:00:00Z")
        val report = createTrafficReport(
            type = TrafficReportType.TRAFFIC_MANAGEMENT,
            locationLabel = "Campus road",
            description = "Public traffic management presence",
            now = now,
        )

        assertEquals(now.plusSeconds(6 * 60 * 60), report.defaultExpiresAt)
    }

    @Test
    fun feedbackAdjustsConfidenceAndCanExpireReport() {
        val report = createTrafficReport(
            type = TrafficReportType.ROAD_CONTROL,
            locationLabel = "Lushan South Road",
            description = "Temporary road control",
        )

        val confirmed = report.applyFeedback(FeedbackChoice.CONFIRM_VALID)
        assertTrue(confirmed.confidenceScore > report.confidenceScore)

        val expired = report
            .applyFeedback(FeedbackChoice.MARK_EXPIRED)
            .applyFeedback(FeedbackChoice.MARK_EXPIRED)
        assertEquals(TrafficReportStatus.EXPIRED, expired.status)
    }
}
