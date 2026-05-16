# ADR-003 — Flyway for Database Schema Migrations

| Field       | Value        |
|-------------|--------------|
| **Status**  | Accepted     |
| **Date**    | 2024-06-01   |
| **Author**  | HexaPay Team |

---

## Context

The database schema will evolve over time. We need a strategy to:
1. Apply schema changes repeatably and safely across all environments (local, CI, staging, production).
2. Maintain an audit trail of every schema change.
3. Ensure the schema in production exactly matches what the codebase expects.

## Decision

Use **Flyway** for versioned SQL migrations, integrated with Spring Boot's auto-configuration.

## Migration naming convention

```
V{version}__{description}.sql
 │            └── snake_case description
 └── Numeric, monotonically increasing (e.g. 1, 2, 3...)
```

Examples:
```
V1__create_payments_table.sql
V2__add_idempotency_key_to_payments.sql
V3__create_merchants_table.sql
```

## Rules (enforced)

1. **Never edit** an already-applied migration file — Flyway checksums detect this and will refuse to start.
2. **Always** create a new versioned file for schema changes.
3. **Rollback scripts** are written manually if needed (Flyway OSS does not auto-rollback).

## Alternatives Considered

| Alternative     | Rejected because                                               |
|-----------------|----------------------------------------------------------------|
| Liquibase       | XML/YAML format adds indirection; SQL migrations are clearer   |
| Hibernate DDL   | `ddl-auto: create-drop` is dangerous in production            |
| Manual SQL      | No tracking of what was applied; error-prone across envs       |

## Consequences

- `spring.jpa.hibernate.ddl-auto=validate` is set — Hibernate validates but never creates/alters tables.
- `flyway.validate-on-migrate=true` ensures CI fails if a migration is tampered with.
- TestContainers in integration tests receives a fresh database; Flyway runs all migrations from scratch, verifying them on every CI run.
