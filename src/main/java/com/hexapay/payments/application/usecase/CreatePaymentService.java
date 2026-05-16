package com.hexapay.payments.application.usecase;

import com.hexapay.payments.domain.model.Money;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentMethod;
import com.hexapay.payments.domain.port.in.CreatePaymentUseCase;
import com.hexapay.payments.domain.port.out.SavePaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing the CreatePayment use case.
 *
 * This class orchestrates domain objects and ports — it contains no business logic
 * itself. All rules live in the {@link Payment} aggregate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePaymentService implements CreatePaymentUseCase {

    private final SavePaymentPort savePaymentPort;

    @Override
    @Transactional
    public Payment createPayment(CreatePaymentCommand command) {
        log.info("Creating payment for merchant='{}', amount={} {}",
                command.merchantId(), command.amount(), command.currency());

        Money money = new Money(command.amount(), command.currency().toUpperCase());
        PaymentMethod method = parseMethod(command.paymentMethod());

        Payment payment = Payment.create(
                command.merchantId(),
                money,
                method,
                command.description()
        );

        Payment saved = savePaymentPort.save(payment);
        log.info("Payment created successfully with id='{}'", saved.getId());
        return saved;
    }

    private PaymentMethod parseMethod(String paymentMethod) {
        try {
            return PaymentMethod.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid payment method: '" + paymentMethod + "'. Accepted values: CREDIT_CARD, DEBIT_CARD, PIX, BOLETO, BANK_TRANSFER");
        }
    }
}
