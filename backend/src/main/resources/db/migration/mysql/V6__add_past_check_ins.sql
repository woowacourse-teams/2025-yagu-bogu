CREATE TABLE past_check_ins
(
    past_check_ins_id BIGINT NOT NULL AUTO_INCREMENT,
    game_id      BIGINT NOT NULL,
    member_id    BIGINT NOT NULL,
    team_id      BIGINT NOT NULL,
    PRIMARY KEY (past_check_ins_id),
    CONSTRAINT fk_past_checkins_game FOREIGN KEY (game_id) REFERENCES games (game_id),
    CONSTRAINT fk_past_checkins_member FOREIGN KEY (member_id) REFERENCES members (member_id),
    CONSTRAINT fk_past_checkins_team FOREIGN KEY (team_id) REFERENCES teams (team_id)
) ENGINE = InnoDB;
