package com.hexapay.payments.domain.exception;

import java.util.UUID;

/**
 * Thrown when a requested payment does not exist in the system.
 * Maps to HTTP 404 Not Found.
 */
public class PaymentNotFoundException extends RuntimeException {

    private final UUID paymentId;

    public PaymentNotFoundException(UUID paymentId) {
        super("Payment not found with ID: " + paymentId);
        this.paymentId = paymentId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }
}
