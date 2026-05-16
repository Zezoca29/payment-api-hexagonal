package com.hexapay.payments.unit.domain;

import com.hexapay.payments.domain.exception.InvalidPaymentStateException;
import com.hexapay.payments.domain.exception.PaymentAlreadyRefundedException;
import com.hexapay.payments.domain.model.Money;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentMethod;
import com.hexapay.payments.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payment domain model")
class PaymentTest {

    private static final Money VALID_AMOUNT = new Money(BigDecimal.valueOf(150.00), "BRL");
    private static final String MERCHANT_ID = "merchant-001";
    private static final String DESCRIPTION = "Order #42";

    // ─── Factory ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create payment with PENDING status and generated UUID")
        void shouldCreateWithPendingStatus() {
            Payment payment = Payment.create(MERCHANT_ID, VALID_AMOUNT, PaymentMethod.PIX, DESCRIPTION);

            assertThat(payment.getId()).isNotNull();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getMerchantId()).isEqualTo(MERCHANT_ID);
            assertThat(payment.getAmount()).isEqualTo(VALID_AMOUNT);
            assertThat(payment.getMethod()).isEqualTo(PaymentMethod.PIX);
            assertThat(payment.getCreatedAt()).isNotNull();
            assertThat(payment.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should reject null merchantId")
        void shouldRejectNullMerchantId() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Payment.create(null, VALID_AMOUNT, PaymentMethod.PIX, DESCRIPTION));
        }

        @Test
        @DisplayName("should reject blank merchantId")
        void shouldRejectBlankMerchantId() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Payment.create("  ", VALID_AMOUNT, PaymentMethod.PIX, DESCRIPTION));
        }

        @Test
        @DisplayName("should reject null amount")
        void shouldRejectNullAmount() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Payment.create(MERCHANT_ID, null, PaymentMethod.PIX, DESCRIPTION));
        }
    }

    // ─── complete() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("complete()")
    class Complete {

        @Test
        @DisplayName("should transition PENDING → COMPLETED with external reference")
        void shouldCompleteSuccessfully() {
            Payment payment = pendingPayment();

            payment.complete("ext-ref-xyz-123");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.getExternalReference()).isEqualTo("ext-ref-xyz-123");
            assertThat(payment.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw when completing a non-PENDING payment")
        void shouldThrowOnInvalidTransition() {
            Payment payment = completedPayment();

            assertThatExceptionOfType(InvalidPaymentStateException.class)
                    .isThrownBy(() -> payment.complete("ext-ref-002"))
                    .withMessageContaining("PENDING");
        }
    }

    // ─── fail() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("fail()")
    class Fail {

        @Test
        @DisplayName("should transition PENDING → FAILED with failure reason")
        void shouldFailSuccessfully() {
            Payment payment = pendingPayment();

            payment.fail("Insufficient funds");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureReason()).isEqualTo("Insufficient funds");
        }

        @Test
        @DisplayName("should throw when failing a COMPLETED payment")
        void shouldThrowOnInvalidTransition() {
            Payment payment = completedPayment();

            assertThatExceptionOfType(InvalidPaymentStateException.class)
                    .isThrownBy(() -> payment.fail("some reason"));
        }
    }

    // ─── refund() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("refund()")
    class Refund {

        @Test
        @DisplayName("should transition COMPLETED → REFUNDED")
        void shouldRefundSuccessfully() {
            Payment payment = completedPayment();

            payment.refund();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getRefundedAt()).isNotNull();
            assertThat(payment.isRefundable()).isFalse();
        }

        @Test
        @DisplayName("should throw PaymentAlreadyRefundedException when refunding twice")
        void shouldThrowWhenAlreadyRefunded() {
            Payment payment = completedPayment();
            payment.refund();

            assertThatExceptionOfType(PaymentAlreadyRefundedException.class)
                    .isThrownBy(payment::refund)
                    .withMessageContaining("already been refunded");
        }

        @Test
        @DisplayName("should throw InvalidPaymentStateException when refunding a PENDING payment")
        void shouldThrowWhenRefundingPending() {
            Payment payment = pendingPayment();

            assertThatExceptionOfType(InvalidPaymentStateException.class)
                    .isThrownBy(payment::refund)
                    .withMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("should throw InvalidPaymentStateException when refunding a FAILED payment")
        void shouldThrowWhenRefundingFailed() {
            Payment payment = pendingPayment();
            payment.fail("Declined");

            assertThatExceptionOfType(InvalidPaymentStateException.class)
                    .isThrownBy(payment::refund);
        }
    }

    // ─── isRefundable() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("isRefundable() returns true only for COMPLETED payments")
    void shouldBeRefundableOnlyWhenCompleted() {
        assertThat(pendingPayment().isRefundable()).isFalse();
        assertThat(completedPayment().isRefundable()).isTrue();

        Payment failed = pendingPayment();
        failed.fail("Declined");
        assertThat(failed.isRefundable()).isFalse();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Payment pendingPayment() {
        return Payment.create(MERCHANT_ID, VALID_AMOUNT, PaymentMethod.PIX, DESCRIPTION);
    }

    private Payment completedPayment() {
        Payment payment = pendingPayment();
        payment.complete("ext-ref-001");
        return payment;
    }
}
