package com.hexapay.payments.adapters.in.web;

import com.hexapay.payments.adapters.in.web.dto.CreatePaymentRequest;
import com.hexapay.payments.adapters.in.web.dto.PagedPaymentResponse;
import com.hexapay.payments.adapters.in.web.dto.PaymentResponse;
import com.hexapay.payments.adapters.in.web.mapper.PaymentWebMapper;
import com.hexapay.payments.domain.model.Payment;
import com.hexapay.payments.domain.model.PaymentPage;
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
import java.util.stream.Collectors;

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
            summary = "List payments with optional filters (paginated)",
            description = "Filter by status, date range, or merchantId. Provide exactly one filter at a time. " +
                    "Results are paginated — use 'page' (0-based) and 'size' to navigate."
    )
    public PagedPaymentResponse listPayments(
            @Parameter(description = "Filter by payment status (PENDING, COMPLETED, FAILED, REFUNDED)")
            @RequestParam(required = false) PaymentStatus status,

            @Parameter(description = "Start of date range (ISO-8601, e.g. 2024-01-01T00:00:00)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "End of date range (ISO-8601, e.g. 2024-12-31T23:59:59)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @Parameter(description = "Filter by merchant identifier")
            @RequestParam(required = false) String merchantId,

            @Parameter(description = "Zero-based page index (default 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size — number of items per page (default 20, max 100)")
            @RequestParam(defaultValue = "20") int size
    ) {
        int clampedSize = Math.min(size, 100);
        PaymentPage result;
        if (status != null) {
            result = queryPaymentUseCase.findByStatus(status, page, clampedSize);
        } else if (from != null && to != null) {
            result = queryPaymentUseCase.findByDateRange(from, to, page, clampedSize);
        } else if (merchantId != null) {
            result = queryPaymentUseCase.findByMerchant(merchantId, page, clampedSize);
        } else {
            result = new PaymentPage(List.of(), 0, clampedSize, 0L, 0);
        }
        return toPagedResponse(result);
    }

    private PagedPaymentResponse toPagedResponse(PaymentPage paymentPage) {
        List<PaymentResponse> content = paymentPage.content().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return new PagedPaymentResponse(
                content,
                paymentPage.pageNumber(),
                paymentPage.pageSize(),
                paymentPage.totalElements(),
                paymentPage.totalPages()
        );
    }
}
