# HexaPay — Payment API

[![CI/CD](https://github.com/YOUR_USERNAME/hexapay/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/YOUR_USERNAME/hexapay/actions/workflows/ci-cd.yml)
[![Coverage](https://codecov.io/gh/YOUR_USERNAME/hexapay/branch/main/graph/badge.svg)](https://codecov.io/gh/YOUR_USERNAME/hexapay)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **Production-grade REST API for payment processing**, built with **Hexagonal Architecture** (Ports & Adapters).  
> Demonstrates domain isolation, comprehensive testing, and an automated CI/CD pipeline — the engineering standards expected at senior level.

---

## Table of Contents

- [Architecture](#architecture)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [API Reference](#api-reference)
- [Running Tests](#running-tests)
- [Project Structure](#project-structure)
- [Architecture Decision Records](#architecture-decision-records)
- [Git Flow](#git-flow)
- [Contributing](#contributing)

---

## Architecture

This project implements **Hexagonal Architecture** (Ports & Adapters), coined by Alistair Cockburn. The central principle: the **domain is fully isolated from all infrastructure concerns**. Frameworks, databases, and HTTP are implementation details.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           LEFT (Driving) Side                           │
│                                                                         │
│   REST Client ──► PaymentController ──► CreatePaymentUseCase (port in) │
│   Swagger UI  ──►         │          ──► RefundPaymentUseCase  (port in) │
│                           │          ──► QueryPaymentUseCase   (port in) │
│                           │                                             │
│  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ │
│                           │         APPLICATION CORE                    │
│                           ▼                                             │
│              ┌─────────────────────────┐                                │
│              │       Domain Model       │                                │
│              │  Payment · Money        │                                │
│              │  PaymentStatus          │                                │
│              │  PaymentMethod          │                                │
│              └─────────────────────────┘                                │
│                           │                                             │
│              SavePaymentPort (port out) ──► PaymentPersistenceAdapter   │
│              LoadPaymentPort (port out) ──►        │                    │
│                                                    ▼                    │
│                                           PaymentJpaRepository          │
│                                                    │                    │
│ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ │
│                          RIGHT (Driven) Side        ▼                   │
│                                             PostgreSQL Database          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Dependency Rule

All source-code dependencies point **inward only**:

```
HTTP Adapter  →  Port Interface (in)  ←  Service  →  Port Interface (out)  ←  JPA Adapter  →  PostgreSQL
```

The domain (`Payment`, `Money`, etc.) depends on **nothing**. It can be tested with zero infrastructure.

### Why this matters for payments

Payments is one of the most regulated, audited, and failure-sensitive domains in software. Hexagonal architecture makes explicit the answer to "where does this rule live?": always in the domain, never in a controller or repository.

---

## Features

- **Full payment lifecycle**: create, query, refund, filter by status / date range / merchant
- **Explicit state machine**: `PENDING` → `COMPLETED` → `REFUNDED` | `PENDING` → `FAILED`
- **Business rules in the domain**, not controllers (tested without Spring)
- **Bean Validation** at the HTTP boundary, not inside business logic
- **Standardized error responses** with error codes and timestamps
- **OpenAPI 3 / Swagger UI** — interactive documentation at `/swagger-ui.html`
- **Flyway versioned migrations** — reproducible schema across all environments
- **Spring Actuator** health and metrics endpoints
- **JaCoCo coverage report** — minimum 80% enforced as a build gate
- **GitHub Actions CI/CD** — test, coverage, Docker build, push to GHCR
- **Docker Compose** — entire stack up with a single command

---

## Tech Stack

| Layer          | Technology                              |
|----------------|-----------------------------------------|
| Language       | Java 21 (LTS)                           |
| Framework      | Spring Boot 3.4                         |
| Database       | PostgreSQL 16                           |
| ORM            | Spring Data JPA / Hibernate 6           |
| Migrations     | Flyway                                  |
| Documentation  | SpringDoc OpenAPI 3 (Swagger UI)        |
| Testing        | JUnit 5 + Mockito + TestContainers      |
| Coverage       | JaCoCo (≥ 80% enforced)                 |
| CI/CD          | GitHub Actions                          |
| Containerization | Docker + Docker Compose               |
| Registry       | GitHub Container Registry (GHCR)        |

---

## Quick Start

### Prerequisites

- **Docker 24+** and **Docker Compose V2** — that's it for running the app
- **Java 21** and **Maven 3.9+** — for local development and running tests

### Run with Docker Compose (recommended)

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/hexapay.git
cd hexapay

# 2. Start all services (PostgreSQL + API)
docker compose up --build

# 3. Access the application
#    API base URL:  http://localhost:8080/api/v1/payments
#    Swagger UI:    http://localhost:8080/swagger-ui.html
#    Health check:  http://localhost:8080/actuator/health
```

### Run Locally (development mode)

```bash
# Start only the database
docker compose up postgres -d

# Run the application with the 'local' profile (verbose SQL logging)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## API Reference

Full interactive documentation: **`http://localhost:8080/swagger-ui.html`**

### Endpoints Summary

| Method   | Endpoint                          | Description                   | Status Codes           |
|----------|-----------------------------------|-------------------------------|------------------------|
| `POST`   | `/api/v1/payments`                | Create a new payment          | 201, 400, 422          |
| `GET`    | `/api/v1/payments/{id}`           | Get payment by ID             | 200, 404               |
| `POST`   | `/api/v1/payments/{id}/refund`    | Refund a payment              | 200, 404, 409, 422     |
| `GET`    | `/api/v1/payments?status=...`     | Filter by status              | 200                    |
| `GET`    | `/api/v1/payments?from=...&to=...`| Filter by date range          | 200, 400               |
| `GET`    | `/api/v1/payments?merchantId=...` | Filter by merchant            | 200                    |

### Create a Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "loja-abc-001",
    "amount": 299.90,
    "currency": "BRL",
    "paymentMethod": "PIX",
    "description": "Pedido #7842 — Tênis Nike Air Max"
  }'
```

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "merchantId": "loja-abc-001",
  "amount": 299.90,
  "currency": "BRL",
  "paymentMethod": "PIX",
  "description": "Pedido #7842 — Tênis Nike Air Max",
  "status": "PENDING",
  "externalReference": null,
  "failureReason": null,
  "createdAt": "2024-06-01T10:30:00",
  "updatedAt": "2024-06-01T10:30:00",
  "refundedAt": null
}
```

### Refund a Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments/550e8400-e29b-41d4-a716-446655440000/refund
```

### Filter by Status

```bash
curl "http://localhost:8080/api/v1/payments?status=COMPLETED"
```

### Filter by Date Range

```bash
curl "http://localhost:8080/api/v1/payments?from=2024-06-01T00:00:00&to=2024-06-30T23:59:59"
```

### Payment Methods

| Value           | Description                                      |
|-----------------|--------------------------------------------------|
| `PIX`           | Brazilian instant payment (Banco Central)        |
| `BOLETO`        | Brazilian bank slip                              |
| `CREDIT_CARD`   | Credit card (Visa, Mastercard, Amex, etc.)       |
| `DEBIT_CARD`    | Debit card                                       |
| `BANK_TRANSFER` | Standard wire transfer (TED/DOC)                 |

### Error Response Format

```json
{
  "code": "PAYMENT_NOT_FOUND",
  "message": "Payment not found with ID: 550e8400-...",
  "timestamp": "2024-06-01T10:30:00.000Z"
}
```

| HTTP Code | Error Code                   | Trigger                                  |
|-----------|------------------------------|------------------------------------------|
| `400`     | `VALIDATION_ERROR`           | Invalid request body (Bean Validation)   |
| `400`     | `INVALID_ARGUMENT`           | Invalid enum value or date range         |
| `404`     | `PAYMENT_NOT_FOUND`          | Payment ID does not exist                |
| `409`     | `PAYMENT_ALREADY_REFUNDED`   | Attempted double refund                  |
| `422`     | `INVALID_PAYMENT_STATE`      | State transition not allowed             |
| `500`     | `INTERNAL_ERROR`             | Unexpected server error                  |

---

## Running Tests

```bash
# Run unit tests only (fast, no infrastructure needed)
mvn test

# Run all tests + generate JaCoCo coverage report
mvn verify

# View the HTML coverage report
start target/site/jacoco/index.html   # Windows
open target/site/jacoco/index.html    # macOS/Linux
```

### Test Pyramid

```
                        ┌──────────────┐
                        │  Integration │  (TestContainers + full HTTP stack)
                       ┌┤    Tests     ├┐
                      ┌┤└──────────────┘├┐
                     ┌┤  Application    ├┐│
                    ┌┤│   Unit Tests    │├┐│
                   ┌┤ │  (Mocked ports) │ ├┐│
                  ┌┤  └─────────────────┘  ├┐│
                 ┌┤       Domain          ├┐│
                ─┤─    Unit Tests         ├─┤─
                 └┤  (Pure Java, no mocks)├┘
```

| Test type      | Location                   | What it tests                          | Infra needed |
|----------------|----------------------------|----------------------------------------|--------------|
| Domain unit    | `unit/domain/`             | Business rules, state transitions      | None         |
| Use case unit  | `unit/application/`        | Orchestration with mocked ports        | None         |
| Integration    | `integration/`             | Full HTTP → DB → HTTP round-trip       | PostgreSQL   |

---

## Project Structure

```
.
├── .github/
│   └── workflows/
│       └── ci-cd.yml              ← GitHub Actions pipeline
├── docs/
│   └── adr/                       ← Architecture Decision Records
│       ├── ADR-001-hexagonal-architecture.md
│       ├── ADR-002-postgresql-database.md
│       └── ADR-003-flyway-migrations.md
├── src/
│   ├── main/
│   │   ├── java/com/hexapay/payments/
│   │   │   ├── PaymentApiApplication.java
│   │   │   │
│   │   │   ├── domain/                    ← CORE — zero framework dependencies
│   │   │   │   ├── model/
│   │   │   │   │   ├── Payment.java       ← Aggregate root with business rules
│   │   │   │   │   ├── Money.java         ← Value object (immutable record)
│   │   │   │   │   ├── PaymentStatus.java ← State enum
│   │   │   │   │   └── PaymentMethod.java ← Method enum
│   │   │   │   ├── port/
│   │   │   │   │   ├── in/                ← Driving ports (use case interfaces)
│   │   │   │   │   │   ├── CreatePaymentUseCase.java
│   │   │   │   │   │   ├── RefundPaymentUseCase.java
│   │   │   │   │   │   └── QueryPaymentUseCase.java
│   │   │   │   │   └── out/               ← Driven ports (repository interfaces)
│   │   │   │   │       ├── SavePaymentPort.java
│   │   │   │   │       └── LoadPaymentPort.java
│   │   │   │   └── exception/
│   │   │   │       ├── PaymentNotFoundException.java
│   │   │   │       ├── PaymentAlreadyRefundedException.java
│   │   │   │       └── InvalidPaymentStateException.java
│   │   │   │
│   │   │   ├── application/               ← Orchestration (no business rules)
│   │   │   │   └── usecase/
│   │   │   │       ├── CreatePaymentService.java
│   │   │   │       ├── RefundPaymentService.java
│   │   │   │       └── QueryPaymentService.java
│   │   │   │
│   │   │   ├── adapters/
│   │   │   │   ├── in/web/                ← Primary adapter: REST API
│   │   │   │   │   ├── PaymentController.java
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── CreatePaymentRequest.java
│   │   │   │   │   │   ├── PaymentResponse.java
│   │   │   │   │   │   └── ErrorResponse.java
│   │   │   │   │   ├── mapper/
│   │   │   │   │   │   └── PaymentWebMapper.java
│   │   │   │   │   └── handler/
│   │   │   │   │       └── GlobalExceptionHandler.java
│   │   │   │   └── out/persistence/       ← Secondary adapter: JPA + PostgreSQL
│   │   │   │       ├── PaymentPersistenceAdapter.java
│   │   │   │       ├── PaymentJpaRepository.java
│   │   │   │       ├── entity/
│   │   │   │       │   └── PaymentEntity.java
│   │   │   │       └── mapper/
│   │   │   │           └── PaymentPersistenceMapper.java
│   │   │   │
│   │   │   └── config/
│   │   │       └── OpenApiConfig.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       └── db/migration/
│   │           └── V1__create_payments_table.sql
│   │
│   └── test/
│       ├── java/com/hexapay/payments/
│       │   ├── unit/
│       │   │   ├── domain/
│       │   │   │   ├── PaymentTest.java   ← Pure domain tests
│       │   │   │   └── MoneyTest.java
│       │   │   └── application/
│       │   │       ├── CreatePaymentServiceTest.java
│       │   │       ├── RefundPaymentServiceTest.java
│       │   │       └── QueryPaymentServiceTest.java
│       │   └── integration/
│       │       └── PaymentControllerIntegrationTest.java
│       └── resources/
│           └── application-test.yml
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## Architecture Decision Records

Every significant architectural decision is documented in `docs/adr/`:

| ADR     | Decision                         | Status   |
|---------|----------------------------------|----------|
| [ADR-001](docs/adr/ADR-001-hexagonal-architecture.md) | Hexagonal Architecture | Accepted |
| [ADR-002](docs/adr/ADR-002-postgresql-database.md) | PostgreSQL as primary database | Accepted |
| [ADR-003](docs/adr/ADR-003-flyway-migrations.md) | Flyway for schema migrations | Accepted |

---

## Git Flow

This project follows the **Git Flow** branching model:

```
main         ←── production releases (tagged, protected)
develop      ←── integration branch (always deployable)
feature/*    ←── new features  (branch from develop)
release/*    ←── release prep  (branch from develop)
hotfix/*     ←── urgent fixes  (branch from main)
```

### Common workflows

```bash
# ── Start a new feature ──────────────────────────────────────
git checkout develop
git checkout -b feature/payment-notifications
# ... write code, commit with Conventional Commits format ...
git checkout develop
git merge --no-ff feature/payment-notifications
git branch -d feature/payment-notifications
git push origin develop

# ── Prepare a release ────────────────────────────────────────
git checkout develop
git checkout -b release/1.1.0
# Bump version in pom.xml, update CHANGELOG
git checkout main
git merge --no-ff release/1.1.0
git tag -a v1.1.0 -m "Release 1.1.0"
git checkout develop
git merge --no-ff release/1.1.0
git branch -d release/1.1.0

# ── Hotfix ───────────────────────────────────────────────────
git checkout main
git checkout -b hotfix/fix-refund-race-condition
# Fix, test
git checkout main
git merge --no-ff hotfix/fix-refund-race-condition
git tag -a v1.0.1
git checkout develop
git merge --no-ff hotfix/fix-refund-race-condition
```

### Commit message convention

This project uses [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add payment notification on status change
fix: handle concurrent refund requests safely
test: add integration test for date range filter
docs: update ADR-002 with index rationale
refactor: extract Money comparison logic
```

---

## Contributing

1. Fork the repository
2. Create your branch from `develop`: `git checkout -b feature/your-feature`
3. Commit with Conventional Commits: `git commit -m 'feat: add your feature'`
4. Ensure tests pass: `mvn verify`
5. Push: `git push origin feature/your-feature`
6. Open a Pull Request targeting `develop`

Please do not open PRs directly to `main`.

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Built to demonstrate production-grade Java engineering at senior level
</p>
