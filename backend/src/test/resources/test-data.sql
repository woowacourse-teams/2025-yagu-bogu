INSERT INTO teams (name, short_name, team_code)
VALUES ('기아 타이거즈', '기아', 'HT'),
       ('롯데 자이언츠', '롯데', 'LT'),
       ('삼성 라이온즈', '삼성', 'SS'),
       ('두산 베어스', '두산', 'OB');

INSERT INTO members (team_id, nickname, email, provider, sub, role, image)
VALUES (1, '포르', 'por@example.com', 'GOOGLE', 'sub-por', 'USER', 'https://image.com/por.png'),
    (2, '포라', 'pora@example.com', 'GOOGLE', 'sub-pora', 'USER', 'https://image.com/pora.png'),
    (3, '두리', 'doori@example.com', 'GOOGLE', 'sub-doori', 'USER', 'https://image.com/doori.png'),
    (NULL, '관리자', 'admin@example.com', 'GOOGLE', 'sub-admin', 'ADMIN', 'https://image.com/admin.png'),
    (1, '밍트', 'mint@example.com', 'GOOGLE', 'sub-mint', 'USER', 'https://image.com/mint.png'),
    (4, '우가', 'wooga@example.com', 'GOOGLE', 'sub-wooga', 'USER', 'https://image.com/wooga.png');

INSERT INTO stadiums (full_name, short_name, location, latitude, longitude)
VALUES ('잠실 야구장', '잠실구장', '잠실', 37.512192, 127.072055),
       ('고척 스카이돔', '고척돔', '고척', 37.498191, 126.867073),
       ('인천 SSG 랜더스필드', '랜더스필드', '인천', 37.437196, 126.693294),
       ('대전 한화생명 볼파크', '볼파크', '대전', 36.316589, 127.431211),
       ('광주 KIA 챔피언스필드', '챔피언스필드', '광주', 35.168282, 126.889138),
       ('대구 삼성라이온즈파크', '라이온즈파크', '대구', 35.841318, 128.681559),
       ('창원 NC파크', '엔씨파크', '창원', 35.222754, 128.582251),
       ('수원 KT위즈파크', '위즈파크', '수원', 37.299977, 127.009690),
       ('부산 사직야구장', '사직구장', '부산', 35.194146, 129.061497);

INSERT INTO games (stadium_id, home_team_id, away_team_id, date, start_at, home_score, away_score, game_code)
VALUES (1, 1, 2, '2025-07-21', '18:30', 10, 9, '20250721LGHT0'),
       (1, 1, 3, '2025-07-20', '18:30', 5, 5, '20250720WOHT0'),
       (1, 1, 3, '2025-07-19', '18:30', 10, 5, '20250719WOHT0'),
       (5, 1, 2, '2025-07-18', '18:30', 10, 9, '20250718LGHT0'),
       (5, 3, 1, '2025-07-17', '18:30', 1, 9, '20250717HTWO0'),
       (6, 1, 2, '2025-07-16', '18:30', 10, 9, '20250716LGHT0'),
       (1, 1, 3, '2024-05-05', '18:30', 10, 9, '20240505WOHT0');

INSERT INTO check_ins (member_id, game_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (1, 6),
       (2, 1),
       (3, 2),
       (5, 1);
