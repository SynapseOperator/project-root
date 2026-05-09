package com.yuelutraffic.reports;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrafficReportRepository extends JpaRepository<TrafficReport, UUID> {

    List<TrafficReport> findByStatusAndLatitudeBetweenAndLongitudeBetweenOrderBySubmittedAtDesc(
            ReportStatus status,
            double minLatitude,
            double maxLatitude,
            double minLongitude,
            double maxLongitude);

    List<TrafficReport> findByStatusAndDefaultExpiresAtBefore(ReportStatus status, Instant now);

    List<TrafficReport> findByStatusOrderBySubmittedAtDesc(ReportStatus status);
}
