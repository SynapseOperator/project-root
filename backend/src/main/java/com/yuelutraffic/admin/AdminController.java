package com.yuelutraffic.admin;

import com.yuelutraffic.auth.AuthService;
import com.yuelutraffic.reports.ReportResponse;
import com.yuelutraffic.user.UserSummary;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AuthService authService;
    private final AdminService adminService;

    public AdminController(AuthService authService, AdminService adminService) {
        this.authService = authService;
        this.adminService = adminService;
    }

    @GetMapping("/review-queue")
    public ReviewQueueResponse reviewQueue(@RequestHeader("Authorization") String authorizationHeader) {
        authService.requireAdmin(authorizationHeader);
        return adminService.reviewQueue();
    }

    @PostMapping("/reports/{reportId}/moderate")
    public ReportResponse moderateReport(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID reportId,
            @Valid @RequestBody ModerateReportRequest request) {
        return adminService.moderateReport(authService.requireAdmin(authorizationHeader), reportId, request);
    }

    @PostMapping("/accidents/{accidentId}/moderate")
    public Object moderateAccident(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID accidentId,
            @Valid @RequestBody ModerateAccidentRequest request) {
        return adminService.moderateAccident(authService.requireAdmin(authorizationHeader), accidentId, request);
    }

    @PostMapping("/users/{userId}/restrictions")
    public UserSummary restrictUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID userId,
            @Valid @RequestBody UserRestrictionRequest request) {
        return adminService.restrictUser(authService.requireAdmin(authorizationHeader), userId, request);
    }
}
