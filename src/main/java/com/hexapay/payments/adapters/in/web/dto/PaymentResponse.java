package com.hexapay.payments.adapters.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outbound DTO representing a payment in API responses.
 * Decouples the HTTP representation from the domain model.
 */
public record PaymentResponse(
        UUID id,
        String merchantId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String description,
        String status,
        String externalReference,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime refundedAt
) {}
