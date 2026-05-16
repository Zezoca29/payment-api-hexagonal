package com.hexapay.payments.integration;

import com.hexapay.payments.adapters.in.web.dto.CreatePaymentRequest;
import com.hexapay.payments.adapters.in.web.dto.ErrorResponse;
import com.hexapay.payments.adapters.in.web.dto.PagedPaymentResponse;
import com.hexapay.payments.adapters.in.web.dto.PaymentResponse;
import com.hexapay.payments.config.security.ApiKeyAuthFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test — boots the entire Spring context and communicates
 * with a real PostgreSQL database managed by TestContainers.
 *
 * This test validates the complete flow: HTTP → Controller → Use Case → Repository → DB.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Payment API integration tests")
class PaymentControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("paymentsdb_test")
            .withUsername("payments")
            .withPassword("payments");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${app.security.api-key}")
    private String apiKey;

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(ApiKeyAuthFilter.API_KEY_HEADER, apiKey);
        return headers;
    }

    private <T> HttpEntity<T> withAuth(T body) {
        return new HttpEntity<>(body, authHeaders());
    }

    private HttpEntity<Void> withAuth() {
        return new HttpEntity<>(authHeaders());
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/payments — should return 201 and a PENDING payment")
    void shouldCreatePayment() {
        var request = createRequest();

        ResponseEntity<PaymentResponse> response =
                restTemplate.postForEntity("/api/v1/payments", withAuth(request), PaymentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("PENDING");
        assertThat(response.getBody().merchantId()).isEqualTo("merchant-001");
        assertThat(response.getBody().amount()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
    }

    @Test
    @DisplayName("POST /api/v1/payments — should return 401 when API key is missing")
    void shouldReturn401WhenApiKeyMissing() {
        ResponseEntity<String> response =
                restTemplate.postForEntity("/api/v1/payments", createRequest(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/v1/payments — should return 400 for missing required fields")
    void shouldReturn400OnValidationError() {
        var invalid = new CreatePaymentRequest("", BigDecimal.valueOf(100), "BRL", "PIX", "Test");

        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity("/api/v1/payments", withAuth(invalid), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
    }

    // ─── Get by ID ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/payments/{id} — should return the payment after creation")
    void shouldGetPaymentById() {
        PaymentResponse created = createPaymentAndGet();

        ResponseEntity<PaymentResponse> response =
                restTemplate.exchange("/api/v1/payments/" + created.id(),
                        HttpMethod.GET, withAuth(), PaymentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(created.id());
    }

    @Test
    @DisplayName("GET /api/v1/payments/{id} — should return 404 for unknown ID")
    void shouldReturn404ForUnknownId() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.exchange("/api/v1/payments/" + UUID.randomUUID(),
                        HttpMethod.GET, withAuth(), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("PAYMENT_NOT_FOUND");
    }

    // ─── Refund ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/payments/{id}/refund — should return 422 for PENDING payment")
    void shouldReturn422WhenRefundingPendingPayment() {
        PaymentResponse payment = createPaymentAndGet();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/payments/" + payment.id() + "/refund", withAuth(), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("INVALID_PAYMENT_STATE");
    }

    // ─── List ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/payments?status=PENDING — should return paginated results filtered by status")
    void shouldFilterByStatus() {
        createPaymentAndGet(); // ensure at least one PENDING payment

        ResponseEntity<PagedPaymentResponse> response =
                restTemplate.exchange("/api/v1/payments?status=PENDING",
                        HttpMethod.GET, withAuth(), PagedPaymentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).isNotEmpty();
        assertThat(response.getBody().content()).allMatch(p -> "PENDING".equals(p.status()));
        assertThat(response.getBody().totalElements()).isGreaterThan(0);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private CreatePaymentRequest createRequest() {
        return new CreatePaymentRequest(
                "merchant-001",
                BigDecimal.valueOf(150.00),
                "BRL",
                "PIX",
                "Integration test order"
        );
    }

    private PaymentResponse createPaymentAndGet() {
        ResponseEntity<PaymentResponse> response =
                restTemplate.postForEntity("/api/v1/payments", withAuth(createRequest()), PaymentResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }
}
