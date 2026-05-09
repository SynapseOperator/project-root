package com.yuelutraffic.reports;

import java.time.Duration;

public enum ReportType {
    TRAFFIC_MANAGEMENT(Duration.ofHours(6), 45),
    CONSTRUCTION(Duration.ofHours(12), 50),
    CONGESTION(Duration.ofMinutes(30), 45),
    ROAD_CONTROL(Duration.ofHours(12), 55),
    ACCIDENT_OR_HAZARD(Duration.ofHours(4), 55);

    private final Duration defaultTtl;
    private final int baseConfidence;

    ReportType(Duration defaultTtl, int baseConfidence) {
        this.defaultTtl = defaultTtl;
        this.baseConfidence = baseConfidence;
    }

    public Duration defaultTtl() {
        return defaultTtl;
    }

    public int baseConfidence() {
        return baseConfidence;
    }
}
