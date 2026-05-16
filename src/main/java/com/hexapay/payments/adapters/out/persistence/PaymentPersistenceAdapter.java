package com.hexapay.payments.adapters.out.persistence;

import com.hexapay.payments.adapters.out.persistence.entity.PaymentEntity;
import com.hexapay.payments.adapters.out.persistence.mapper.PaymentPersistenceMapper;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentPage;
import com.hexapay.payments.domain.model.PaymentStatus;
import com.hexapay.payments.domain.port.out.LoadPaymentPort;
import com.hexapay.payments.domain.port.out.SavePaymentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
 *
 * Spring Data's {@code Page<T>} is mapped to the domain {@link PaymentPage} here,
 * keeping the domain layer free of any framework dependency.
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
    public PaymentPage findByStatus(PaymentStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return toPaymentPage(repository.findByStatus(status.name(), pageable));
    }

    @Override
    public PaymentPage findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return toPaymentPage(repository.findByCreatedAtBetween(from, to, pageable));
    }

    @Override
    public PaymentPage findByMerchantId(String merchantId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return toPaymentPage(repository.findByMerchantId(merchantId, pageable));
    }

    private PaymentPage toPaymentPage(Page<PaymentEntity> springPage) {
        List<Payment> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return new PaymentPage(
                content,
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages()
        );
    }
}
