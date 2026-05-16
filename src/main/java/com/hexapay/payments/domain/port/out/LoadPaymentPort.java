package com.hexapay.payments.domain.port.out;

import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Driven port — LoadPayment.
 *
 * Defines all queries the domain needs against its persistence store.
 * The repository abstraction belongs here, in the domain — NOT in the persistence adapter.
 */
public interface LoadPaymentPort {

    Optional<Payment> findById(UUID id);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<Payment> findByMerchantId(String merchantId);
}
