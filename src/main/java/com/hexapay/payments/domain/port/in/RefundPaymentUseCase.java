package com.hexapay.payments.domain.port.in;

import com.hexapay.payments.domain.model.Payment;

import java.util.UUID;

/**
 * Driving port — RefundPayment use case.
 *
 * Defines the contract for refunding a previously completed payment.
 * Only COMPLETED payments may be refunded — enforcement lives in the domain model.
 */
public interface RefundPaymentUseCase {

    /**
     * Refunds the specified payment.
     *
     * @param paymentId the unique identifier of the payment to refund
     * @return the updated payment in REFUNDED state
     * @throws com.hexapay.payments.domain.exception.PaymentNotFoundException       if not found
     * @throws com.hexapay.payments.domain.exception.PaymentAlreadyRefundedException if already refunded
     * @throws com.hexapay.payments.domain.exception.InvalidPaymentStateException    if not refundable
     */
    Payment refundPayment(UUID paymentId);
}
