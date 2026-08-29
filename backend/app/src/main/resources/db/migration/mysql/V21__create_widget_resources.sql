CREATE TABLE widget_devices
(
    widget_device_id BIGINT        NOT NULL AUTO_INCREMENT,
    member_id        BIGINT        NOT NULL,
    platform         VARCHAR(20)   NOT NULL,
    device_id        VARCHAR(36)   NOT NULL,
    push_token       VARCHAR(4096) NOT NULL,
    app_version      VARCHAR(50)   NULL,
    enabled          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (widget_device_id),
    UNIQUE KEY uq_widget_devices_device_id (device_id),
    INDEX idx_widget_devices_member_platform (member_id, platform),
    CONSTRAINT fk_widget_devices_member FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE = InnoDB;

CREATE TABLE widget_live_activities
(
    widget_live_activity_id BIGINT        NOT NULL AUTO_INCREMENT,
    widget_device_id        BIGINT        NOT NULL,
    game_id                 BIGINT        NOT NULL,
    activity_id             VARCHAR(255)  NOT NULL,
    update_token            VARCHAR(4096) NOT NULL,
    created_at              DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (widget_live_activity_id),
    UNIQUE KEY uq_widget_live_activities_activity_id (activity_id),
    UNIQUE KEY uq_widget_live_activities_device_game (widget_device_id, game_id),
    INDEX idx_widget_live_activities_game (game_id),
    CONSTRAINT fk_widget_live_activities_device FOREIGN KEY (widget_device_id)
        REFERENCES widget_devices (widget_device_id) ON DELETE CASCADE,
    CONSTRAINT fk_widget_live_activities_game FOREIGN KEY (game_id) REFERENCES games (game_id)
) ENGINE = InnoDB;
