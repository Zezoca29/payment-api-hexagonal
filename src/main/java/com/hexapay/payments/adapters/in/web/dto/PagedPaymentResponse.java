package com.hexapay.payments.adapters.in.web.dto;

import java.util.List;

/**
 * Outbound DTO for paginated list responses.
 * Wraps the payment items with standard pagination metadata.
 */
public record PagedPaymentResponse(
        List<PaymentResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {}
