package com.hexapay.payments.domain.model;

import java.util.List;

/**
 * Paginated result — a domain value object wrapping a page of payments.
 *
 * Uses only standard Java types so the domain layer remains framework-free.
 * The persistence adapter is responsible for mapping Spring Data's {@code Page<T>}
 * into this type before crossing the port boundary.
 */
public record PaymentPage(
        List<Payment> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {}
