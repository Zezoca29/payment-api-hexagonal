package com.hexapay.payments.adapters.in.web.mapper;

import com.hexapay.payments.adapters.in.web.dto.CreatePaymentRequest;
import com.hexapay.payments.adapters.in.web.dto.PaymentResponse;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.port.in.CreatePaymentUseCase;
import org.springframework.stereotype.Component;

/**
 * Maps between the web layer DTOs and the domain model / use case commands.
 *
 * Keeping the mapper in the adapter layer ensures the domain never
 * knows about HTTP concepts (requests, responses, JSON).
 */
@Component
public class PaymentWebMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMerchantId(),
                payment.getAmount().amount(),
                payment.getAmount().currency(),
                payment.getMethod().name(),
                payment.getDescription(),
                payment.getStatus().name(),
                payment.getExternalReference(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getRefundedAt()
        );
    }

    public CreatePaymentUseCase.CreatePaymentCommand toCommand(CreatePaymentRequest request) {
        return new CreatePaymentUseCase.CreatePaymentCommand(
                request.merchantId(),
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                request.description()
        );
    }
}
