package com.yuelutraffic.common;

import java.time.OffsetDateTime;

public record ApiError(
        String error,
        String message,
        int status,
        String path,
        String timestamp) {

    public static ApiError of(String error, String message, int status, String path) {
        return new ApiError(error, message, status, path, OffsetDateTime.now().toString());
    }
}
