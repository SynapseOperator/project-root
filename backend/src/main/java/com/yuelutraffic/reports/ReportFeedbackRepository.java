package com.yuelutraffic.reports;

import com.yuelutraffic.user.AppUser;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportFeedbackRepository extends JpaRepository<ReportFeedback, UUID> {

    boolean existsByReportAndUser(TrafficReport report, AppUser user);

    List<ReportFeedback> findByReport(TrafficReport report);
}
