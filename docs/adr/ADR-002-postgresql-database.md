# ADR-002 — PostgreSQL as Primary Database

| Field       | Value        |
|-------------|--------------|
| **Status**  | Accepted     |
| **Date**    | 2024-06-01   |
| **Author**  | Kaique Augusto da Cruz Zeza |

---

## Context

The payment API needs a durable, consistent, and ACID-compliant data store. Payment data is highly sensitive: partial writes or lost updates can cause financial discrepancies.

## Decision

Use **PostgreSQL 16** as the primary and only database for this service.

## Rationale

| Criterion             | PostgreSQL 16                                           |
|-----------------------|---------------------------------------------------------|
| ACID compliance       | Full support — critical for financial data              |
| `UUID` native type    | Efficient storage and indexing for payment IDs          |
| `DECIMAL` precision   | Avoids floating-point errors on monetary values         |
| `CHECK` constraints   | DB-level enforcement of status enum and positive amount |
| `pg_isready` utility  | Simple health check for Docker/Kubernetes               |
| License               | Open-source (PostgreSQL License)                        |

## Schema design decisions

- `DECIMAL(19, 4)` for amounts — supports up to 999 trillion with 4 decimal places.
- Status stored as `VARCHAR(50)` with a `CHECK` constraint (not an ENUM type) — allows adding new statuses via migration without `ALTER TYPE`.
- Compound index `(merchant_id, status)` — covers the most frequent query pattern.

## Alternatives Considered

| Alternative | Rejected because                                       |
|-------------|--------------------------------------------------------|
| MySQL 8     | `DECIMAL` behavior differences; weaker CHECK support   |
| MongoDB     | No multi-document ACID by default (pre-4.0 behavior)  |
| H2 in-memory| Only for test scope; production needs durability       |

## Consequences

- All integration tests use a real PostgreSQL container via TestContainers — no H2 dialect mismatch risk.
- Switching the database in the future requires only a new `PaymentPersistenceAdapter` implementation — the domain is unaffected (hexagonal boundary holds).
