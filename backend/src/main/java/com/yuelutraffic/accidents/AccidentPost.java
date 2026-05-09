package com.yuelutraffic.accidents;

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
@Table(name = "accident_posts")
public class AccidentPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(length = 160)
    private String locationLabel;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_user_id")
    private AppUser createdByUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AccidentStatus status = AccidentStatus.OPEN;

    @Column(length = 300)
    private String hiddenReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected AccidentPost() {
    }

    public AccidentPost(double latitude, double longitude, String locationLabel, Instant occurredAt, String description, AppUser createdByUser) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationLabel = locationLabel;
        this.occurredAt = occurredAt;
        this.description = description;
        this.createdByUser = createdByUser;
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocationLabel() {
        return locationLabel;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getDescription() {
        return description;
    }

    public AppUser getCreatedByUser() {
        return createdByUser;
    }

    public AccidentStatus getStatus() {
        return status;
    }

    public void setStatus(AccidentStatus status) {
        this.status = status;
    }

    public String getHiddenReason() {
        return hiddenReason;
    }

    public void setHiddenReason(String hiddenReason) {
        this.hiddenReason = hiddenReason;
    }
}
