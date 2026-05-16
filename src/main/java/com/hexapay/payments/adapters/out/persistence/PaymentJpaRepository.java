package com.hexapay.payments.adapters.out.persistence;

import com.hexapay.payments.adapters.out.persistence.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data JPA repository.
 *
 * This interface is an implementation detail of the persistence adapter.
 * It is NOT visible to the domain — the domain only sees {@link com.hexapay.payments.domain.port.out.LoadPaymentPort}.
 */
@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    Page<PaymentEntity> findByStatus(String status, Pageable pageable);

    Page<PaymentEntity> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<PaymentEntity> findByMerchantId(String merchantId, Pageable pageable);
}
