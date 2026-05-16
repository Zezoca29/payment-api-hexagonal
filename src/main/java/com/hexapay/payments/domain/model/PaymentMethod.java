package com.hexapay.payments.domain.model;

/**
 * PaymentMethod — the instrument used to execute a payment.
 */
public enum PaymentMethod {
    /** Credit card payment (Visa, Mastercard, Amex, etc.). */
    CREDIT_CARD,

    /** Debit card payment. */
    DEBIT_CARD,

    /** Brazilian instant payment system (Banco Central). */
    PIX,

    /** Brazilian bank slip (boleto bancário). */
    BOLETO,

    /** Standard bank wire / TED / DOC. */
    BANK_TRANSFER
}
