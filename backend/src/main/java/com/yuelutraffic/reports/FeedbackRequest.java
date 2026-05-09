package com.yuelutraffic.reports;

import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(@NotNull FeedbackType feedbackType) {
}
