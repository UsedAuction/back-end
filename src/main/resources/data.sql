INSERT INTO member (member_id, pass_word, email, site_alarm, point, social, social_provider_id,
                    deleted_at, created_at, updated_at)
VALUES ('loginId',
        '1231231312',
        'test@example.com',
        false,
        100,
        'socialLogin',
        'socialProviderId',
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- 대분류 카테고리 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('남성의류', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('여성의류', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('키덜트', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('가전제품', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('도서제품', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('유아용품', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('굿즈', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('식품', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('뷰티', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('반려동물', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('가구', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('스포츠', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('생활용품', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('식물', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('악세사리', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('기타', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 남성의류 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('셔츠', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('바지', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('자켓', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('코트', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('액세서리', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 여성의류 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('드레스', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('블라우스', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('스커트', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('코트', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('가방', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 키덜트 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('피규어', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('레고', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('보드게임', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('모형', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('퍼즐', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 가전제품 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('노트북', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('스마트폰', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('TV', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('카메라', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('오디오', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 도서제품 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('소설', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('자기계발서', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('만화책', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('전문서적', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('전자책', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 유아용품 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('기저귀', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('아기옷', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('유모차', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('장난감', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('아기침대', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 굿즈 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('아이돌 굿즈', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('애니메이션 굿즈', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('게임 굿즈', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('영화 굿즈', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('기타 굿즈', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 식품 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('과자', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('음료수', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('반찬', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('즉석식품', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('과일', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 뷰티 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('화장품', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('헤어케어', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('바디케어', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('향수', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('네일', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 반려동물 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('사료', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('장난감', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('목욕용품', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('옷', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('훈련용품', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 가구 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('의자', 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('책상', 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('침대', 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('소파', 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('수납장', 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 스포츠 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('운동기구', 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('운동복', 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('신발', 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('야외활동 용품', 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('스포츠 용품', 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 생활용품 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('청소도구', 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('조리도구', 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('세제', 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('조명', 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('소형가전', 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 식물 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('화분', 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('씨앗', 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('비료', 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('정원도구', 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('인테리어 식물', 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 악세사리 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('목걸이', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('귀걸이', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('반지', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('팔찌', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('시계', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 기타 소분류 삽입
INSERT INTO category (category_name, parent_id, created_at, updated_at)
VALUES ('기타1', 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('기타2', 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('기타3', 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('기타4', 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('기타5', 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);