-- 1. 팀 데이터
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
       ('두산 베어스', '두산', 'OB');

-- 2. 구장 데이터
INSERT INTO stadiums (stadium_id, full_name, short_name, location, latitude, longitude)
VALUES (1, '챔피언스필드', '챔피언스필드', '광주', 35.1683, 126.8889),
       (2, '잠실야구장', '잠실구장', '잠실', 37.5121, 127.0710),
       (3, '고척스카이돔', '고척돔', '고척', 37.4982, 126.8676),
       (4, '수원KT위즈파크', '위즈파크', '수원', 37.2996, 126.9707),
       (5, '대구삼성라이온즈파크', '라이온즈파크', '대구', 35.8419, 128.6815),
       (6, '사직야구장', '사직구장', '부산', 35.1943, 129.0615),
       (7, '문학야구장', '랜더스필드', '인천', 37.4361, 126.6892),
       (8, '마산야구장', '엔씨파크', '마산', 35.2281, 128.6819),
       (9, '이글스파크', '볼파크', '대전', 36.3173, 127.4280);

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
