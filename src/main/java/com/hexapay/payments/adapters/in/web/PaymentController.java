package com.hexapay.payments.adapters.in.web;

import com.hexapay.payments.adapters.in.web.dto.CreatePaymentRequest;
import com.hexapay.payments.adapters.in.web.dto.PaymentResponse;
import com.hexapay.payments.adapters.in.web.mapper.PaymentWebMapper;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentStatus;
import com.hexapay.payments.domain.port.in.CreatePaymentUseCase;
import com.hexapay.payments.domain.port.in.QueryPaymentUseCase;
import com.hexapay.payments.domain.port.in.RefundPaymentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Primary adapter — exposes the payment domain via a REST API.
 *
 * This controller's only responsibility is HTTP: parse requests,
 * call use cases, and serialize responses. No business logic here.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management — creation, query, and refund operations")
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final RefundPaymentUseCase refundPaymentUseCase;
    private final QueryPaymentUseCase queryPaymentUseCase;
    private final PaymentWebMapper mapper;

    // ─── Commands ─────────────────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new payment", description = "Creates a payment in PENDING state.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment created"),
            @ApiResponse(responseCode = "400", description = "Validation error in request body"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        Payment payment = createPaymentUseCase.createPayment(mapper.toCommand(request));
        return mapper.toResponse(payment);
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund a payment", description = "Refunds a COMPLETED payment. Fails for PENDING, FAILED, or already REFUNDED payments.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment refunded"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "409", description = "Payment already refunded"),
            @ApiResponse(responseCode = "422", description = "Payment is not in a refundable state")
    })
    public PaymentResponse refundPayment(
            @Parameter(description = "Payment UUID") @PathVariable UUID id) {
        Payment payment = refundPaymentUseCase.refundPayment(id);
        return mapper.toResponse(payment);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public PaymentResponse getPayment(
            @Parameter(description = "Payment UUID") @PathVariable UUID id) {
        return mapper.toResponse(queryPaymentUseCase.findById(id));
    }

    @GetMapping
    @Operation(
            summary = "List payments with optional filters",
            description = "Filter by status, date range, or merchantId. Provide exactly one filter at a time."
    )
    public List<PaymentResponse> listPayments(
            @Parameter(description = "Filter by payment status (PENDING, COMPLETED, FAILED, REFUNDED)")
            @RequestParam(required = false) PaymentStatus status,

            @Parameter(description = "Start of date range (ISO-8601, e.g. 2024-01-01T00:00:00)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "End of date range (ISO-8601, e.g. 2024-12-31T23:59:59)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @Parameter(description = "Filter by merchant identifier")
            @RequestParam(required = false) String merchantId
    ) {
        if (status != null) {
            return toResponseList(queryPaymentUseCase.findByStatus(status));
        }
        if (from != null && to != null) {
            return toResponseList(queryPaymentUseCase.findByDateRange(from, to));
        }
        if (merchantId != null) {
            return toResponseList(queryPaymentUseCase.findByMerchant(merchantId));
        }
        return List.of();
    }

    private List<PaymentResponse> toResponseList(List<Payment> payments) {
        return payments.stream().map(mapper::toResponse).toList();
    }
}
