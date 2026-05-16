# ADR-001 — Hexagonal Architecture (Ports & Adapters)

| Field       | Value                              |
|-------------|-----------------------------------|
| **Status**  | Accepted                          |
| **Date**    | 2024-06-01                        |
| **Author**  | Kaique Augusto da Cruz Zeza       |

---

## Context

We are building a payment processing API that is expected to:
- Evolve its business rules independently of its delivery mechanism (REST today, gRPC tomorrow, message queue next week).
- Be testable without database or HTTP infrastructure.
- Be understandable by engineers who join the project months later.

The most critical concern is that **business rules do not leak into infrastructure code**.

## Decision

We adopt **Hexagonal Architecture** (also known as Ports & Adapters), defined by Alistair Cockburn (2005).

The system is organized into three concentric zones:

```
┌──────────────────────────────────────────────────────────────┐
│  Infrastructure (adapters)                                   │
│  ┌──────────────────────────────────────────────────────┐    │
│  │  Application (use cases / orchestration)             │    │
│  │  ┌────────────────────────────────────────────────┐  │    │
│  │  │  Domain (entities, business rules, port def.)  │  │    │
│  │  └────────────────────────────────────────────────┘  │    │
│  └──────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
```

### Port taxonomy

| Port type | Direction | Owner  | Example                         |
|-----------|-----------|--------|---------------------------------|
| Driving   | In (left) | Domain | `CreatePaymentUseCase`          |
| Driven    | Out (right)| Domain | `SavePaymentPort`              |

### Dependency rule (enforced)

> All source-code dependencies point **inward**. The domain depends on nothing. Infrastructure depends on domain interfaces.

```
REST Controller → UseCase interface ← Service impl → Port interface ← JPA Adapter → PostgreSQL
```

## Alternatives Considered

| Alternative        | Rejected because                                               |
|--------------------|----------------------------------------------------------------|
| Layered (N-Tier)   | Business logic bleeds into service/repository layers over time |
| Clean Architecture | More ceremony; hexagonal is sufficient for this domain size    |
| CQRS + Event Sourcing | Overkill for MVP; can be added later without structural change |

## Consequences

**Positive:**
- Domain code can be unit-tested without any Spring context or database.
- Switching from PostgreSQL to another database = write one new adapter.
- The architecture acts as living documentation: reading a port interface tells you exactly what the system does.

**Negative:**
- More files and initial boilerplate vs a simple MVC controller-service-repository.
- Developers unfamiliar with the pattern need a brief orientation.

## References

- Alistair Cockburn — [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/) (2005)
- Tom Hombergs — *Get Your Hands Dirty on Clean Architecture* (2nd ed., 2023)
