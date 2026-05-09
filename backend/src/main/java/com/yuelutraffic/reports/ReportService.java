package com.yuelutraffic.reports;

import com.yuelutraffic.common.ApiException;
import com.yuelutraffic.location.LocationService;
import com.yuelutraffic.reputation.ReputationEvent;
import com.yuelutraffic.reputation.ReputationEventRepository;
import com.yuelutraffic.user.AppUser;
import com.yuelutraffic.user.AppUserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final TrafficReportRepository reports;
    private final ReportFeedbackRepository feedback;
    private final ReputationEventRepository reputationEvents;
    private final AppUserRepository users;
    private final LocationService locationService;

    public ReportService(
            TrafficReportRepository reports,
            ReportFeedbackRepository feedback,
            ReputationEventRepository reputationEvents,
            AppUserRepository users,
            LocationService locationService) {
        this.reports = reports;
        this.feedback = feedback;
        this.reputationEvents = reputationEvents;
        this.users = users;
        this.locationService = locationService;
    }

    @Transactional
    public ReportResponse createReport(AppUser submitter, CreateReportRequest request) {
        Instant now = Instant.now();
        if (submitter.isPostingBanned(now)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Posting is temporarily restricted for this user");
        }
        locationService.requireInsidePilotArea(request.latitude(), request.longitude());
        Instant submittedAt = request.submittedAt() == null ? now : request.submittedAt();
        int initialCredibility = request.initialCredibility() == null ? 50 : request.initialCredibility();
        TrafficReport report = new TrafficReport(
                request.type(),
                request.latitude(),
                request.longitude(),
                emptyToNull(request.locationLabel()),
                emptyToNull(request.description()),
                submittedAt,
                initialCredibility,
                submitter);
        report.setConfidenceScore(calculateConfidence(report, List.of(), now));
        TrafficReport saved = reports.save(report);
        applyReputation(submitter, saved.getId(), 2, 1, "REPORT_SUBMITTED");
        return ReportResponse.from(saved);
    }

    @Transactional
    public List<ReportResponse> listReports(double minLat, double minLng, double maxLat, double maxLng, ReportStatus status) {
        expireOverdueReports();
        ReportStatus requestedStatus = status == null ? ReportStatus.ACTIVE : status;
        double lowerLat = Math.min(minLat, maxLat);
        double upperLat = Math.max(minLat, maxLat);
        double lowerLng = Math.min(minLng, maxLng);
        double upperLng = Math.max(minLng, maxLng);
        return reports.findByStatusAndLatitudeBetweenAndLongitudeBetweenOrderBySubmittedAtDesc(
                        requestedStatus, lowerLat, upperLat, lowerLng, upperLng)
                .stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Transactional
    public ReportResponse getReport(UUID reportId) {
        expireOverdueReports();
        return ReportResponse.from(requireReport(reportId));
    }

    @Transactional
    public ReportResponse addFeedback(AppUser user, UUID reportId, FeedbackRequest request) {
        TrafficReport report = requireReport(reportId);
        if (feedback.existsByReportAndUser(report, user)) {
            throw new ApiException(HttpStatus.CONFLICT, "User has already evaluated this report");
        }
        double weight = feedbackWeight(user);
        feedback.save(new ReportFeedback(report, user, request.feedbackType(), weight));
        List<ReportFeedback> reportFeedback = feedback.findByReport(report);
        applyFeedbackReputation(user, report.getSubmitter(), report.getId(), request.feedbackType());
        updateReportFromFeedback(report, reportFeedback, Instant.now());
        return ReportResponse.from(report);
    }

    @Transactional
    public int expireOverdueReports() {
        Instant now = Instant.now();
        List<TrafficReport> expired = reports.findByStatusAndDefaultExpiresAtBefore(ReportStatus.ACTIVE, now);
        for (TrafficReport report : expired) {
            report.setStatus(ReportStatus.EXPIRED);
            report.setConfidenceScore(Math.min(report.getConfidenceScore(), 25));
        }
        return expired.size();
    }

    private TrafficReport requireReport(UUID reportId) {
        return reports.findById(reportId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Traffic report not found"));
    }

    private void updateReportFromFeedback(TrafficReport report, List<ReportFeedback> reportFeedback, Instant now) {
        FeedbackTotals totals = FeedbackTotals.from(reportFeedback);
        if (totals.expired >= 2.0) {
            report.setStatus(ReportStatus.EXPIRED);
        }
        if (totals.falseOrMalicious() >= 2.0) {
            report.setStatus(ReportStatus.UNDER_REVIEW);
        }
        if (totals.malicious >= 3.0) {
            report.getSubmitter().setPostingBanUntil(now.plus(24, ChronoUnit.HOURS));
        }
        report.setConfidenceScore(calculateConfidence(report, reportFeedback, now));
    }

    private int calculateConfidence(TrafficReport report, List<ReportFeedback> reportFeedback, Instant now) {
        FeedbackTotals totals = FeedbackTotals.from(reportFeedback);
        int submitterFactor = Math.max(-15, Math.min(15, (report.getSubmitter().getReputationScore() - 50) / 2));
        int timeDecay = now.isAfter(report.getDefaultExpiresAt()) ? 30 : 0;
        int score = ((report.getType().baseConfidence() + report.getInitialCredibility()) / 2)
                + submitterFactor
                + (int) Math.round(totals.confirm * 8)
                - (int) Math.round(totals.expired * 12)
                - (int) Math.round(totals.falseReports * 20)
                - (int) Math.round(totals.malicious * 25)
                - timeDecay;
        return Math.max(0, Math.min(100, score));
    }

    private double feedbackWeight(AppUser user) {
        return 1.0 + Math.max(0.0, Math.min(2.0, user.getReputationScore() / 100.0));
    }

    private void applyFeedbackReputation(AppUser actor, AppUser submitter, UUID sourceId, FeedbackType type) {
        switch (type) {
            case CONFIRM_VALID -> {
                applyReputation(actor, sourceId, 1, 1, "CONFIRM_VALID");
                applyReputation(submitter, sourceId, 2, 1, "REPORT_CONFIRMED");
            }
            case MARK_EXPIRED -> applyReputation(actor, sourceId, 1, 0, "MARK_EXPIRED");
            case REPORT_FALSE -> applyReputation(submitter, sourceId, 0, -4, "REPORT_MARKED_FALSE");
            case REPORT_MALICIOUS -> applyReputation(submitter, sourceId, 0, -8, "REPORT_MARKED_MALICIOUS");
        }
    }

    private void applyReputation(AppUser user, UUID sourceId, int pointsDelta, int reputationDelta, String reasonCode) {
        user.addPoints(pointsDelta);
        user.addReputation(reputationDelta);
        users.save(user);
        reputationEvents.save(new ReputationEvent(user, "TRAFFIC_REPORT", sourceId, pointsDelta, reputationDelta, reasonCode));
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record FeedbackTotals(double confirm, double expired, double falseReports, double malicious) {

        static FeedbackTotals from(List<ReportFeedback> feedback) {
            EnumMap<FeedbackType, Double> totals = new EnumMap<>(FeedbackType.class);
            for (ReportFeedback item : feedback) {
                totals.merge(item.getFeedbackType(), item.getWeightSnapshot(), Double::sum);
            }
            double falseReports = totals.getOrDefault(FeedbackType.REPORT_FALSE, 0.0);
            double malicious = totals.getOrDefault(FeedbackType.REPORT_MALICIOUS, 0.0);
            return new FeedbackTotals(
                    totals.getOrDefault(FeedbackType.CONFIRM_VALID, 0.0),
                    totals.getOrDefault(FeedbackType.MARK_EXPIRED, 0.0),
                    falseReports,
                    malicious);
        }

        double falseOrMalicious() {
            return falseReports + malicious;
        }
    }
}
