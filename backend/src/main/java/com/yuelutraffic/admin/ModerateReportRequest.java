package com.yuelutraffic.admin;

import com.yuelutraffic.reports.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModerateReportRequest(
        @NotNull ReportStatus status,
        @NotBlank @Size(max = 500) String reason) {
}
