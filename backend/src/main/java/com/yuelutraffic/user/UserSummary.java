package com.yuelutraffic.user;

import java.time.Instant;
import java.util.UUID;

public record UserSummary(
        UUID id,
        String publicCode,
        UserRole role,
        int reputationScore,
        int points,
        String titleCode,
        Instant postingBanUntil) {

    public static UserSummary from(AppUser user) {
        return new UserSummary(
                user.getId(),
                user.getPublicCode(),
                user.getRole(),
                user.getReputationScore(),
                user.getPoints(),
                user.getTitleCode(),
                user.getPostingBanUntil());
    }
}
