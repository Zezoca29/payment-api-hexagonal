package com.hexapay.payments.domain.port.out;

import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentPage;
import com.hexapay.payments.domain.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Driven port — LoadPayment.
 *
 * Defines all queries the domain needs against its persistence store.
 * The repository abstraction belongs here, in the domain — NOT in the persistence adapter.
 *
 * List methods use page/size to push pagination down to the database,
 * avoiding full-table loads for large datasets.
 */
public interface LoadPaymentPort {

    Optional<Payment> findById(UUID id);

    PaymentPage findByStatus(PaymentStatus status, int page, int size);

    PaymentPage findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, int page, int size);

    PaymentPage findByMerchantId(String merchantId, int page, int size);
}
