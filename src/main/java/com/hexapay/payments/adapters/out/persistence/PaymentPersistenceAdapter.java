package com.hexapay.payments.adapters.out.persistence;

import com.hexapay.payments.adapters.out.persistence.mapper.PaymentPersistenceMapper;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentStatus;
import com.hexapay.payments.domain.port.out.LoadPaymentPort;
import com.hexapay.payments.domain.port.out.SavePaymentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary adapter — implements both {@link SavePaymentPort} and {@link LoadPaymentPort}.
 *
 * This class bridges the gap between the domain's port interfaces and the
 * Spring Data JPA repository. The domain depends on the ports (abstractions);
 * this adapter depends on both, completing the dependency inversion.
 *
 * Dependency direction:
 *   Domain → Port Interface ← This Adapter → JpaRepository → PostgreSQL
 */
@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements SavePaymentPort, LoadPaymentPort {

    private final PaymentJpaRepository repository;
    private final PaymentPersistenceMapper mapper;

    @Override
    public Payment save(Payment payment) {
        var entity = mapper.toEntity(payment);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return repository.findByStatus(status.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Payment> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return repository.findByCreatedAtBetween(from, to)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Payment> findByMerchantId(String merchantId) {
        return repository.findByMerchantId(merchantId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
