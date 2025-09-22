ALTER TABLE check_ins
    ADD COLUMN created_at DATETIME(6) NULL,
    ADD COLUMN updated_at DATETIME(6) NULL,
    ADD COLUMN deleted_at DATETIME(6) NULL;
