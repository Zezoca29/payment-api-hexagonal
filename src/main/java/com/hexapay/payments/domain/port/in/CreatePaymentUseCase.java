package com.hexapay.payments.domain.port.in;

import com.hexapay.payments.domain.model.Payment;

import java.math.BigDecimal;

/**
 * Driving port — CreatePayment use case.
 *
 * This interface is the contract between the application core and anything
 * that wants to create a payment (REST controller, message consumer, CLI, etc.).
 * It lives in the domain because it IS part of the application's core contract.
 */
public interface CreatePaymentUseCase {

    /**
     * Creates and persists a new payment in PENDING state.
     *
     * @param command the validated input data
     * @return the newly created payment
     */
    Payment createPayment(CreatePaymentCommand command);

    /**
     * Immutable command object carrying all data needed to create a payment.
     * Using a record ensures it is always in a valid, fully-initialized state.
     */
    record CreatePaymentCommand(
            String merchantId,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String description
    ) {}
}
