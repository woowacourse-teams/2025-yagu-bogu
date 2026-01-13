-- Bronze 레이어: 원본 데이터 저장 (메달리온 아키텍처)
-- 크롤링한 JSON을 가공 없이 그대로 저장
-- 정책 변경 시 재처리 가능, 데이터 유실 방지

CREATE TABLE bronze_games_raw
(
    raw_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    date             DATE        NOT NULL,
    stadium          VARCHAR(50) NOT NULL,
    home_team        VARCHAR(50) NOT NULL,
    away_team        VARCHAR(50) NOT NULL,
    start_time       TIME,
    state            ENUM ('SCHEDULED','LIVE','COMPLETED','CANCELED'),
    collected_at     DATETIME(6) NOT NULL,
    payload          JSON        NOT NULL,
    content_hash     VARCHAR(64) NOT NULL,
    etl_processed_at DATETIME(6) NULL COMMENT 'ETL 처리 완료 시각 (중복 처리 방지)',

    UNIQUE KEY uk_natural_key (date, stadium, home_team, away_team, start_time),
    INDEX idx_date (date),
    INDEX idx_collected_at (collected_at),
    INDEX idx_etl_pending (collected_at, etl_processed_at) COMMENT 'ETL 미처리 데이터 조회용'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
