-- 선발 예고 투수 (게임센터 크롤링 출처, 경기예정 상태에서만 채워짐)
-- Bronze 재처리 대상이 아니라 games 테이블에 직접 반영함
ALTER TABLE games
    ADD COLUMN home_probable_pitcher VARCHAR(50) NULL,
    ADD COLUMN away_probable_pitcher VARCHAR(50) NULL;
