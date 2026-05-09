package com.yuelutraffic.reports;

import com.yuelutraffic.auth.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final AuthService authService;
    private final ReportService reportService;

    public ReportController(AuthService authService, ReportService reportService) {
        this.authService = authService;
        this.reportService = reportService;
    }

    @GetMapping
    public List<ReportResponse> list(
            @RequestParam double minLat,
            @RequestParam double minLng,
            @RequestParam double maxLat,
            @RequestParam double maxLng,
            @RequestParam(required = false) ReportStatus status) {
        return reportService.listReports(minLat, minLng, maxLat, maxLng, status);
    }

    @PostMapping
    public ReportResponse create(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CreateReportRequest request) {
        return reportService.createReport(authService.requireUser(authorizationHeader), request);
    }

    @GetMapping("/{reportId}")
    public ReportResponse detail(@PathVariable UUID reportId) {
        return reportService.getReport(reportId);
    }

    @PostMapping("/{reportId}/feedback")
    public ReportResponse feedback(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID reportId,
            @Valid @RequestBody FeedbackRequest request) {
        return reportService.addFeedback(authService.requireUser(authorizationHeader), reportId, request);
    }
}
