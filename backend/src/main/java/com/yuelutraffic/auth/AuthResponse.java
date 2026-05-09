package com.yuelutraffic.auth;

import com.yuelutraffic.user.UserSummary;

public record AuthResponse(
        String accessToken,
        UserSummary user,
        boolean newUser,
        String privacyNotice) {
}
