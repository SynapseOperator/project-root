package com.yuelutraffic.admin;

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
@Table(name = "admin_actions")
public class AdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "admin_user_id")
    private AppUser adminUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TargetType targetType;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false, length = 64)
    private String actionType;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private Instant createdAt;

    protected AdminAction() {
    }

    public AdminAction(AppUser adminUser, TargetType targetType, UUID targetId, String actionType, String reason) {
        this.adminUser = adminUser;
        this.targetType = targetType;
        this.targetId = targetId;
        this.actionType = actionType;
        this.reason = reason;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
