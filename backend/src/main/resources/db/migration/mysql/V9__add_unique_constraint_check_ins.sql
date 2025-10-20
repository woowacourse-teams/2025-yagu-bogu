ALTER TABLE check_ins
    ADD CONSTRAINT uk_check_ins_member_game
        UNIQUE (game_id, member_id);
