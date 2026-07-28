ALTER TABLE gifticon_issuances
    ADD COLUMN request_started_at DATETIME(6) NULL AFTER reserve_trace_id,
    ADD COLUMN reconciliation_attempt_count INT NOT NULL DEFAULT 0 AFTER request_started_at,
    ADD COLUMN next_reconciliation_at DATETIME(6) NULL AFTER reconciliation_attempt_count,
    ADD COLUMN last_reconciled_at DATETIME(6) NULL AFTER next_reconciliation_at,
    ADD COLUMN last_reconciliation_error VARCHAR(1000) NULL AFTER last_reconciled_at,
    ADD INDEX idx_gifticon_reconciliation (status, next_reconciliation_at, id);

UPDATE gifticon_issuances
SET request_started_at = updated_at,
    reconciliation_attempt_count = 0,
    next_reconciliation_at = CURRENT_TIMESTAMP(6)
WHERE status = 'REQUEST_IN_PROGRESS';
