package com.hexapay.payments.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Inbound DTO for payment creation requests.
 * Validation annotations enforce contract at the HTTP boundary.
 */
public record CreatePaymentRequest(

        @NotBlank(message = "merchantId is required")
        String merchantId,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "currency is required")
        @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO 4217 code (e.g. BRL, USD)")
        String currency,

        @NotBlank(message = "paymentMethod is required")
        String paymentMethod,

        @NotBlank(message = "description is required")
        @Size(max = 255, message = "description must not exceed 255 characters")
        String description
) {}
