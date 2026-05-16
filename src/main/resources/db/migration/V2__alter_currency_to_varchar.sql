-- ============================================================
-- V2 — Fix currency column type: CHAR(3) -> VARCHAR(3)
-- Author : Kaique Augusto da Cruz Zeza
-- Reason : PostgreSQL stores CHAR(n) as bpchar internally.
--          Hibernate schema validation expects varchar(3) for
--          a plain String field, causing startup failure.
-- ============================================================

ALTER TABLE payments ALTER COLUMN currency TYPE VARCHAR(3);
