package com.hexapay.payments.application.usecase;

import com.hexapay.payments.domain.exception.PaymentNotFoundException;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.port.in.RefundPaymentUseCase;
import com.hexapay.payments.domain.port.out.LoadPaymentPort;
import com.hexapay.payments.domain.port.out.SavePaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service implementing the RefundPayment use case.
 *
 * Loads the payment from the store, delegates the refund business rule
 * to the domain aggregate, then persists the new state.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundPaymentService implements RefundPaymentUseCase {

    private final LoadPaymentPort loadPaymentPort;
    private final SavePaymentPort savePaymentPort;

    @Override
    @Transactional
    public Payment refundPayment(UUID paymentId) {
        log.info("Processing refund for payment id='{}'", paymentId);

        Payment payment = loadPaymentPort.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        payment.refund(); // business rule enforced by the domain aggregate

        Payment refunded = savePaymentPort.save(payment);
        log.info("Payment id='{}' successfully refunded", paymentId);
        return refunded;
    }
}
