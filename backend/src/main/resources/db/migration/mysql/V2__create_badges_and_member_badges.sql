-- 1. badges 테이블 생성
CREATE TABLE badges
(
    badge_id              BIGINT       NOT NULL AUTO_INCREMENT,
    badge_name            VARCHAR(255) NOT NULL UNIQUE,
    badge_description     VARCHAR(255),
    badge_condition_type  VARCHAR(50)  NOT NULL,
    badge_condition_value INT          NOT NULL,
    badge_achieved_rate   DOUBLE       NOT NULL,
    PRIMARY KEY (badge_id)
) ENGINE = InnoDB;

-- 2. member_badges 테이블 생성
CREATE TABLE member_badges
(
    member_badge_id BIGINT      NOT NULL AUTO_INCREMENT,
    badge_id        BIGINT      NOT NULL,
    member_id       BIGINT      NOT NULL,
    progress        DOUBLE      NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    deleted_at      DATETIME(6) NULL,
    PRIMARY KEY (member_badge_id),
    FOREIGN KEY (badge_id) REFERENCES badges (badge_id),
    FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE = InnoDB;

CREATE TABLE badge_update_queue
(
    badge_update_queue_id BIGINT NOT NULL,
    pending_count         BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (badge_update_queue_id)
) ENGINE = InnoDB;

ALTER TABLE members
    ADD COLUMN representative_badge_id BIGINT NULL,
    ADD CONSTRAINT fk_member_representative_badge
        FOREIGN KEY (representative_badge_id) REFERENCES badges (badge_id);
