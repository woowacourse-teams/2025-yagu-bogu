ALTER TABLE gifticon_issuances
    ADD COLUMN recipient_phone_number VARCHAR(20) NULL AFTER external_order_id,
    ADD COLUMN reserve_trace_id BIGINT NULL AFTER status,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER reserve_trace_id,
    MODIFY COLUMN status VARCHAR(30) NOT NULL;

UPDATE gifticon_issuances
SET status = CASE
                 WHEN status = 'READY' AND recipient_phone_number IS NULL THEN 'AWAITING_RECIPIENT_INFO'
                 WHEN status = 'READY' THEN 'REQUEST_RETRYABLE'
                 WHEN status = 'REQUESTING' THEN 'REQUEST_IN_PROGRESS'
                 WHEN status = 'REQUESTED' THEN 'REQUEST_ACCEPTED'
                 ELSE status
             END
WHERE status IN ('READY', 'REQUESTING', 'REQUESTED');
