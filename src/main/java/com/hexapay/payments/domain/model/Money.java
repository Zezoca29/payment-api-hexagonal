package com.hexapay.payments.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Money — Value Object.
 *
 * Encapsulates an amount and its currency, enforcing invariants at construction time.
 * Being a record, Money is immutable by design — two Money objects with the same
 * amount and currency are considered equal.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero, got: " + amount);
        }
        if (currency.isBlank() || currency.length() != 3) {
            throw new IllegalArgumentException("Currency must be a valid 3-letter ISO 4217 code, got: " + currency);
        }
    }

    /**
     * Returns a new Money representing the sum of this and another Money.
     *
     * @throws IllegalArgumentException if currencies differ
     */
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Returns true if this amount is greater than the other.
     *
     * @throws IllegalArgumentException if currencies differ
     */
    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equalsIgnoreCase(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot operate on different currencies: " + this.currency + " vs " + other.currency);
        }
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }
}
