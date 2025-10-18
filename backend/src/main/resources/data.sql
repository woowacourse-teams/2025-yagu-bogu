-- 1. 팀 데이터
INSERT INTO teams (name, short_name, team_code, status)
VALUES ('KIA 타이거즈', 'KIA', 'HT', 'ACTIVE'),
       ('LG 트윈스', 'LG', 'LG', 'ACTIVE'),
       ('키움 히어로즈', '키움', 'WO', 'ACTIVE'),
       ('KT 위즈', 'KT', 'KT', 'ACTIVE'),
       ('삼성 라이온즈', '삼성', 'SS', 'ACTIVE'),
       ('롯데 자이언츠', '롯데', 'LT', 'ACTIVE'),
       ('SSG 랜더스', 'SSG', 'SK', 'ACTIVE'),
       ('NC 다이노스', 'NC', 'NC', 'ACTIVE'),
       ('한화 이글스', '한화', 'HH', 'ACTIVE'),
       ('두산 베어스', '두산', 'OB', 'ACTIVE'),
       ('SK 와이번스', 'SK', 'SK2', 'INACTIVE'),
       ('현대 유니콘스', '현대', 'HD', 'INACTIVE'),
       ('나눔 올스타', '나눔', 'NN', 'INACTIVE'),
       ('드림 올스타', '드림', 'DR', 'INACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name),
                        short_name=VALUES(short_name);

-- 2. 구장 데이터
INSERT INTO stadiums (stadium_id, full_name, short_name, location, latitude, longitude, level)
VALUES (1, '광주 기아 챔피언스필드', '챔피언스필드', '광주', 35.168139, 126.889111, 'MAIN'),
       (2, '잠실 야구장', '잠실구장', '잠실', 37.512150, 127.071976, 'MAIN'),
       (3, '고척 스카이돔', '고척돔', '고척', 37.498222, 126.867250, 'MAIN'),
       (4, '수원 KT 위즈파크', '위즈파크', '수원', 37.299759, 127.009781, 'MAIN'),
       (5, '대구 삼성 라이온즈파크', '라이온즈파크', '대구', 35.841111, 128.681667, 'MAIN'),
       (6, '사직야구장', '사직구장', '사직', 35.194077, 129.061584, 'MAIN'),
       (7, '인천 SSG 랜더스필드', '랜더스필드', '문학', 37.436778, 126.693306, 'MAIN'),
       (8, '창원 NC 파크', '엔씨파크', '창원', 35.222754, 128.582251, 'MAIN'),
       (9, '대전 한화생명 볼파크', '볼파크', '대전', 36.316589, 127.431211, 'MAIN'),
       (10, '울산 문수 야구장', '문수구장', '울산', 35.532334, 129.265575, 'SECONDARY'),
       (11, '월명종합경기장 야구장', '군산구장', '군산', 35.966360, 126.748161, 'SECONDARY'),
       (12, '청주 야구장', '청주구장', '청주', 36.638840, 127.470149, 'SECONDARY'),
       (13, '포항 야구장', '포항구장', '포항', 36.008273, 129.359410, 'SECONDARY'),
       (14, '한화생명 이글스파크', '이글스파크', '한밭', 36.317178, 127.429167, 'SECONDARY'),
       (15, '대구시민운동장 야구장', '시민운동장', '시민', 35.881162, 128.586371, 'SECONDARY'),
       (16, '무등 야구장', '무등야구장', '무등', 35.169165, 126.887245, 'SECONDARY'),
       (17, '마산 야구장', '마산야구장', '마산', 35.220855, 128.581050, 'SECONDARY'),
       (18, '숭의 야구장', '숭의야구장', '인천', 37.466591, 126.643239, 'SECONDARY'),
       (19, '삼성 라이온즈 볼파크', '라이온즈볼파크', '경산', 35.864844, 128.805667, 'SECONDARY')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),
                        short_name=VALUES(short_name),
                        location=VALUES(location),
                        latitude=VALUES(latitude),
                        longitude=VALUES(longitude);

-- 3. 멤버 정보
INSERT INTO members (member_id, team_id, nickname, email, provider, oauth_id, role, image_url, representative_badge_id)
VALUES (5000, 2, '엘지1', '엘지1@example.com', 'GOOGLE', 'sub-엘지1', 'USER', 'https://image.com/엘지1.png', null),
       (5001, 2, '엘지2', '엘지2@example.com', 'GOOGLE', 'sub-엘지2', 'USER', 'https://image.com/엘지2.png', null),
       (5002, 10, '두산2', '두산2@example.com', 'GOOGLE', 'sub-두산2', 'USER', 'https://image.com/두산2.png', null),
       (5003, 10, '두산3', '두산3@example.com', 'GOOGLE', 'sub-두산3', 'USER', 'https://image.com/두산3.png', null),
       (5004, 10, '두산4', '두산4@example.com', 'GOOGLE', 'sub-두산4', 'USER', 'https://image.com/두산4.png', null),
       (5005, 10, '두산5', '두산5@example.com', 'GOOGLE', 'sub-두산5', 'USER', 'https://image.com/두산5.png', null),
       (5006, 1, '기아1', '기아1@example.com', 'GOOGLE', 'sub-기아1', 'USER', 'https://image.com/기아1.png', null),
       (5007, 1, '기아2', '기아2@example.com', 'GOOGLE', 'sub-기아2', 'USER', 'https://image.com/기아2.png', null),
       (5008, 1, '기아3', '기아3@example.com', 'GOOGLE', 'sub-기아3', 'USER', 'https://image.com/기아3.png', null),
       (5009, 2, '엘지3', '엘지3@example.com', 'GOOGLE', 'sub-엘지3', 'USER', 'https://image.com/엘지3.png', null),
       (5010, 6, '롯데1', '롯데1@example.com', 'GOOGLE', 'sub-롯데1', 'USER', 'https://image.com/롯데1.png', null),
       (5011, 6, '롯데2', '롯데2@example.com', 'GOOGLE', 'sub-롯데2', 'USER', 'https://image.com/롯데2.png', null),
       (5012, 6, '롯데3', '롯데3@example.com', 'GOOGLE', 'sub-롯데3', 'USER', 'https://image.com/롯데3.png', null),
       (5013, 6, '롯데4', '롯데4@example.com', 'GOOGLE', 'sub-롯데4', 'USER', 'https://image.com/롯데4.png', null),
       (5014, 6, '롯데5', '롯데5@example.com', 'GOOGLE', 'sub-롯데5', 'USER', 'https://image.com/롯데5.png', null),
       (5015, 6, '롯데6', '롯데6@example.com', 'GOOGLE', 'sub-롯데6', 'USER', 'https://image.com/롯데6.png', null),
       (5016, 2, '엘지4', '엘지4@example.com', 'GOOGLE', 'sub-엘지4', 'USER', 'https://image.com/엘지4.png', null),
       (5017, 2, '엘지5', '엘지5@example.com', 'GOOGLE', 'sub-엘지5', 'USER', 'https://image.com/엘지5.png', null),
       (5018, 10, '두산6', '두산6@example.com', 'GOOGLE', 'sub-두산6', 'USER', 'https://image.com/두산6.png', null),
       (5019, 9, '한화1', '한화1@example.com', 'GOOGLE', 'sub-한화1', 'USER', 'https://image.com/한화1.png', null),
       (5020, 2, '엘지6', '엘지6@example.com', 'GOOGLE', 'sub-엘지6', 'USER', 'https://image.com/엘지6.png', null),
       (5021, 2, '엘지7', '엘지7@example.com', 'GOOGLE', 'sub-엘지7', 'USER', 'https://image.com/엘지7.png', null);

-- 4. 스코어보드 데이터
INSERT INTO score_boards (runs, hits, errors, bases_on_balls, inning_scores)
VALUES
    -- 경기 1 (HT vs OB), score_board_id: 1, 2
    (5, 8, 0, 4, '0,1,0,0,3,0,1,0,-,-,-'),   -- 홈팀 HT (ID: 1)
    (4, 7, 1, 3, '2,0,0,1,0,1,0,0,0,-,-'),   -- 원정팀 OB (ID: 2)
    -- 경기 2 (HT vs OB), score_board_id: 3, 4
    (5, 9, 0, 5, '1,1,1,1,1,0,0,0,0,-,-'),   -- HT (ID: 3)
    (4, 6, 1, 2, '0,0,0,0,2,0,2,0,0,-,-'),   -- 원정팀 OB (ID: 4)
    -- 경기 3 (LT vs LG), score_board_id: 5, 6
    (3, 5, 2, 3, '0,0,0,0,0,1,2,0,0,-,-'),   -- 홈팀 LT (ID: 5)
    (5, 10, 0, 6, '1,0,0,4,0,0,0,0,0,-,-'),  -- LG (ID: 6)
    -- 경기 4 (LG vs HT), score_board_id: 7, 8
    (10, 12, 0, 8, '5,0,0,0,5,0,0,0,0,-,-'), -- LG (ID: 7)
    (0, 3, 3, 2, '0,0,0,0,0,0,0,0,0,-,-'),   -- 원정팀 HT (ID: 8)
    -- 경기 5 (LG vs HT), score_board_id: 9, 10
    (10, 15, 1, 7, '3,0,1,0,2,0,4,0,0,-,-'), -- LG (ID: 9)
    (0, 4, 2, 1, '0,0,0,0,0,0,0,0,0,-,-');
-- 원정팀 HT (ID: 10)

-- 5. 경기 데이터
INSERT INTO games (stadium_id, home_team_id, away_team_id, date, start_at, game_code, home_score, away_score,
                   game_state, home_score_board_id, away_score_board_id, home_pitcher, away_pitcher)
VALUES (4, 10, 4, '2025-07-25', '18:30', '20250725SSKT0', NULL, NULL, 'SCHEDULED', NULL, NULL, NULL, NULL),
       (1, 10, 1, '2025-07-24', '18:30', '20250724LGHT0', 5, 4, 'COMPLETED', 1, 2, '양현종', '알칸타라'),
       (1, 1, 10, '2025-07-25', '18:30', '20250725HTOB0', 5, 4, 'COMPLETED', 3, 4, '이의리', '곽빈'),
       (6, 6, 2, '2025-07-25', '18:30', '20250725LTLG0', 3, 5, 'COMPLETED', 5, 6, '켈리', '반즈'),
       (2, 2, 1, '2025-07-25', '18:30', '20250725LGHT0', 10, 0, 'COMPLETED', 9, 10, '엔스', '오원석');

-- 6. 체크인 정보
INSERT INTO check_ins (member_id, game_id, team_id)
VALUES
    -- 경기 1
    (5001, 1, 2),
    (5002, 1, 10),
    (5004, 1, 10),
    (5005, 1, 10),
    (5006, 1, 10),
    (5007, 1, 1),
    (5008, 1, 1),
    (5009, 1, 1),
    (5010, 1, 2),
    (5011, 1, 6),

    -- 경기 2
    (5002, 2, 10),
    (5003, 2, 10),
    (5004, 2, 10),
    (5005, 2, 10),
    (5006, 2, 10),
    (5007, 2, 1),
    (5008, 2, 1),
    (5009, 2, 1),
    (5010, 2, 2),
    (5011, 2, 6),

    -- 경기 3
    (5012, 3, 6),
    (5013, 3, 6),
    (5014, 3, 6),
    (5015, 3, 6),
    (5016, 3, 6),
    (5017, 3, 2),
    (5018, 3, 2),
    (5019, 3, 10),
    (5020, 3, 9),
    (5021, 3, 2),

    -- 경기 5
    (5007, 5, 1),
    (5008, 5, 1),
    (5009, 5, 1),
    (5010, 5, 2),
    (5017, 5, 2),
    (5018, 5, 2),
    (5020, 5, 2),
    (5021, 5, 2);

-- badges
INSERT INTO badges (badge_name, badge_description, badge_policy, badge_threshold, badge_image_url)
VALUES ('리드오프',
        '리드오프 타자처럼, 이제 당신의 야구보구 여정이 시작됐어요! ⚾🎉\n앱을 처음 설치한 팬에게만 주어지는 특별한 시작의 배지랍니다.',
        'SIGN_UP', 1,
        'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/leadoff_500.png'),
       ('말문이 트이다',
        '첫 현장톡 작성 기념 배지예요! 💬\n\n처음으로 팬들과 대화를 나누며,\n직관 이야기에 당신의 목소리가 더해졌어요.',
        'CHAT', 1,
        'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/open_mouth_500.png'),
       ('공포의 주둥아리',
        '현장톡 100회 달성! 🎉\n\n''공포의 주둥아리''라 불리우는 당신,\n이제 모두가 인정하는 현장톡의 프린세스 👑',
        'CHAT', 100,
        'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/terrible_mouth_500.png'),
       ('플레이볼',
        '첫 직관 인증 기념 배지예요! 🎉\n\n이제 당신의 직관 여정이 본격적으로 시작돼요.\n\n앞으로도 다양한 순간들을 기록하며,\n멋진 야구 이야기를 만들어가 보세요!',
        'CHECK_IN', 1,
        'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/playball_500.png'),
       ('그랜드슬램',
        '모든 구장을 방문한 팬에게 주어지는 특별한 배지! 🏟️\n\n이제 당신은 진정한 직관 마스터! ✨\n\n그랜드 슬램처럼 화려하고 멋진 추억들이\n늘 당신의 직관 여정 속에 함께할 거예요.',
        'GRAND_SLAM', 9,
        'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/grandslam_500.png')
