-- 1. badges 테이블 생성
CREATE TABLE badges
(
    badge_id          BIGINT       NOT NULL AUTO_INCREMENT,
    badge_name        VARCHAR(255) NOT NULL UNIQUE,
    badge_description VARCHAR(255),
    badge_type        VARCHAR(50)  NOT NULL,
    badge_threshold   INT          NOT NULL,
    PRIMARY KEY (badge_id)
) ENGINE = InnoDB;

-- 2. member_badges 테이블 생성
CREATE TABLE member_badges
(
    member_badge_id BIGINT      NOT NULL AUTO_INCREMENT,
    badge_id        BIGINT      NOT NULL,
    member_id       BIGINT      NOT NULL,
    progress        INT         NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    deleted_at      DATETIME(6) NULL,
    PRIMARY KEY (member_badge_id),
    FOREIGN KEY (badge_id) REFERENCES badges (badge_id),
    FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE = InnoDB;

ALTER TABLE members
    ADD COLUMN representative_badge_id BIGINT NULL,
    ADD CONSTRAINT fk_member_representative_badge
        FOREIGN KEY (representative_badge_id) REFERENCES badges (badge_id);
