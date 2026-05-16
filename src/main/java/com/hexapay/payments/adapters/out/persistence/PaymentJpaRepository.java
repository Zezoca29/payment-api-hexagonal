package com.hexapay.payments.adapters.out.persistence;

import com.hexapay.payments.adapters.out.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository.
 *
 * This interface is an implementation detail of the persistence adapter.
 * It is NOT visible to the domain — the domain only sees {@link com.hexapay.payments.domain.port.out.LoadPaymentPort}.
 */
@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    List<PaymentEntity> findByStatus(String status);

    List<PaymentEntity> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<PaymentEntity> findByMerchantId(String merchantId);
}
