-- 경기중 실시간 상태(현재 이닝, 타자/투수, 진루정보, 볼/스트라이크/아웃)
-- 경기 진행에 따라 계속 덮어써지는 값이라 Bronze 재처리 대상이 아니라 games 테이블에 직접 반영함
ALTER TABLE games
    ADD COLUMN current_batter_team    VARCHAR(10) NULL,
    ADD COLUMN current_batter_name    VARCHAR(50) NULL,
    ADD COLUMN current_pitcher_team   VARCHAR(10) NULL,
    ADD COLUMN current_pitcher_name   VARCHAR(50) NULL,
    ADD COLUMN current_inning         TINYINT     NULL,
    ADD COLUMN current_inning_half    VARCHAR(10) NULL,
    ADD COLUMN first_base_occupied    BOOLEAN     NULL,
    ADD COLUMN second_base_occupied   BOOLEAN     NULL,
    ADD COLUMN third_base_occupied    BOOLEAN     NULL,
    ADD COLUMN balls                  TINYINT     NULL,
    ADD COLUMN strikes                TINYINT     NULL,
    ADD COLUMN outs                   TINYINT     NULL,
    ADD INDEX idx_games_date_start_at (date, start_at);
