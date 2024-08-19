-- 회원 생성
INSERT INTO member (member_id, pass_word, email, site_alarm, point, social, social_provider_id,
                    deleted_at, created_at, updated_at)
VALUES ('test', '1231231312', 'test@example.com', false, 100,
        'socialLogin', 'socialProviderId', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);