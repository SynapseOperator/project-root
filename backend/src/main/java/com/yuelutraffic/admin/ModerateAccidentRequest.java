package com.yuelutraffic.admin;

import com.yuelutraffic.accidents.AccidentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModerateAccidentRequest(
        @NotNull AccidentStatus status,
        @NotBlank @Size(max = 500) String reason) {
}
