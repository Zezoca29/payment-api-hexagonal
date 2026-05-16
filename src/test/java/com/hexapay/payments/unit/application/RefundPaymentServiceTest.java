package com.hexapay.payments.unit.application;

import com.hexapay.payments.application.usecase.RefundPaymentService;
import com.hexapay.payments.domain.exception.InvalidPaymentStateException;
import com.hexapay.payments.domain.exception.PaymentNotFoundException;
import com.hexapay.payments.domain.model.Money;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentMethod;
import com.hexapay.payments.domain.model.PaymentStatus;
import com.hexapay.payments.domain.port.out.LoadPaymentPort;
import com.hexapay.payments.domain.port.out.SavePaymentPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("RefundPaymentService")
@ExtendWith(MockitoExtension.class)
class RefundPaymentServiceTest {

    @Mock
    private LoadPaymentPort loadPaymentPort;

    @Mock
    private SavePaymentPort savePaymentPort;

    @InjectMocks
    private RefundPaymentService refundPaymentService;

    @Test
    @DisplayName("should refund a COMPLETED payment and return REFUNDED state")
    void shouldRefundSuccessfully() {
        // Given
        Payment payment = completedPayment();
        when(loadPaymentPort.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(savePaymentPort.save(payment)).thenReturn(payment);

        // When
        Payment result = refundPaymentService.refundPayment(payment.getId());

        // Then
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(result.getRefundedAt()).isNotNull();
        verify(savePaymentPort).save(payment);
    }

    @Test
    @DisplayName("should throw PaymentNotFoundException when payment does not exist")
    void shouldThrowWhenNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(loadPaymentPort.findById(unknownId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(PaymentNotFoundException.class)
                .isThrownBy(() -> refundPaymentService.refundPayment(unknownId));

        verifyNoInteractions(savePaymentPort);
    }

    @Test
    @DisplayName("should propagate InvalidPaymentStateException for PENDING payment")
    void shouldThrowForPendingPayment() {
        Payment payment = pendingPayment();
        when(loadPaymentPort.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThatExceptionOfType(InvalidPaymentStateException.class)
                .isThrownBy(() -> refundPaymentService.refundPayment(payment.getId()));

        verify(savePaymentPort, never()).save(any());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Payment pendingPayment() {
        return Payment.create("merchant-001",
                new Money(BigDecimal.valueOf(100), "BRL"), PaymentMethod.PIX, "Test");
    }

    private Payment completedPayment() {
        Payment payment = pendingPayment();
        payment.complete("ext-ref-001");
        return payment;
    }
}
