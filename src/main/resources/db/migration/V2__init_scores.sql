-- V2__init_scores.sql
-- 使用者分數資料表（每個 user 一筆，累計 / 最高分）
-- 注意：fk_user_id 對應 users(id)，但刻意不在 DB 建立外鍵約束，關聯僅由 JPA 維護

CREATE TABLE scores (
    id          BIGSERIAL        PRIMARY KEY,
    -- 對應 users.id，邏輯外鍵（DB 不建約束），UNIQUE 確保一個 user 只有一筆
    fk_user_id  BIGINT           NOT NULL UNIQUE,
    score       INT              NOT NULL DEFAULT 0,

    -- 時間戳記（由 JPA Auditing 維護）
    created_at  TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

-- 依 user 查詢
CREATE INDEX idx_scores_fk_user_id ON scores (fk_user_id);
-- 排行榜查詢
CREATE INDEX idx_scores_score      ON scores (score DESC);
