-- 회원 생성
INSERT INTO member (member_id, pass_word, email, site_alarm, point, social, social_provider_id,
                    deleted_at, created_at, updated_at)
VALUES ('test', '1231231312', 'test@example.com', false, 100,
        'socialLogin', 'socialProviderId', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 카테고리 생성
insert into category (category_name, parent_id, created_at, updated_at, deleted_at)
values ('category1', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null);
insert into category (category_name, parent_id, created_at, updated_at, deleted_at)
values ('category2', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null);