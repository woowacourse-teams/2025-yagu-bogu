ALTER TABLE victory_fairy_rankings
    ADD INDEX idx_victory_fairy_rankings_year_score (game_year, score DESC);
