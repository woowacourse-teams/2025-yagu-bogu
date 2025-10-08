CREATE TABLE teams
(
    team_id    BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    short_name VARCHAR(255) NOT NULL,
    team_code  VARCHAR(255) NOT NULL,
    PRIMARY KEY (team_id),
    UNIQUE (name),
    UNIQUE (short_name),
    UNIQUE (team_code)
) ENGINE = InnoDB;

CREATE TABLE stadiums
(
    stadium_id BIGINT       NOT NULL AUTO_INCREMENT,
    full_name  VARCHAR(255) NOT NULL,
    short_name VARCHAR(255) NOT NULL,
    location   VARCHAR(255) NOT NULL,
    latitude   FLOAT(53)    NOT NULL,
    longitude  FLOAT(53)    NOT NULL,
    PRIMARY KEY (stadium_id),
    UNIQUE (full_name),
    UNIQUE (short_name)
) ENGINE = InnoDB;

CREATE TABLE members
(
    member_id  BIGINT                NOT NULL AUTO_INCREMENT,
    team_id    BIGINT,
    email      VARCHAR(255)          NOT NULL,
    nickname   VARCHAR(255)          NOT NULL,
    oauth_id   VARCHAR(255)          NOT NULL,
    provider   ENUM ('GOOGLE')       NOT NULL,
    role       ENUM ('ADMIN','USER') NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    PRIMARY KEY (member_id),
    FOREIGN KEY (team_id) REFERENCES teams (team_id)
) ENGINE = InnoDB;

CREATE TABLE games
(
    game_id             BIGINT       NOT NULL AUTO_INCREMENT,
    game_code           VARCHAR(255) NOT NULL,
    date                DATE         NOT NULL,
    start_at            TIME(6)      NOT NULL,
    stadium_id          BIGINT       NOT NULL,
    home_team_id        BIGINT       NOT NULL,
    away_team_id        BIGINT       NOT NULL,
    home_score          INT,
    away_score          INT,
    home_hits           INT,
    away_hits           INT,
    home_errors         INT,
    away_errors         INT,
    home_bases_on_balls INT,
    away_bases_on_balls INT,
    game_state          ENUM ('CANCELED','COMPLETED','LIVE','SCHEDULED'),
    PRIMARY KEY (game_id),
    UNIQUE (game_code),
    FOREIGN KEY (stadium_id) REFERENCES stadiums (stadium_id),
    FOREIGN KEY (home_team_id) REFERENCES teams (team_id),
    FOREIGN KEY (away_team_id) REFERENCES teams (team_id)
) ENGINE = InnoDB;

CREATE TABLE check_ins
(
    check_ins_id BIGINT NOT NULL AUTO_INCREMENT,
    game_id      BIGINT NOT NULL,
    member_id    BIGINT NOT NULL,
    team_id      BIGINT NOT NULL,
    PRIMARY KEY (check_ins_id),
    FOREIGN KEY (game_id) REFERENCES games (game_id),
    FOREIGN KEY (member_id) REFERENCES members (member_id),
    FOREIGN KEY (team_id) REFERENCES teams (team_id)
) ENGINE = InnoDB;

CREATE TABLE talks
(
    talk_id    BIGINT       NOT NULL AUTO_INCREMENT,
    game_id    BIGINT       NOT NULL,
    member_id  BIGINT       NOT NULL,
    content    VARCHAR(255) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (talk_id)
) ENGINE = InnoDB;

CREATE TABLE talk_reports
(
    talk_report_id BIGINT      NOT NULL AUTO_INCREMENT,
    talk_id        BIGINT      NOT NULL,
    reporter_id    BIGINT      NOT NULL,
    reported_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (talk_report_id),
    UNIQUE (talk_id, reporter_id)
) ENGINE = InnoDB;

CREATE TABLE refresh_tokens
(
    id         VARCHAR(36)           NOT NULL,
    member_id  BIGINT                NOT NULL,
    expires_at DATETIME(6)           NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE victory_fairy_rankings
(
    victory_fairy_ranking_id BIGINT      NOT NULL AUTO_INCREMENT,
    member_id                BIGINT      NOT NULL,
    score                    DOUBLE      NOT NULL DEFAULT 0,
    win_count                INT         NOT NULL DEFAULT 0,
    check_in_count           INT         NOT NULL DEFAULT 0,
    game_year                INT         NOT NULL,
    updated_at               DATETIME(6) NULL,
    PRIMARY KEY (victory_fairy_ranking_id),
    UNIQUE KEY uq_vfr_member_year (member_id, game_year),
    CONSTRAINT fk_vfr_member FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE = InnoDB;
