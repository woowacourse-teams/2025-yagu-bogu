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
       ('두산 베어스', '두산', 'OB'),
       ('SK 와이번스', 'SK', 'SK2'),
       ('현대 유니콘스', '현대', 'HD'),
       ('나눔 올스타', '나눔', 'NN'),
       ('드림 올스타', '드림', 'DR')
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
        AS new_data
ON DUPLICATE KEY UPDATE badge_name        = new_data.badge_name,
                        badge_description = new_data.badge_description,
                        badge_policy      = new_data.badge_policy,
                        badge_threshold   = new_data.badge_threshold,
                        badge_image_url   = new_data.badge_image_url;
