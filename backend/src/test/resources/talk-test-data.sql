-- 1. 팀 생성 (ID 명시)
INSERT INTO teams (team_id, name, short_name, team_code)
VALUES (1, '롯데자이언츠', '롯데', 'LT');
INSERT INTO teams (team_id, name, short_name, team_code)
VALUES (2, '한화이글스', '한화', 'HH');

-- 2. 멤버 생성 (ID 명시, team_id는 위에서 명시한 ID 참조)
INSERT INTO members (member_id, team_id, nickname, role)
VALUES (1, 1, '포라', 'USER');
INSERT INTO members (member_id, team_id, nickname, role)
VALUES (2, 2, '누구', 'USER');

-- 3. 구장 생성 (ID 명시)
INSERT INTO stadiums (stadium_id, full_name, short_name, location, latitude, longitude)
VALUES (1, '사직야구장', '사직구장', '부산', 35.1943, 129.0615);

-- 4. 경기 생성 (ID 명시, stadium_id, home_team_id, away_team_id는 위에서 명시한 ID 참조)
INSERT INTO games (game_id, stadium_id, home_team_id, away_team_id, date, home_score, away_score)
VALUES (1, 1, 1, 2, '2025-07-25', 10, 0);

-- 5. 톡 52개 생성
-- 기준 시간: 2025-07-25 15:00:00
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 1', '2025-07-25 15:00:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 2', '2025-07-25 15:01:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 3', '2025-07-25 15:02:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 4', '2025-07-25 15:03:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 5', '2025-07-25 15:04:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 6', '2025-07-25 15:05:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 7', '2025-07-25 15:06:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 8', '2025-07-25 15:07:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 9', '2025-07-25 15:08:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 10', '2025-07-25 15:09:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 11', '2025-07-25 15:10:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 12', '2025-07-25 15:11:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 13', '2025-07-25 15:12:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 14', '2025-07-25 15:13:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 15', '2025-07-25 15:14:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 16', '2025-07-25 15:15:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 17', '2025-07-25 15:16:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 18', '2025-07-25 15:17:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 19', '2025-07-25 15:18:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 20', '2025-07-25 15:19:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 21', '2025-07-25 15:20:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 22', '2025-07-25 15:21:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 23', '2025-07-25 15:22:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 24', '2025-07-25 15:23:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 25', '2025-07-25 15:24:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 26', '2025-07-25 15:25:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 27', '2025-07-25 15:26:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 28', '2025-07-25 15:27:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 29', '2025-07-25 15:28:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 30', '2025-07-25 15:29:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 31', '2025-07-25 15:30:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 32', '2025-07-25 15:31:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 33', '2025-07-25 15:32:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 34', '2025-07-25 15:33:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 35', '2025-07-25 15:34:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 36', '2025-07-25 15:35:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 37', '2025-07-25 15:36:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 38', '2025-07-25 15:37:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 39', '2025-07-25 15:38:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 40', '2025-07-25 15:39:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 41', '2025-07-25 15:40:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 42', '2025-07-25 15:41:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 43', '2025-07-25 15:42:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 44', '2025-07-25 15:43:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 45', '2025-07-25 15:44:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 46', '2025-07-25 15:45:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 47', '2025-07-25 15:46:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 48', '2025-07-25 15:47:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 49', '2025-07-25 15:48:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 50', '2025-07-25 15:49:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 1, '메세지 51', '2025-07-25 15:50:00');
INSERT INTO talks (game_id, member_id, content, created_at)
VALUES (1, 2, '메세지 52', '2025-07-25 15:51:00');
