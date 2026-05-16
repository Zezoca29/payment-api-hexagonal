package com.hexapay.payments.unit.application;

import com.hexapay.payments.application.usecase.QueryPaymentService;
import com.hexapay.payments.domain.exception.PaymentNotFoundException;
import com.hexapay.payments.domain.model.Money;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentMethod;
import com.hexapay.payments.domain.model.PaymentPage;
import com.hexapay.payments.domain.model.PaymentStatus;
import com.hexapay.payments.domain.port.out.LoadPaymentPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("QueryPaymentService")
@ExtendWith(MockitoExtension.class)
class QueryPaymentServiceTest {

    @Mock
    private LoadPaymentPort loadPaymentPort;

    @InjectMocks
    private QueryPaymentService queryPaymentService;

    @Test
    @DisplayName("findById() should return payment when found")
    void shouldFindById() {
        Payment payment = aPayment();
        when(loadPaymentPort.findById(payment.getId())).thenReturn(Optional.of(payment));

        Payment result = queryPaymentService.findById(payment.getId());

        assertThat(result).isEqualTo(payment);
    }

    @Test
    @DisplayName("findById() should throw PaymentNotFoundException when not found")
    void shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(loadPaymentPort.findById(id)).thenReturn(Optional.empty());

        assertThatExceptionOfType(PaymentNotFoundException.class)
                .isThrownBy(() -> queryPaymentService.findById(id));
    }

    @Test
    @DisplayName("findByStatus() should delegate to port")
    void shouldFindByStatus() {
        Payment payment = aPayment();
        PaymentPage page = new PaymentPage(List.of(payment), 0, 20, 1L, 1);
        when(loadPaymentPort.findByStatus(PaymentStatus.PENDING, 0, 20)).thenReturn(page);

        PaymentPage result = queryPaymentService.findByStatus(PaymentStatus.PENDING, 0, 20);

        assertThat(result.content()).containsExactly(payment);
        verify(loadPaymentPort).findByStatus(PaymentStatus.PENDING, 0, 20);
    }

    @Test
    @DisplayName("findByDateRange() should delegate to port when dates are valid")
    void shouldFindByDateRange() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 59);
        Payment payment = aPayment();
        PaymentPage page = new PaymentPage(List.of(payment), 0, 20, 1L, 1);
        when(loadPaymentPort.findByCreatedAtBetween(from, to, 0, 20)).thenReturn(page);

        PaymentPage result = queryPaymentService.findByDateRange(from, to, 0, 20);

        assertThat(result.content()).containsExactly(payment);
    }

    @Test
    @DisplayName("findByDateRange() should throw when 'from' is after 'to'")
    void shouldThrowWhenFromIsAfterTo() {
        LocalDateTime from = LocalDateTime.of(2024, 12, 31, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 0, 0);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> queryPaymentService.findByDateRange(from, to, 0, 20))
                .withMessageContaining("'from' date must be before");

        verifyNoInteractions(loadPaymentPort);
    }

    @Test
    @DisplayName("findByMerchant() should delegate to port")
    void shouldFindByMerchant() {
        Payment payment = aPayment();
        PaymentPage page = new PaymentPage(List.of(payment), 0, 20, 1L, 1);
        when(loadPaymentPort.findByMerchantId("merchant-001", 0, 20)).thenReturn(page);

        PaymentPage result = queryPaymentService.findByMerchant("merchant-001", 0, 20);

        assertThat(result.content()).containsExactly(payment);
    }

    private Payment aPayment() {
        return Payment.create("merchant-001",
                new Money(BigDecimal.valueOf(100), "BRL"), PaymentMethod.PIX, "Test");
    }
}
