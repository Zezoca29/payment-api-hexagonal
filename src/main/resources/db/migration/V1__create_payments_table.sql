-- ============================================================
-- V1 — Initial schema: payments table
-- Author : Kaique Augusto da Cruz Zeza
-- Date   : 2024-06-01
-- ============================================================

CREATE TABLE payments
(
    id                 UUID         NOT NULL,
    merchant_id        VARCHAR(255) NOT NULL,
    amount             DECIMAL(19, 4) NOT NULL,
    currency           CHAR(3)      NOT NULL,
    payment_method     VARCHAR(50)  NOT NULL,
    description        VARCHAR(255) NOT NULL,
    status             VARCHAR(50)  NOT NULL,
    external_reference VARCHAR(255),
    failure_reason     TEXT,
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL,
    refunded_at        TIMESTAMP,

    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT chk_payments_amount CHECK (amount > 0),
    CONSTRAINT chk_payments_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    CONSTRAINT chk_payments_method CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'BOLETO', 'BANK_TRANSFER'))
);

-- Performance indexes
CREATE INDEX idx_payments_status      ON payments (status);
CREATE INDEX idx_payments_merchant_id ON payments (merchant_id);
CREATE INDEX idx_payments_created_at  ON payments (created_at DESC);

-- Compound index for common query: merchant + status
CREATE INDEX idx_payments_merchant_status ON payments (merchant_id, status);

COMMENT ON TABLE payments IS 'Central payments ledger — stores all payment events.';
COMMENT ON COLUMN payments.status IS 'Lifecycle state: PENDING → COMPLETED → REFUNDED | PENDING → FAILED';
COMMENT ON COLUMN payments.external_reference IS 'Transaction ID returned by the payment gateway.';
