package com.hexapay.payments.adapters.out.persistence.mapper;

import com.hexapay.payments.adapters.out.persistence.entity.PaymentEntity;
import com.hexapay.payments.domain.model.Money;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentMethod;
import com.hexapay.payments.domain.model.PaymentStatus;
import org.springframework.stereotype.Component;

/**
 * Translates between the JPA {@link PaymentEntity} and the domain {@link Payment} aggregate.
 *
 * Intentionally written manually (without MapStruct) to make the use of
 * {@code Payment.reconstitute()} explicit — a deliberate architectural boundary.
 * MapStruct cannot call private constructors or static factory methods automatically.
 */
@Component
public class PaymentPersistenceMapper {

    public PaymentEntity toEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId())
                .merchantId(payment.getMerchantId())
                .amount(payment.getAmount().amount())
                .currency(payment.getAmount().currency())
                .paymentMethod(payment.getMethod().name())
                .description(payment.getDescription())
                .status(payment.getStatus().name())
                .externalReference(payment.getExternalReference())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .refundedAt(payment.getRefundedAt())
                .build();
    }

    public Payment toDomain(PaymentEntity entity) {
        return Payment.reconstitute(
                entity.getId(),
                entity.getMerchantId(),
                new Money(entity.getAmount(), entity.getCurrency()),
                PaymentMethod.valueOf(entity.getPaymentMethod()),
                entity.getDescription(),
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getExternalReference(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getRefundedAt()
        );
    }
}
