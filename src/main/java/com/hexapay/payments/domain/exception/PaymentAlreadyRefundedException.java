package com.hexapay.payments.domain.exception;

/**
 * Thrown when a refund is requested on a payment that has already been refunded.
 * Maps to HTTP 409 Conflict.
 */
public class PaymentAlreadyRefundedException extends RuntimeException {

    public PaymentAlreadyRefundedException(String message) {
        super(message);
    }
}
