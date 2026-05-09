package com.yuelutraffic.reports;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateReportRequest(
        @NotNull ReportType type,
        @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
        @Size(max = 160) String locationLabel,
        @Size(max = 500) String description,
        Instant submittedAt,
        @Min(0) @Max(100) Integer initialCredibility) {
}
