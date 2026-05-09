package com.yuelutraffic.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 128)
    private String studentNumberHash;

    @Column(nullable = false, unique = true, length = 32)
    private String publicCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private int reputationScore = 50;

    @Column(nullable = false)
    private int points = 0;

    @Column(nullable = false, length = 32)
    private String titleCode = "NEWCOMER";

    private Instant postingBanUntil;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected AppUser() {
    }

    public AppUser(String studentNumberHash, String publicCode, UserRole role) {
        this.studentNumberHash = studentNumberHash;
        this.publicCode = publicCode;
        this.role = role;
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

    public String getStudentNumberHash() {
        return studentNumberHash;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public UserRole getRole() {
        return role;
    }

    public int getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(int reputationScore) {
        this.reputationScore = reputationScore;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getTitleCode() {
        return titleCode;
    }

    public void setTitleCode(String titleCode) {
        this.titleCode = titleCode;
    }

    public Instant getPostingBanUntil() {
        return postingBanUntil;
    }

    public void setPostingBanUntil(Instant postingBanUntil) {
        this.postingBanUntil = postingBanUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
