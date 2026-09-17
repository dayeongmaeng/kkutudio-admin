-- 첫 관리 대상 앱(꼬리) 등록. base_url/credential_env_key는 비밀값이 아니라
-- 연동 위치와 환경변수 "이름"일 뿐이므로 커밋 가능하다. 실제 API 키 값은
-- KKORI_ADMIN_API_KEY 환경변수로만 주입한다.
INSERT INTO managed_app (app_code, name, description, status, display_order)
VALUES ('kkori', '꼬리', NULL, 'DEVELOPMENT', 0);

INSERT INTO app_connection (app_id, base_url, auth_type, credential_env_key, timeout_ms)
SELECT id, 'http://kkori-api:8080/internal/admin', 'API_KEY', 'KKORI_ADMIN_API_KEY', 5000
FROM managed_app
WHERE app_code = 'kkori';
