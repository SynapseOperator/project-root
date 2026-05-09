package com.yuelutraffic.reports;

import com.yuelutraffic.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "traffic_reports")
public class TrafficReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportType type;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(length = 160)
    private String locationLabel;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Instant submittedAt;

    @Column(nullable = false)
    private Instant defaultExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ReportStatus status = ReportStatus.ACTIVE;

    @Column(nullable = false)
    private int initialCredibility;

    @Column(nullable = false)
    private int confidenceScore;

    @ManyToOne(optional = false)
    @JoinColumn(name = "submitter_id")
    private AppUser submitter;

    @Column(length = 300)
    private String hiddenReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected TrafficReport() {
    }

    public TrafficReport(
            ReportType type,
            double latitude,
            double longitude,
            String locationLabel,
            String description,
            Instant submittedAt,
            int initialCredibility,
            AppUser submitter) {
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationLabel = locationLabel;
        this.description = description;
        this.submittedAt = submittedAt;
        this.defaultExpiresAt = submittedAt.plus(type.defaultTtl());
        this.initialCredibility = initialCredibility;
        this.submitter = submitter;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public ReportType getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocationLabel() {
        return locationLabel;
    }

    public String getDescription() {
        return description;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getDefaultExpiresAt() {
        return defaultExpiresAt;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public int getInitialCredibility() {
        return initialCredibility;
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(int confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public AppUser getSubmitter() {
        return submitter;
    }

    public String getHiddenReason() {
        return hiddenReason;
    }

    public void setHiddenReason(String hiddenReason) {
        this.hiddenReason = hiddenReason;
    }
}
