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
INSERT INTO members (member_id, team_id, nickname, email, provider, oauth_id, role, image_url)
VALUES
    (5000, 2, '엘지1', '엘지1@example.com', 'GOOGLE', 'sub-엘지1', 'USER', 'https://image.com/엘지1.png'),
    (5001, 2, '엘지2', '엘지2@example.com', 'GOOGLE', 'sub-엘지2', 'USER', 'https://image.com/엘지2.png'),
    (5002, 10, '두산2', '두산2@example.com', 'GOOGLE', 'sub-두산2', 'USER', 'https://image.com/두산2.png'),
    (5003, 10, '두산3', '두산3@example.com', 'GOOGLE', 'sub-두산3', 'USER', 'https://image.com/두산3.png'),
    (5004, 10, '두산4', '두산4@example.com', 'GOOGLE', 'sub-두산4', 'USER', 'https://image.com/두산4.png'),
    (5005, 10, '두산5', '두산5@example.com', 'GOOGLE', 'sub-두산5', 'USER', 'https://image.com/두산5.png'),
    (5006, 1, '기아1', '기아1@example.com', 'GOOGLE', 'sub-기아1', 'USER', 'https://image.com/기아1.png'),
    (5007, 1, '기아2', '기아2@example.com', 'GOOGLE', 'sub-기아2', 'USER', 'https://image.com/기아2.png'),
    (5008, 1, '기아3', '기아3@example.com', 'GOOGLE', 'sub-기아3', 'USER', 'https://image.com/기아3.png'),
    (5009, 2, '엘지3', '엘지3@example.com', 'GOOGLE', 'sub-엘지3', 'USER', 'https://image.com/엘지3.png'),
    (5010, 6, '롯데1', '롯데1@example.com', 'GOOGLE', 'sub-롯데1', 'USER', 'https://image.com/롯데1.png'),
    (5011, 6, '롯데2', '롯데2@example.com', 'GOOGLE', 'sub-롯데2', 'USER', 'https://image.com/롯데2.png'),
    (5012, 6, '롯데3', '롯데3@example.com', 'GOOGLE', 'sub-롯데3', 'USER', 'https://image.com/롯데3.png'),
    (5013, 6, '롯데4', '롯데4@example.com', 'GOOGLE', 'sub-롯데4', 'USER', 'https://image.com/롯데4.png'),
    (5014, 6, '롯데5', '롯데5@example.com', 'GOOGLE', 'sub-롯데5', 'USER', 'https://image.com/롯데5.png'),
    (5015, 6, '롯데6', '롯데6@example.com', 'GOOGLE', 'sub-롯데6', 'USER', 'https://image.com/롯데6.png'),
    (5016, 2, '엘지4', '엘지4@example.com', 'GOOGLE', 'sub-엘지4', 'USER', 'https://image.com/엘지4.png'),
    (5017, 2, '엘지5', '엘지5@example.com', 'GOOGLE', 'sub-엘지5', 'USER', 'https://image.com/엘지5.png'),
    (5018, 10, '두산6', '두산6@example.com', 'GOOGLE', 'sub-두산6', 'USER', 'https://image.com/두산6.png'),
    (5019, 9, '한화1', '한화1@example.com', 'GOOGLE', 'sub-한화1', 'USER', 'https://image.com/한화1.png'),
    (5020, 2, '엘지6', '엘지6@example.com', 'GOOGLE', 'sub-엘지6', 'USER', 'https://image.com/엘지6.png'),
    (5021, 2, '엘지7', '엘지7@example.com', 'GOOGLE', 'sub-엘지7', 'USER', 'https://image.com/엘지7.png');


-- 4. 경기 정보
INSERT INTO games (game_id, stadium_id, home_team_id, away_team_id, date, start_at, game_code, home_score, away_score,
                   game_state)
VALUES (1, 1, 1, 10, '2025-07-24', '18:30', '20250724OBHT0', 5, 4, 'COMPLETED'),
       (2, 1, 1, 10, '2025-07-25', '18:30', '20250725OBHT0', 5, 4, 'COMPLETED'),
       (3, 6, 2, 6, '2025-07-25', '18:30', '20250725LTLG0', 3, 5, 'COMPLETED'),
       (4, 2, 2, 1, '2025-07-24', '18:30', '20250724HTLG0', 10, 0, 'COMPLETED'),
       (5, 2, 2, 1, '2025-07-25', '18:30', '20250725HTLG0', 10, 0, 'COMPLETED');
-- 5. 체크인 정보
INSERT INTO check_ins (member_id, game_id, team_id)
VALUES
    -- 경기 1
    (5001, 1, 2),
    (5002, 1, 10),
    (5003, 1, 10),
    (5004, 1, 10),
    (5005, 1, 10),
    (5006, 1, 1),
    (5007, 1, 1),
    (5008, 1, 1),
    (5009, 1, 2),
    (5010, 1, 6),

    -- 경기 2
    (5001, 2, 2),
    (5002, 2, 10),
    (5003, 2, 10),
    (5004, 2, 10),
    (5005, 2, 10),
    (5006, 2, 1),
    (5007, 2, 1),
    (5008, 2, 1),
    (5009, 2, 2),
    (5010, 2, 6),

    -- 경기 3
    (5011, 3, 6),
    (5012, 3, 6),
    (5013, 3, 6),
    (5014, 3, 6),
    (5015, 3, 6),
    (5016, 3, 2),
    (5017, 3, 2),
    (5018, 3, 10),
    (5019, 3, 9),
    (5001, 3, 2),

    (5006, 5, 1),
    (5007, 5, 1),
    (5008, 5, 1),
    (5009, 5, 2),
    (5016, 5, 2),
    (5017, 5, 2),
    (5020, 5, 2),
    (5021, 5, 2);
