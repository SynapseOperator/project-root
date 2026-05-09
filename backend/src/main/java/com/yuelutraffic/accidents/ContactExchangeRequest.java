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
@Table(name = "contact_exchange_requests")
public class ContactExchangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "accident_id")
    private AccidentPost accident;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requesting_user_id")
    private AppUser requestingUser;

    @ManyToOne
    @JoinColumn(name = "target_user_id")
    private AppUser targetUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ContactExchangeStatus status = ContactExchangeStatus.PENDING;

    private Instant requesterConfirmedAt;

    private Instant targetConfirmedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ContactExchangeRequest() {
    }

    public ContactExchangeRequest(AccidentPost accident, AppUser requestingUser) {
        this.accident = accident;
        this.requestingUser = requestingUser;
        this.requesterConfirmedAt = Instant.now();
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

    public AccidentPost getAccident() {
        return accident;
    }

    public AppUser getRequestingUser() {
        return requestingUser;
    }

    public AppUser getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(AppUser targetUser) {
        this.targetUser = targetUser;
    }

    public ContactExchangeStatus getStatus() {
        return status;
    }

    public void setStatus(ContactExchangeStatus status) {
        this.status = status;
    }

    public Instant getRequesterConfirmedAt() {
        return requesterConfirmedAt;
    }

    public Instant getTargetConfirmedAt() {
        return targetConfirmedAt;
    }

    public void setTargetConfirmedAt(Instant targetConfirmedAt) {
        this.targetConfirmedAt = targetConfirmedAt;
    }
}
