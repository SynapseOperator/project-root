package com.yuelutraffic.reports;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReportExpiryJob {

    private final ReportService reports;

    public ReportExpiryJob(ReportService reports) {
        this.reports = reports;
    }

    @Scheduled(fixedDelayString = "${app.reports.expiry-scan-ms:60000}")
    public void expireOverdueReports() {
        reports.expireOverdueReports();
    }
}
