package com.yuelutraffic.accidents;

import com.yuelutraffic.user.UserSummary;
import java.time.Instant;
import java.util.UUID;

public record AccidentResponse(
        UUID id,
        double latitude,
        double longitude,
        String locationLabel,
        Instant occurredAt,
        String description,
        AccidentStatus status,
        UserSummary createdByUser) {

    public static AccidentResponse from(AccidentPost post) {
        return new AccidentResponse(
                post.getId(),
                post.getLatitude(),
                post.getLongitude(),
                post.getLocationLabel(),
                post.getOccurredAt(),
                post.getDescription(),
                post.getStatus(),
                UserSummary.from(post.getCreatedByUser()));
    }
}
