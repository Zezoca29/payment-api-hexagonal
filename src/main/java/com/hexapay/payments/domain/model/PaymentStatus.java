package com.hexapay.payments.domain.model;

/**
 * PaymentStatus — lifecycle states for a payment.
 *
 * Valid transitions:
 *   PENDING ──► COMPLETED ──► REFUNDED
 *      └──────► FAILED
 */
public enum PaymentStatus {
    /** Payment created and awaiting processing by the gateway. */
    PENDING,

    /** Payment was successfully processed. Eligible for refund. */
    COMPLETED,

    /** Payment processing failed (e.g., insufficient funds, declined). */
    FAILED,

    /** Payment was refunded. Terminal state. */
    REFUNDED
}
