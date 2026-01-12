-- talks 테이블에 client_message_id 컬럼 추가
ALTER TABLE talks
    ADD COLUMN client_message_id VARCHAR(36) NOT NULL;

-- Unique 제약 추가
ALTER TABLE talks
    ADD CONSTRAINT uk_client_message_id UNIQUE (client_message_id);

-- 인덱스 추가 (성능 최적화)
CREATE INDEX idx_game_member_content_created
    ON talks (game_id, member_id, content, created_at);

CREATE INDEX idx_client_message_id
    ON talks (client_message_id);
