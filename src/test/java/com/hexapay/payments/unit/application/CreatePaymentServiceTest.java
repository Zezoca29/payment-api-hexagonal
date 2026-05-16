package com.hexapay.payments.unit.application;

import com.hexapay.payments.application.usecase.CreatePaymentService;
import com.hexapay.payments.domain.model.Money;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentMethod;
import com.hexapay.payments.domain.model.PaymentStatus;
import com.hexapay.payments.domain.port.in.CreatePaymentUseCase;
import com.hexapay.payments.domain.port.out.SavePaymentPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CreatePaymentService")
@ExtendWith(MockitoExtension.class)
class CreatePaymentServiceTest {

    @Mock
    private SavePaymentPort savePaymentPort;

    @InjectMocks
    private CreatePaymentService createPaymentService;

    @Test
    @DisplayName("should create and persist a PENDING payment")
    void shouldCreatePaymentSuccessfully() {
        // Given
        var command = validCommand("PIX");
        var expectedPayment = Payment.create("merchant-001",
                new Money(BigDecimal.valueOf(200.00), "BRL"), PaymentMethod.PIX, "Order #1");
        when(savePaymentPort.save(any(Payment.class))).thenReturn(expectedPayment);

        // When
        Payment result = createPaymentService.createPayment(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(savePaymentPort).save(captor.capture());
        Payment saved = captor.getValue();
        assertThat(saved.getMerchantId()).isEqualTo("merchant-001");
        assertThat(saved.getAmount().amount()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        assertThat(saved.getMethod()).isEqualTo(PaymentMethod.PIX);
    }

    @Test
    @DisplayName("should accept all valid payment methods")
    void shouldAcceptAllValidPaymentMethods() {
        when(savePaymentPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        for (PaymentMethod method : PaymentMethod.values()) {
            var command = validCommand(method.name());
            assertThatCode(() -> createPaymentService.createPayment(command))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("should be case-insensitive for payment method")
    void shouldBeCaseInsensitive() {
        when(savePaymentPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = validCommand("pix");
        Payment result = createPaymentService.createPayment(command);
        assertThat(result.getMethod()).isEqualTo(PaymentMethod.PIX);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for unknown payment method")
    void shouldThrowForInvalidPaymentMethod() {
        var command = validCommand("CRYPTO");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> createPaymentService.createPayment(command))
                .withMessageContaining("Invalid payment method");

        verifyNoInteractions(savePaymentPort);
    }

    @Test
    @DisplayName("should uppercase currency code before creating Money")
    void shouldUppercaseCurrency() {
        when(savePaymentPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreatePaymentUseCase.CreatePaymentCommand(
                "merchant-001", BigDecimal.valueOf(100), "brl", "PIX", "Test");
        Payment result = createPaymentService.createPayment(command);

        assertThat(result.getAmount().currency()).isEqualTo("BRL");
    }

    private CreatePaymentUseCase.CreatePaymentCommand validCommand(String method) {
        return new CreatePaymentUseCase.CreatePaymentCommand(
                "merchant-001",
                BigDecimal.valueOf(200.00),
                "BRL",
                method,
                "Order #1"
        );
    }
}
