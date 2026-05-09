package com.yuelutraffic.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class ReportTypeTest {

    @Test
    void p0DefaultExpirationDurationsAreStable() {
        assertEquals(Duration.ofMinutes(30), ReportType.CONGESTION.defaultTtl());
        assertEquals(Duration.ofHours(6), ReportType.TRAFFIC_MANAGEMENT.defaultTtl());
        assertEquals(Duration.ofHours(12), ReportType.CONSTRUCTION.defaultTtl());
        assertEquals(Duration.ofHours(12), ReportType.ROAD_CONTROL.defaultTtl());
        assertEquals(Duration.ofHours(4), ReportType.ACCIDENT_OR_HAZARD.defaultTtl());
    }
}
