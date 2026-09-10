-- V22가 이미 적용된 환경에서도 Game의 Integer 매핑과 일치하도록 타입을 확장한다.
-- 기존 값과 NULL 허용 여부를 유지한다.
ALTER TABLE games
    MODIFY COLUMN current_inning INT NULL,
    MODIFY COLUMN balls          INT NULL,
    MODIFY COLUMN strikes        INT NULL,
    MODIFY COLUMN outs           INT NULL;
