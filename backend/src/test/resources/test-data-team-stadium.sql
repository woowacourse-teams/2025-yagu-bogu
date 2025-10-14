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
INSERT INTO stadiums (stadium_id, full_name, short_name, location, latitude, longitude, level)
VALUES (1, '챔피언스필드', '챔피언스필드', '광주', 35.1683, 126.8889, 'MAIN'),
       (2, '잠실야구장', '잠실구장', '잠실', 37.5121, 127.0710, 'MAIN'),
       (3, '고척스카이돔', '고척돔', '고척', 37.4982, 126.8676, 'MAIN'),
       (4, '수원KT위즈파크', '위즈파크', '수원', 37.2996, 126.9707, 'MAIN'),
       (5, '대구삼성라이온즈파크', '라이온즈파크', '대구', 35.8419, 128.6815, 'MAIN'),
       (6, '사직야구장', '사직구장', '부산', 35.1943, 129.0615, 'MAIN'),
       (7, '문학야구장', '랜더스필드', '인천', 37.4361, 126.6892, 'MAIN'),
       (8, '마산야구장', '엔씨파크', '마산', 35.2281, 128.6819, 'MAIN'),
       (9, '이글스파크', '볼파크', '대전', 36.3173, 127.4280, 'MAIN');

-- 3. 회원가입 뱃지 데이터
INSERT INTO badges (badge_name, badge_description, badge_policy, badge_threshold, badge_image_url)
VALUES ('리드오프', '회원가입한 회원', 'SIGN_UP', 1,
        'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/leadoff_500.png'),
       ('말문이 트이다', '첫 현장톡 작성', 'CHAT', 1,
        'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/open_mouth_500.png'),
       ('공포의 주둥아리', '현장톡 누적 100회', 'CHAT', 100,
        'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/terrible_mouth_500.png'),
       ('플레이볼', '첫 직관 인증', 'CHECK_IN', 1,
        'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/playball_500.png'),
       ('그랜드슬램', '9개 전구장 방문', 'GRAND_SLAM', 9,
        'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/grandslam_500.png');
