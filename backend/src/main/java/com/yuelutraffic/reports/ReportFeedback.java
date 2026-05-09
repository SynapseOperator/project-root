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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "report_feedback")
public class ReportFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id")
    private TrafficReport report;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FeedbackType feedbackType;

    @Column(nullable = false)
    private double weightSnapshot;

    @Column(nullable = false)
    private Instant createdAt;

    protected ReportFeedback() {
    }

    public ReportFeedback(TrafficReport report, AppUser user, FeedbackType feedbackType, double weightSnapshot) {
        this.report = report;
        this.user = user;
        this.feedbackType = feedbackType;
        this.weightSnapshot = weightSnapshot;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public FeedbackType getFeedbackType() {
        return feedbackType;
    }

    public double getWeightSnapshot() {
        return weightSnapshot;
    }
}
