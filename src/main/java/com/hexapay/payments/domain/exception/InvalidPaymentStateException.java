package com.hexapay.payments.domain.exception;

/**
 * Thrown when an invalid state transition is attempted on a payment.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class InvalidPaymentStateException extends RuntimeException {

    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
