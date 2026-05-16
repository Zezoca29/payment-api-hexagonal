package com.hexapay.payments.application.usecase;

import com.hexapay.payments.domain.exception.PaymentNotFoundException;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentPage;
import com.hexapay.payments.domain.model.PaymentStatus;
import com.hexapay.payments.domain.port.in.QueryPaymentUseCase;
import com.hexapay.payments.domain.port.out.LoadPaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application service implementing the QueryPayment use case.
 *
 * All read operations are marked readOnly=true for performance optimization
 * (Hibernate skips dirty-checking on entities within read-only transactions).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryPaymentService implements QueryPaymentUseCase {

    private final LoadPaymentPort loadPaymentPort;

    @Override
    @Transactional(readOnly = true)
    public Payment findById(UUID id) {
        return loadPaymentPort.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentPage findByStatus(PaymentStatus status, int page, int size) {
        log.debug("Querying payments with status='{}' (page={}, size={})", status, page, size);
        return loadPaymentPort.findByStatus(status, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentPage findByDateRange(LocalDateTime from, LocalDateTime to, int page, int size) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "The 'from' date must be before the 'to' date. from=" + from + ", to=" + to);
        }
        log.debug("Querying payments between {} and {} (page={}, size={})", from, to, page, size);
        return loadPaymentPort.findByCreatedAtBetween(from, to, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentPage findByMerchant(String merchantId, int page, int size) {
        log.debug("Querying payments for merchant='{}' (page={}, size={})", merchantId, page, size);
        return loadPaymentPort.findByMerchantId(merchantId, page, size);
    }
}
