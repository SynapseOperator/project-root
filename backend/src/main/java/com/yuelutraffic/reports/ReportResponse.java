package com.yuelutraffic.reports;

import com.yuelutraffic.user.UserSummary;
import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        ReportType type,
        double latitude,
        double longitude,
        String locationLabel,
        String description,
        Instant submittedAt,
        Instant defaultExpiresAt,
        ReportStatus status,
        int initialCredibility,
        int confidenceScore,
        UserSummary submitter) {

    public static ReportResponse from(TrafficReport report) {
        return new ReportResponse(
                report.getId(),
                report.getType(),
                report.getLatitude(),
                report.getLongitude(),
                report.getLocationLabel(),
                report.getDescription(),
                report.getSubmittedAt(),
                report.getDefaultExpiresAt(),
                report.getStatus(),
                report.getInitialCredibility(),
                report.getConfidenceScore(),
                UserSummary.from(report.getSubmitter()));
    }
}
