package com.yuelutraffic.admin;

import com.yuelutraffic.reports.ReportResponse;
import java.util.List;

public record ReviewQueueResponse(List<ReportResponse> reportsUnderReview) {
}
