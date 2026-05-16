package com.hexapay.payments.domain.port.in;

import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentPage;
import com.hexapay.payments.domain.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Driving port — QueryPayment use case.
 *
 * Provides read access to the payment domain.
 * Query operations are intentionally split from command operations (CQRS-lite).
 * All list operations are paginated to prevent unbounded result sets.
 */
public interface QueryPaymentUseCase {

    /**
     * Returns a payment by its unique identifier.
     *
     * @throws com.hexapay.payments.domain.exception.PaymentNotFoundException if not found
     */
    Payment findById(UUID id);

    /**
     * Returns a page of payments with the given status, ordered by creation date descending.
     */
    PaymentPage findByStatus(PaymentStatus status, int page, int size);

    /**
     * Returns a page of payments created within the specified date-time range (inclusive).
     *
     * @throws IllegalArgumentException if {@code from} is after {@code to}
     */
    PaymentPage findByDateRange(LocalDateTime from, LocalDateTime to, int page, int size);

    /**
     * Returns a page of payments associated with the given merchant.
     */
    PaymentPage findByMerchant(String merchantId, int page, int size);
}
