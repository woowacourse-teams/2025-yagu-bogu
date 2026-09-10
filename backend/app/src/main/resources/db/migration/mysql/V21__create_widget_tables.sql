-- widget_devices: 디바이스별 푸시 토큰 등록
-- deviceId를 PK로 사용 (앱이 생성한 UUID)
CREATE TABLE widget_devices (
    device_id   VARCHAR(255) NOT NULL,
    member_id   BIGINT       NOT NULL,
    platform    VARCHAR(10)  NOT NULL COMMENT 'IOS | ANDROID',
    push_token  VARCHAR(500) NOT NULL COMMENT 'iOS=push-to-start 토큰, Android=FCM 등록 토큰',
    app_version VARCHAR(20),
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (device_id),
    CONSTRAINT fk_widget_devices_member FOREIGN KEY (member_id) REFERENCES members (member_id)
);

-- widget_live_activities: iOS Live Activity 갱신 토큰 등록 (iOS 전용)
CREATE TABLE widget_live_activities (
    activity_id  VARCHAR(255) NOT NULL COMMENT 'ActivityKit이 부여한 활동 식별자',
    device_id    VARCHAR(255) NOT NULL,
    game_id      BIGINT       NOT NULL,
    update_token VARCHAR(500) NOT NULL COMMENT '활동별 갱신 토큰 (hex)',
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    PRIMARY KEY (activity_id),
    CONSTRAINT fk_widget_live_activities_device FOREIGN KEY (device_id) REFERENCES widget_devices (device_id),
    CONSTRAINT fk_widget_live_activities_game FOREIGN KEY (game_id) REFERENCES games (game_id)
);

-- widget_settings: 계정 단위 위젯 on/off 토글 (기본값 true)
CREATE TABLE widget_settings (
    member_id  BIGINT      NOT NULL,
    enabled    TINYINT(1)  NOT NULL DEFAULT 1,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (member_id),
    CONSTRAINT fk_widget_settings_member FOREIGN KEY (member_id) REFERENCES members (member_id)
);

-- widget_game_pushes: 경기별 START/END 푸시 발송 이력
-- 중복 발송 방지 및 더블헤더 지연 시작 추적용
CREATE TABLE widget_game_pushes (
    game_id       BIGINT NOT NULL,
    start_sent_at DATETIME(6),
    end_sent_at   DATETIME(6),
    PRIMARY KEY (game_id),
    CONSTRAINT fk_widget_game_pushes_game FOREIGN KEY (game_id) REFERENCES games (game_id)
);
