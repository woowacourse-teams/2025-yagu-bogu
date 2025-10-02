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
INSERT INTO stadiums (stadium_id, full_name, short_name, location, latitude, longitude, level)
    VALUES (1, '광주 기아 챔피언스필드', '챔피언스필드', '광주', 35.168139, 126.889111, 'MAIN'),
           (2, '잠실 야구장', '잠실구장', '잠실', 37.512150, 127.071976, 'MAIN'),
           (3, '고척 스카이돔', '고척돔', '고척', 37.498222, 126.867250, 'MAIN'),
           (4, '수원 KT 위즈파크', '위즈파크', '수원', 37.299759, 127.009781, 'MAIN'),
           (5, '대구 삼성 라이온즈파크', '라이온즈파크', '대구', 35.841111, 128.681667, 'MAIN'),
           (6, '사직야구장', '사직구장', '부산', 35.194077, 129.061584, 'MAIN'),
           (7, '인천 SSG 랜더스필드', '랜더스필드', '인천', 37.436778, 126.693306, 'MAIN'),
           (8, '창원 NC 파크', '엔씨파크', '창원', 35.222754, 128.582251, 'MAIN'),
           (9, '대전 한화생명 볼파크', '볼파크', '대전', 36.316589, 127.431211, 'MAIN'),
           (10, '울산 문수 야구장', '문수구장', '울산', 35.532334, 129.265575, 'SECONDARY'),
           (11, '월명종합경기장 야구장', '군산구장', '군산', 35.966360, 126.748161, 'SECONDARY'),
           (12, '청주 야구장', '청주구장', '청주', 36.638840, 127.470149, 'SECONDARY'),
           (13, '포항 야구장', '포항구장', '포항', 36.008273, 129.359410, 'SECONDARY')
        AS new_data
ON DUPLICATE KEY UPDATE full_name  = new_data.full_name,
                        short_name = new_data.short_name,
                        location   = new_data.location,
                        latitude   = new_data.latitude,
                        longitude  = new_data.longitude,
                        level      = new_data.level;

-- badges
INSERT INTO badges (badge_name, badge_description, badge_type, badge_threshold, badge_image_url)
    VALUES ('리드오프',
            '리드오프 타자처럼, 이제 당신의 야구보구 여정이 시작됐어요! ⚾🎉\n앱을 처음 설치한 팬에게만 주어지는 특별한 시작의 배지랍니다.',
            'SIGN_UP', 1,
            'https://github.com/user-attachments/assets/68f40c11-e0ac-4917-9cab-d482bd44bdea'),
           ('말문이 트이다',
            '첫 현장톡 작성 기념 배지예요! 💬\n\n처음으로 팬들과 대화를 나누며,\n직관 이야기에 당신의 목소리가 더해졌어요.',
            'CHAT', 1,
            'https://github.com/user-attachments/assets/7f6cc5ae-e4af-41c7-96f1-e531c661f771'),
           ('공포의 주둥아리',
            '현장톡 100회 달성! 🎉\n\n''공포의 주둥아리''라 불리우는 당신,\n이제 모두가 인정하는 현장톡의 프린세스 👑',
            'CHAT', 100,
            'https://github.com/user-attachments/assets/b393d494-7168-4c4a-821d-113db6f6d7f0'),
           ('플레이볼',
            '첫 직관 인증 기념 배지예요! 🎉\n\n이제 당신의 직관 여정이 본격적으로 시작돼요.\n\n앞으로도 다양한 순간들을 기록하며,\n멋진 야구 이야기를 만들어가 보세요!',
            'CHECK_IN', 1,
            'https://github.com/user-attachments/assets/36a27348-0870-4910-b106-c35319eb4ac6'),
           ('그랜드슬램',
            '모든 구장을 방문한 팬에게 주어지는 특별한 배지! 🏟️\n\n이제 당신은 진정한 직관 마스터! ✨\n\n그랜드 슬램처럼 화려하고 멋진 추억들이\n늘 당신의 직관 여정 속에 함께할 거예요.',
            'GRAND_SLAM', 9,
            'https://github.com/user-attachments/assets/7ef1ead4-78cf-472e-a610-48f9d0439ded')
        AS new_data
ON DUPLICATE KEY UPDATE badge_name        = new_data.badge_name,
                        badge_description = new_data.badge_description,
                        badge_type        = new_data.badge_type,
                        badge_threshold   = new_data.badge_threshold,
                        badge_image_url   = new_data.badge_image_url;

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
