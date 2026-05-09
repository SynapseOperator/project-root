package com.yuelutraffic.accidents;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateAccidentRequest(
        @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
        @Size(max = 160) String locationLabel,
        Instant occurredAt,
        @NotBlank @Size(max = 500) String description) {
}
