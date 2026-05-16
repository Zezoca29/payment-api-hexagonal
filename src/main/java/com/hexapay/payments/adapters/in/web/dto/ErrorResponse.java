package com.hexapay.payments.adapters.in.web.dto;

import java.time.Instant;

/**
 * Standardized error response body for all API errors.
 */
public record ErrorResponse(
        String code,
        String message,
        Instant timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, Instant.now());
    }
}
