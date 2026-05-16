package com.hexapay.payments.adapters.in.web.handler;

import com.hexapay.payments.adapters.in.web.dto.ErrorResponse;
import com.hexapay.payments.domain.exception.InvalidPaymentStateException;
import com.hexapay.payments.domain.exception.PaymentAlreadyRefundedException;
import com.hexapay.payments.domain.exception.PaymentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler — translates domain/application exceptions into
 * standardized HTTP responses. Lives in the adapter layer; the domain has
 * zero knowledge of HTTP status codes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePaymentNotFound(PaymentNotFoundException ex) {
        log.warn("Payment not found: {}", ex.getMessage());
        return new ErrorResponse("PAYMENT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(PaymentAlreadyRefundedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyRefunded(PaymentAlreadyRefundedException ex) {
        log.warn("Conflict — payment already refunded: {}", ex.getMessage());
        return new ErrorResponse("PAYMENT_ALREADY_REFUNDED", ex.getMessage());
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleInvalidState(InvalidPaymentStateException ex) {
        log.warn("Invalid payment state transition: {}", ex.getMessage());
        return new ErrorResponse("INVALID_PAYMENT_STATE", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", details);
        return new ErrorResponse("VALIDATION_ERROR", details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return new ErrorResponse("INVALID_ARGUMENT", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred. Please try again later.");
    }
}
