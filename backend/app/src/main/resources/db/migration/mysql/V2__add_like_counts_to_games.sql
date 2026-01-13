-- V12__create_likes_table.sql
-- 엔진/문자셋은 프로젝트 표준에 맞추세요 (InnoDB, utf8mb4 권장)

CREATE TABLE IF NOT EXISTS likes (
     id            BIGINT NOT NULL AUTO_INCREMENT,
     game_id       BIGINT NOT NULL,
     team_id       BIGINT NOT NULL,
     total_count   BIGINT NOT NULL DEFAULT 0,
     created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     CONSTRAINT pk_likes PRIMARY KEY (id),

    -- 한 경기에서 팀별 카운터는 한 행만 존재해야 함(UPSERT 전제)
    CONSTRAINT uk_likes_game_team UNIQUE (game_id, team_id),

    -- 외래키 (테이블명은 실제 스키마에 맞게 수정: games/teams)
    CONSTRAINT fk_likes_game FOREIGN KEY (game_id) REFERENCES games (game_id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_team FOREIGN KEY (team_id) REFERENCES teams (team_id) ON DELETE RESTRICT
    ) ENGINE=InnoDB;

CREATE TABLE like_windows (
      id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      game_id BIGINT NOT NULL,
      client_instance_id VARCHAR(64) NOT NULL,
      window_start_epoch_sec BIGINT NOT NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      CONSTRAINT uk_like_windows UNIQUE (game_id, client_instance_id, window_start_epoch_sec),
      CONSTRAINT fk_like_windows_game FOREIGN KEY (game_id) REFERENCES games (game_id) ON DELETE CASCADE
) ENGINE=InnoDB;
