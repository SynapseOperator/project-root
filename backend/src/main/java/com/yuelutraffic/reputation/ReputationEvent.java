package com.yuelutraffic.reputation;

import com.yuelutraffic.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "reputation_events")
public class ReputationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false, length = 40)
    private String sourceType;

    @Column(nullable = false)
    private UUID sourceId;

    @Column(nullable = false)
    private int pointsDelta;

    @Column(nullable = false)
    private int reputationDelta;

    @Column(nullable = false, length = 64)
    private String reasonCode;

    @Column(nullable = false)
    private Instant createdAt;

    protected ReputationEvent() {
    }

    public ReputationEvent(AppUser user, String sourceType, UUID sourceId, int pointsDelta, int reputationDelta, String reasonCode) {
        this.user = user;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.pointsDelta = pointsDelta;
        this.reputationDelta = reputationDelta;
        this.reasonCode = reasonCode;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
