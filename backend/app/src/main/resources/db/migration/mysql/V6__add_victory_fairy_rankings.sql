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
