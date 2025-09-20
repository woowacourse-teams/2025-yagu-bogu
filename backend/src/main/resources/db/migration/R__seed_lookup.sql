-- teams
INSERT INTO teams (name, short_name, team_code)
    VALUES ('KIA 타이거즈', 'KIA', 'HT'),
           ('LG 트윈스', 'LG', 'LG'),
           ('키움 히어로즈', '키움', 'WO'),
           ('KT 위즈', 'KT', 'KT'),
           ('삼성 라이온즈', '삼성', 'SS'),
           ('롯데 자이언츠', '롯데', 'LT'),
           ('SSG 랜더스', 'SSG', 'SK'),
           ('NC 다이노스', 'NC', 'NC'),
           ('한화 이글스', '한화', 'HH'),
           ('두산 베어스', '두산', 'OB')
        AS new_data
ON DUPLICATE KEY UPDATE name       = new_data.name,
                        short_name = new_data.short_name,
                        team_code  = new_data.team_code;

-- stadiums
INSERT INTO stadiums (stadium_id, full_name, short_name, location, latitude, longitude)
    VALUES (1, '광주 기아 챔피언스필드', '챔피언스필드', '광주', 35.168139, 126.889111),
           (2, '잠실 야구장', '잠실구장', '잠실', 37.512150, 127.071976),
           (3, '고척 스카이돔', '고척돔', '고척', 37.498222, 126.867250),
           (4, '수원 KT 위즈파크', '위즈파크', '수원', 37.299759, 127.009781),
           (5, '대구 삼성 라이온즈파크', '라이온즈파크', '대구', 35.841111, 128.681667),
           (6, '사직야구장', '사직구장', '부산', 35.194077, 129.061584),
           (7, '인천 SSG 랜더스필드', '랜더스필드', '인천', 37.436778, 126.693306),
           (8, '창원 NC 파크', '엔씨파크', '창원', 35.222754, 128.582251),
           (9, '대전 한화생명 볼파크', '볼파크', '대전', 36.316589, 127.431211)
        AS new_data
ON DUPLICATE KEY UPDATE full_name  = new_data.full_name,
                        short_name = new_data.short_name,
                        location   = new_data.location,
                        latitude   = new_data.latitude,
                        longitude  = new_data.longitude;

-- badges
INSERT INTO badges (badge_name, badge_description, badge_type, badge_threshold)
    VALUES ('리드오프', '회원가입한 회원', 'SIGN_UP', 1),
           ('말문이 트이다', '첫 현장톡 작성', 'CHAT', 1),
           ('공포의 주둥아리', '현장톡 누적 100회', 'CHAT', 5)
        AS new_data
ON DUPLICATE KEY UPDATE badge_name        = new_data.badge_name,
                        badge_description = new_data.badge_description,
                        badge_type        = new_data.badge_type,
                        badge_threshold   = new_data.badge_threshold;

INSERT INTO games (game_code, date, start_at, stadium_id, home_team_id, away_team_id,
                   home_score, away_score, home_score_board_id, away_score_board_id,
                   home_pitcher, away_pitcher, game_state)
    VALUES ('G001', '2025-09-15', '18:30:00', 1, 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, 'SCHEDULED')
        AS new_data
ON DUPLICATE KEY UPDATE date                = new_data.date,
                        start_at            = new_data.start_at,
                        stadium_id          = new_data.stadium_id,
                        home_team_id        = new_data.home_team_id,
                        away_team_id        = new_data.away_team_id,
                        home_score          = new_data.home_score,
                        away_score          = new_data.away_score,
                        home_score_board_id = new_data.home_score_board_id,
                        away_score_board_id = new_data.away_score_board_id,
                        home_pitcher        = new_data.home_pitcher,
                        away_pitcher        = new_data.away_pitcher,
                        game_state          = new_data.game_state;
