package com.hexapay.payments.domain.model;

import com.hexapay.payments.domain.exception.InvalidPaymentStateException;
import com.hexapay.payments.domain.exception.PaymentAlreadyRefundedException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Payment — Aggregate Root.
 *
 * Contains all business rules for the payment lifecycle.
 * This class has ZERO framework dependencies: pure Java, fully portable,
 * and independently testable without spinning up any infrastructure.
 *
 * Valid state transitions:
 *   PENDING ──► COMPLETED ──► REFUNDED
 *      └──────► FAILED
 */
public class Payment {

    private final UUID id;
    private final String merchantId;
    private final Money amount;
    private final PaymentMethod method;
    private final String description;
    private PaymentStatus status;
    private String externalReference;
    private String failureReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime refundedAt;

    private Payment(
            UUID id,
            String merchantId,
            Money amount,
            PaymentMethod method,
            String description,
            PaymentStatus status,
            LocalDateTime createdAt) {
        this.id = id;
        this.merchantId = merchantId;
        this.amount = amount;
        this.method = method;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    // ─── Factory Methods ──────────────────────────────────────────────────────

    /**
     * Creates a brand new payment in PENDING state.
     * Called by the application layer when a payment request arrives.
     */
    public static Payment create(
            String merchantId,
            Money amount,
            PaymentMethod method,
            String description) {
        Objects.requireNonNull(merchantId, "Merchant ID must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(method, "Payment method must not be null");
        Objects.requireNonNull(description, "Description must not be null");

        if (merchantId.isBlank()) {
            throw new IllegalArgumentException("Merchant ID must not be blank");
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }

        return new Payment(
                UUID.randomUUID(),
                merchantId,
                amount,
                method,
                description,
                PaymentStatus.PENDING,
                LocalDateTime.now()
        );
    }

    /**
     * Reconstitutes an existing payment from its persisted state.
     * Called exclusively by the persistence adapter — never by business logic.
     */
    public static Payment reconstitute(
            UUID id,
            String merchantId,
            Money amount,
            PaymentMethod method,
            String description,
            PaymentStatus status,
            String externalReference,
            String failureReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime refundedAt) {
        Payment payment = new Payment(id, merchantId, amount, method, description, status, createdAt);
        payment.externalReference = externalReference;
        payment.failureReason = failureReason;
        payment.updatedAt = updatedAt;
        payment.refundedAt = refundedAt;
        return payment;
    }

    // ─── Business Methods ─────────────────────────────────────────────────────

    /**
     * Marks this payment as successfully processed by the payment gateway.
     *
     * @param externalReference the gateway's transaction ID
     * @throws InvalidPaymentStateException if the payment is not in PENDING state
     */
    public void complete(String externalReference) {
        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Payment can only be completed from PENDING state. Current state: " + this.status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.externalReference = externalReference;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks this payment as failed.
     *
     * @param reason human-readable failure description
     * @throws InvalidPaymentStateException if the payment is not in PENDING state
     */
    public void fail(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Payment can only fail from PENDING state. Current state: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Refunds this payment, reversing the charge.
     *
     * @throws PaymentAlreadyRefundedException if already refunded
     * @throws InvalidPaymentStateException    if not in a refundable state (COMPLETED)
     */
    public void refund() {
        if (this.status == PaymentStatus.REFUNDED) {
            throw new PaymentAlreadyRefundedException(
                    "Payment " + this.id + " has already been refunded.");
        }
        if (this.status != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException(
                    "Only COMPLETED payments can be refunded. Current state: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns true if this payment can be refunded.
     */
    public boolean isRefundable() {
        return this.status == PaymentStatus.COMPLETED;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public String getMerchantId() { return merchantId; }
    public Money getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public String getDescription() { return description; }
    public PaymentStatus getStatus() { return status; }
    public String getExternalReference() { return externalReference; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getRefundedAt() { return refundedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment payment)) return false;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Payment{id=" + id + ", status=" + status + ", amount=" + amount + "}";
    }
}
