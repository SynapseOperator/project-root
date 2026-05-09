package com.yuelutraffic.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UserRestrictionRequest(
        @NotNull Instant postingBanUntil,
        @NotBlank @Size(max = 500) String reason) {
}
