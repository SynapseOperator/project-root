package com.yuelutraffic.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AuthRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_-]{4,32}$", message = "must be 4-32 letters, numbers, underscores, or hyphens")
        String studentNumber,

        @AssertTrue(message = "must be accepted before using the app")
        boolean privacyAcknowledged) {
}
