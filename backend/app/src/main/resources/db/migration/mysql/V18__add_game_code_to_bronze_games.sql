ALTER TABLE bronze_games_raw
    ADD COLUMN game_code VARCHAR(20) NULL AFTER raw_id,
    ADD UNIQUE KEY uk_bronze_game_code (game_code);
