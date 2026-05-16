package com.hexapay.payments.domain.port.in;

import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Driving port — QueryPayment use case.
 *
 * Provides read access to the payment domain.
 * Query operations are intentionally split from command operations (CQRS-lite).
 */
public interface QueryPaymentUseCase {

    /**
     * Returns a payment by its unique identifier.
     *
     * @throws com.hexapay.payments.domain.exception.PaymentNotFoundException if not found
     */
    Payment findById(UUID id);

    /**
     * Returns all payments with the given status.
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Returns all payments created within the specified date-time range (inclusive).
     *
     * @throws IllegalArgumentException if {@code from} is after {@code to}
     */
    List<Payment> findByDateRange(LocalDateTime from, LocalDateTime to);

    /**
     * Returns all payments associated with the given merchant.
     */
    List<Payment> findByMerchant(String merchantId);
}
