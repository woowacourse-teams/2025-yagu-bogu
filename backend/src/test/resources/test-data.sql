INSERT INTO teams (name)
VALUES ('기아'),
       ('롯데'),
       ('삼성'),
       ('두산');

INSERT INTO members (team_id, nickname, role)
VALUES (1, '포르', 'USER'),
       (2, '포라', 'USER'),
       (3, '두리', 'USER'),
       (NULL, '관리자', 'ADMIN'),
       (1, '밍트', 'USER'),
       (4, '우가', 'USER');

INSERT INTO stadiums (full_name, short_name, location)
VALUES ('잠실 야구장', '잠실구장', '잠실'),
       ('고척 스카이돔', '고척돔', '고척'),
       ('인천 SSG 랜더스필드', '랜더스필드', '인천'),
       ('대전 한화생명 볼파크', '볼파크', '대전'),
       ('광주 KIA 챔피언스필드', '챔피언스필드', '광주'),
       ('대구 삼성라이온즈파크', '라이온즈파크', '대구'),
       ('창원 NC파크', '엔씨파크', '창원'),
       ('수원 KT위즈파크', '위즈파크', '수원'),
       ('부산 사직야구장', '사직구장', '부산');

INSERT INTO games (stadium_id, home_team_id, away_team_id, date, home_score, away_score)
VALUES (1, 1, 2, '2025-07-21', 10, 9),
       (1, 1, 3, '2025-07-20', 5, 5),
       (1, 1, 3, '2025-07-19', 10, 5),
       (5, 1, 2, '2025-07-18', 10, 9),
       (5, 3, 1, '2025-07-17', 1, 9),
       (6, 1, 2, '2025-07-16', 10, 9),
       (1, 1, 3, '2024-05-05', 10, 9);

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
