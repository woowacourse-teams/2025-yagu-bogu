-- V13__remove_client_message_id_from_talks.sql

-- 인덱스 제거
DROP INDEX idx_client_message_id ON talks;

-- Unique 제약 제거
ALTER TABLE talks
DROP INDEX uk_client_message_id;

-- 컬럼 제거
ALTER TABLE talks
DROP COLUMN client_message_id;
