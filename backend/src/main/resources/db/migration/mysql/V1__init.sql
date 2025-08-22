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
    stadium_id BIGINT        NOT NULL AUTO_INCREMENT,
    full_name  VARCHAR(255)  NOT NULL,
    short_name VARCHAR(255)  NOT NULL,
    location   VARCHAR(255)  NOT NULL,
    latitude   DECIMAL(9, 6) NOT NULL,
    longitude  DECIMAL(9, 6) NOT NULL,
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
    is_deleted BOOLEAN                        DEFAULT FALSE NOT NULL,
    image_url  VARCHAR(512)          NULL,
    created_at DATETIME(6)           NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)           NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6)           NULL,
    PRIMARY KEY (member_id),
    CONSTRAINT fk_members_team FOREIGN KEY (team_id) REFERENCES teams (team_id)
) ENGINE = InnoDB;

CREATE TABLE score_boards
(
    score_board_id BIGINT       NOT NULL AUTO_INCREMENT,
    runs           INT          NOT NULL DEFAULT 0,
    hits           INT          NOT NULL DEFAULT 0,
    errors         INT          NOT NULL DEFAULT 0,
    bases_on_balls INT          NOT NULL DEFAULT 0,
    inning_scores  VARCHAR(100) NOT NULL,
    PRIMARY KEY (score_board_id)
) ENGINE=InnoDB;

CREATE TABLE games
(
    game_id             BIGINT       NOT NULL AUTO_INCREMENT,
    game_code           VARCHAR(255) NOT NULL,
    date                DATE         NOT NULL,
    start_at            TIME         NOT NULL,
    stadium_id          BIGINT       NOT NULL,
    home_team_id        BIGINT       NOT NULL,
    away_team_id        BIGINT       NOT NULL,
    home_score          INT,
    away_score          INT,
    home_score_board_id BIGINT,
    away_score_board_id BIGINT,
    home_pitcher        VARCHAR(255),
    away_pitcher        VARCHAR(255),
    game_state          ENUM ('SCHEDULED','LIVE','COMPLETED','CANCELED'),
    PRIMARY KEY (game_id),
    UNIQUE (game_code),
    CONSTRAINT fk_games_stadium FOREIGN KEY (stadium_id) REFERENCES stadiums (stadium_id),
    CONSTRAINT fk_games_home FOREIGN KEY (home_team_id) REFERENCES teams (team_id),
    CONSTRAINT fk_games_away FOREIGN KEY (away_team_id) REFERENCES teams (team_id),
    CONSTRAINT fk_games_home_score_board FOREIGN KEY (home_score_board_id) REFERENCES score_boards (score_board_id),
    CONSTRAINT fk_games_away_score_board FOREIGN KEY (away_score_board_id) REFERENCES score_boards (score_board_id)
) ENGINE = InnoDB;

CREATE TABLE check_ins
(
    check_ins_id BIGINT NOT NULL AUTO_INCREMENT,
    game_id      BIGINT NOT NULL,
    member_id    BIGINT NOT NULL,
    team_id      BIGINT NOT NULL,
    PRIMARY KEY (check_ins_id),
    CONSTRAINT fk_checkins_game FOREIGN KEY (game_id) REFERENCES games (game_id),
    CONSTRAINT fk_checkins_member FOREIGN KEY (member_id) REFERENCES members (member_id),
    CONSTRAINT fk_checkins_team FOREIGN KEY (team_id) REFERENCES teams (team_id)
) ENGINE = InnoDB;

CREATE TABLE talks
(
    talk_id    BIGINT       NOT NULL AUTO_INCREMENT,
    game_id    BIGINT       NOT NULL,
    member_id  BIGINT       NOT NULL,
    content    VARCHAR(255) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    deleted_at DATETIME(6)  NULL,
    PRIMARY KEY (talk_id),
    CONSTRAINT fk_talks_game FOREIGN KEY (game_id) REFERENCES games (game_id),
    CONSTRAINT fk_talks_member FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE = InnoDB;

CREATE TABLE talk_reports
(
    talk_report_id BIGINT      NOT NULL AUTO_INCREMENT,
    talk_id        BIGINT      NOT NULL,
    reporter_id    BIGINT      NOT NULL,
    reported_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (talk_report_id),
    UNIQUE (talk_id, reporter_id),
    CONSTRAINT fk_report_talk FOREIGN KEY (talk_id) REFERENCES talks (talk_id),
    CONSTRAINT fk_report_member FOREIGN KEY (reporter_id) REFERENCES members (member_id)
) ENGINE = InnoDB;

CREATE TABLE refresh_tokens
(
    id         VARCHAR(36)           NOT NULL,
    member_id  BIGINT                NOT NULL,
    expires_at DATETIME(6)           NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_member FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE = InnoDB;

CREATE INDEX ix_teams_code ON teams (team_code);
