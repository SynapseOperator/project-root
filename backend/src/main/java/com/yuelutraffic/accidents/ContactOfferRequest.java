package com.yuelutraffic.accidents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContactOfferRequest(
        @NotNull ContactType contactType,
        @NotBlank @Size(max = 120) String contactValue) {
}
