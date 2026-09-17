-- =====================================================================
-- app_admin DB 초기 스키마 (PostgreSQL 16 / Flyway)
-- 원칙
--  - 회원·통계 데이터는 저장하지 않음
--    (각 앱 API의 /internal/admin/* 에서 실시간 조회)
--  - 앱 식별자는 managed_app.app_code ('kkori' 등)를 API의 appId로 사용
--  - 시각은 모두 timestamptz, 상태값은 varchar + CHECK (Java enum 매핑)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 관리자
-- ---------------------------------------------------------------------
CREATE TABLE admin_user (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email                VARCHAR(255) NOT NULL UNIQUE,
    password_hash        VARCHAR(255) NOT NULL,
    name                 VARCHAR(100) NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                         CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED')),
    failed_login_count   INT          NOT NULL DEFAULT 0,
    locked_until         TIMESTAMPTZ,
    last_login_at        TIMESTAMPTZ,
    last_login_ip        VARCHAR(45),
    password_changed_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 로그인 시도 이력 (실패 횟수 제한, 이상 접근 확인용)
CREATE TABLE admin_login_history (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_user_id    BIGINT REFERENCES admin_user (id),   -- 존재하지 않는 이메일 시도는 NULL
    attempted_email  VARCHAR(255) NOT NULL,
    success          BOOLEAN      NOT NULL,
    failure_reason   VARCHAR(50),                         -- BAD_CREDENTIALS, LOCKED ...
    ip               VARCHAR(45),
    user_agent       VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_login_history_created ON admin_login_history (created_at DESC);

-- ---------------------------------------------------------------------
-- 2. 관리 대상 앱
-- ---------------------------------------------------------------------
CREATE TABLE managed_app (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app_code       VARCHAR(50)  NOT NULL UNIQUE,          -- 'kkori'
    name           VARCHAR(100) NOT NULL,                 -- '꼬리'
    description    VARCHAR(500),
    icon_url       VARCHAR(500),
    status         VARCHAR(20)  NOT NULL DEFAULT 'DEVELOPMENT'
                   CHECK (status IN ('DEVELOPMENT', 'OPERATING', 'SUSPENDED', 'CLOSED')),
    display_order  INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 플랫폼별 정보 (iOS / Android / Web)
CREATE TABLE app_platform (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app_id                 BIGINT       NOT NULL REFERENCES managed_app (id),
    platform               VARCHAR(20)  NOT NULL
                           CHECK (platform IN ('IOS', 'ANDROID', 'WEB')),
    bundle_id              VARCHAR(255),                  -- 패키지명 / 번들 ID
    store_url              VARCHAR(500),
    latest_version         VARCHAR(30),
    min_supported_version  VARCHAR(30),
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (app_id, platform)
);

-- 앱 API 연결 정보 (비밀값은 저장하지 않고 환경변수 이름만 저장)
CREATE TABLE app_connection (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app_id                  BIGINT       NOT NULL UNIQUE REFERENCES managed_app (id),
    base_url                VARCHAR(500) NOT NULL,        -- http://kkori-api:8080/internal/admin
    auth_type               VARCHAR(20)  NOT NULL DEFAULT 'API_KEY'
                            CHECK (auth_type IN ('API_KEY', 'HMAC')),
    credential_env_key      VARCHAR(100) NOT NULL,        -- 'KKORI_ADMIN_API_KEY'
    timeout_ms              INT          NOT NULL DEFAULT 5000,
    enabled                 BOOLEAN      NOT NULL DEFAULT TRUE,
    last_health_status      VARCHAR(20)
                            CHECK (last_health_status IN ('UP', 'DOWN', 'UNKNOWN')),
    last_health_checked_at  TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- 3. 관리자 작업 이력 (감사 로그, 수정·삭제하지 않는 append-only)
-- ---------------------------------------------------------------------
CREATE TABLE audit_log (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_user_id   BIGINT       NOT NULL REFERENCES admin_user (id),
    app_id          BIGINT       REFERENCES managed_app (id),   -- 앱 무관 작업은 NULL
    action          VARCHAR(50)  NOT NULL,        -- MEMBER_VIEW, MEMBER_SUSPEND, MEMBER_FORCE_LOGOUT, APP_UPDATE ...
    target_type     VARCHAR(30)  NOT NULL,        -- MEMBER, APP, APP_PLATFORM, APP_CONNECTION
    target_id       VARCHAR(100),                 -- 앱 측 회원 ID 등
    reason          VARCHAR(500),                 -- 정지 사유 등
    before_value    JSONB,                        -- 개인정보는 마스킹 후 저장
    after_value     JSONB,
    result          VARCHAR(20)  NOT NULL CHECK (result IN ('SUCCESS', 'FAILURE')),
    error_message   TEXT,
    ip              VARCHAR(45),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_created ON audit_log (created_at DESC);
CREATE INDEX idx_audit_target  ON audit_log (app_id, target_type, target_id);

-- ---------------------------------------------------------------------
-- 4. 세션 (Spring Session JDBC)
-- spring-session-jdbc jar의 org/springframework/session/jdbc/schema-postgresql.sql
-- 내용을 V2__spring_session.sql 로 복사해 사용 (spring.session.jdbc.initialize-schema=never)
-- ---------------------------------------------------------------------
