package com.yuelutraffic.admin;

import com.yuelutraffic.accidents.AccidentPostRepository;
import com.yuelutraffic.common.ApiException;
import com.yuelutraffic.reports.ReportResponse;
import com.yuelutraffic.reports.ReportStatus;
import com.yuelutraffic.reports.TrafficReportRepository;
import com.yuelutraffic.user.AppUser;
import com.yuelutraffic.user.AppUserRepository;
import com.yuelutraffic.user.UserSummary;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final TrafficReportRepository reports;
    private final AccidentPostRepository accidents;
    private final AppUserRepository users;
    private final AdminActionRepository actions;

    public AdminService(TrafficReportRepository reports, AccidentPostRepository accidents, AppUserRepository users, AdminActionRepository actions) {
        this.reports = reports;
        this.accidents = accidents;
        this.users = users;
        this.actions = actions;
    }

    @Transactional(readOnly = true)
    public ReviewQueueResponse reviewQueue() {
        return new ReviewQueueResponse(reports.findByStatusOrderBySubmittedAtDesc(ReportStatus.UNDER_REVIEW).stream()
                .map(ReportResponse::from)
                .toList());
    }

    @Transactional
    public ReportResponse moderateReport(AppUser admin, UUID reportId, ModerateReportRequest request) {
        var report = reports.findById(reportId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Traffic report not found"));
        report.setStatus(request.status());
        report.setHiddenReason(request.reason());
        actions.save(new AdminAction(admin, TargetType.TRAFFIC_REPORT, reportId, "REPORT_" + request.status(), request.reason()));
        return ReportResponse.from(report);
    }

    @Transactional
    public Object moderateAccident(AppUser admin, UUID accidentId, ModerateAccidentRequest request) {
        var accident = accidents.findById(accidentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Accident post not found"));
        accident.setStatus(request.status());
        accident.setHiddenReason(request.reason());
        actions.save(new AdminAction(admin, TargetType.ACCIDENT_POST, accidentId, "ACCIDENT_" + request.status(), request.reason()));
        return com.yuelutraffic.accidents.AccidentResponse.from(accident);
    }

    @Transactional
    public UserSummary restrictUser(AppUser admin, UUID userId, UserRestrictionRequest request) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        user.setPostingBanUntil(request.postingBanUntil());
        actions.save(new AdminAction(admin, TargetType.USER, userId, "POSTING_BAN", request.reason()));
        return UserSummary.from(user);
    }
}
