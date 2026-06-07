-- V1__init_users.sql
-- 使用者資料表初始化

CREATE TABLE users (
    id               BIGSERIAL        PRIMARY KEY,
    username         VARCHAR(50)      NOT NULL UNIQUE,
    -- 密碼（儲存 bcrypt hash，不存明文）
    password_hash    VARCHAR(255)     NOT NULL,
    -- Token
    access_token     TEXT,
    refresh_token    TEXT,
    token_expires_at TIMESTAMPTZ,

    -- IP 紀錄（PostgreSQL 原生 INET 型別）
    register_ip      INET,
    last_login_ip    INET,

    -- 狀態與角色
    role             VARCHAR(20)      NOT NULL DEFAULT 'USER',  -- USER / ADMIN
    is_active        BOOLEAN          NOT NULL DEFAULT TRUE,

    -- 時間戳記
    last_login_at    TIMESTAMPTZ,
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

-- updated_at 自動更新 trigger
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 常用查詢索引
CREATE INDEX idx_users_username    ON users (username);
CREATE INDEX idx_users_access_token ON users (access_token) WHERE access_token IS NOT NULL;
