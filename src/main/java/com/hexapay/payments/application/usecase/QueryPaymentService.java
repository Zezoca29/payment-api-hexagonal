package com.hexapay.payments.application.usecase;

import com.hexapay.payments.domain.exception.PaymentNotFoundException;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentStatus;
import com.hexapay.payments.domain.port.in.QueryPaymentUseCase;
import com.hexapay.payments.domain.port.out.LoadPaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    public List<Payment> findByStatus(PaymentStatus status) {
        log.debug("Querying payments with status='{}'", status);
        return loadPaymentPort.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByDateRange(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "The 'from' date must be before the 'to' date. from=" + from + ", to=" + to);
        }
        log.debug("Querying payments between {} and {}", from, to);
        return loadPaymentPort.findByCreatedAtBetween(from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByMerchant(String merchantId) {
        log.debug("Querying payments for merchant='{}'", merchantId);
        return loadPaymentPort.findByMerchantId(merchantId);
    }
}
