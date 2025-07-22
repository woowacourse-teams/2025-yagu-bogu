INSERT INTO teams (name)
VALUES ('기아'),
       ('롯데'),
       ('삼성');

INSERT INTO members (team_id, nickname, role)
VALUES (1, '포르', 'USER'),
       (2, '포라', 'USER'),
       (3, '두리', 'USER'),
       (NULL, '관리자', 'ADMIN'),
       (1, '밍트', 'USER');

INSERT INTO stadiums (full_name, short_name, location)
VALUES ('광주 KIA 챔피언스필드', '챔피언스필드', '광주');

INSERT INTO games (stadium_id, home_team_id, away_team_id, date, home_score, away_score)
VALUES (1, 1, 2, '2025-07-21', 10, 9),
       (1, 1, 3, '2025-07-20', 5, 5),
       (1, 1, 3, '2025-07-19', 10, 5);

INSERT INTO check_ins (member_id, game_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (2, 1),
       (3, 2),
       (5, 1);
