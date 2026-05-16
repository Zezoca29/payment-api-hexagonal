package com.hexapay.payments.unit.domain;

import com.hexapay.payments.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money value object")
class MoneyTest {

    @Test
    @DisplayName("should create valid Money object")
    void shouldCreateValidMoney() {
        Money money = new Money(BigDecimal.valueOf(100.00), "BRL");
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(money.currency()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("should reject zero amount")
    void shouldRejectZeroAmount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Money(BigDecimal.ZERO, "BRL"))
                .withMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("should reject negative amount")
    void shouldRejectNegativeAmount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Money(BigDecimal.valueOf(-10), "BRL"))
                .withMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("should reject null amount")
    void shouldRejectNullAmount() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Money(null, "BRL"));
    }

    @Test
    @DisplayName("should reject invalid currency code")
    void shouldRejectInvalidCurrency() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Money(BigDecimal.valueOf(100), "BR"))
                .withMessageContaining("ISO 4217");
    }

    @Test
    @DisplayName("should reject null currency")
    void shouldRejectNullCurrency() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Money(BigDecimal.valueOf(100), null));
    }

    @Test
    @DisplayName("should add two Money objects with the same currency")
    void shouldAddMoneyWithSameCurrency() {
        Money a = new Money(BigDecimal.valueOf(50), "BRL");
        Money b = new Money(BigDecimal.valueOf(30), "BRL");

        Money result = a.add(b);

        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(80));
        assertThat(result.currency()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("should throw when adding Money with different currencies")
    void shouldThrowOnCurrencyMismatch() {
        Money brl = new Money(BigDecimal.valueOf(100), "BRL");
        Money usd = new Money(BigDecimal.valueOf(100), "USD");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> brl.add(usd))
                .withMessageContaining("different currencies");
    }

    @Test
    @DisplayName("isGreaterThan() should return correct comparison")
    void shouldCompareAmounts() {
        Money big = new Money(BigDecimal.valueOf(200), "BRL");
        Money small = new Money(BigDecimal.valueOf(100), "BRL");

        assertThat(big.isGreaterThan(small)).isTrue();
        assertThat(small.isGreaterThan(big)).isFalse();
    }

    @Test
    @DisplayName("two Money objects with same amount and currency should be equal")
    void shouldBeEqualWhenSame() {
        Money a = new Money(BigDecimal.valueOf(100.00), "BRL");
        Money b = new Money(BigDecimal.valueOf(100.00), "BRL");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
