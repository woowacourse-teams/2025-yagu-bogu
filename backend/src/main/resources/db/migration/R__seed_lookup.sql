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
ON DUPLICATE KEY UPDATE name=VALUES(name),
                        short_name=VALUES(short_name);

INSERT INTO stadiums (stadium_id, full_name, short_name, location, latitude, longitude)
VALUES (1, '광주 기아 챔피언스필드', '챔피언스필드', '광주', 35.168139, 126.889111),
       (2, '잠실 야구장', '잠실구장', '잠실', 37.512150, 127.071976),
       (3, '고척 스카이돔', '고척돔', '고척', 37.498222, 126.867250),
       (4, '수원 KT 위즈파크', '위즈파크', '수원', 37.299759, 127.009781),
       (5, '대구 삼성 라이온즈파크', '라이온즈파크', '대구', 35.841111, 128.681667),
       (6, '사직야구장', '사직구장', '부산', 35.194077, 129.061584),
       (7, '인천 SSG 랜더스필드', '랜더스필드', '인천', 37.436778, 126.693306),
       (8, '창원 NC 파크', '엔씨파크', '창원', 35.222754, 128.582251),
       (9, '대전 한화생명 볼파크', '볼파크', '대전', 36.316589, 127.431211),
       (10, '울산 문수 야구장', '문수구장', '울산', 35.532334, 129.265575),
       (11, '월명종합경기장 야구장', '군산구장', '군산', 35.966360, 126.748161),
       (12, '청주 야구장', '청주구장', '청주', 36.638840, 127.470149),
       (13, '포항 야구장', '포항구장', '포항', 36.008273, 129.359410)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),
                        short_name=VALUES(short_name),
                        location=VALUES(location),
                        latitude=VALUES(latitude),
                        longitude=VALUES(longitude);
