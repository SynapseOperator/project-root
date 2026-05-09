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
            locationLabel = "麓山南路",
            description = "车流缓慢",
            now = now,
        )

        assertEquals(now.plusSeconds(30 * 60), report.defaultExpiresAt)
    }

    @Test
    fun trafficManagementExpiresAfterSixHoursByDefault() {
        val now = Instant.parse("2026-05-09T00:00:00Z")
        val report = createTrafficReport(
            type = TrafficReportType.TRAFFIC_MANAGEMENT,
            locationLabel = "校园路口",
            description = "公开交通管理提示",
            now = now,
        )

        assertEquals(now.plusSeconds(6 * 60 * 60), report.defaultExpiresAt)
    }

    @Test
    fun feedbackAdjustsConfidenceAndCanExpireReport() {
        val report = createTrafficReport(
            type = TrafficReportType.ROAD_CONTROL,
            locationLabel = "麓山南路",
            description = "临时道路管制",
        )

        val confirmed = report.applyFeedback(FeedbackChoice.CONFIRM_VALID)
        assertTrue(confirmed.confidenceScore > report.confidenceScore)

        val expired = report
            .applyFeedback(FeedbackChoice.MARK_EXPIRED)
            .applyFeedback(FeedbackChoice.MARK_EXPIRED)
        assertEquals(TrafficReportStatus.EXPIRED, expired.status)
    }

    @Test
    fun reportTypeLabelsAreSimplifiedChinese() {
        assertEquals("道路拥堵", TrafficReportType.CONGESTION.label)
        assertEquals("事故或异常路况", TrafficReportType.ACCIDENT_OR_HAZARD.label)
    }
}
