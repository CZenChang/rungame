# Database Schema

## users

由 `V1__init_users.sql` 建立。

| 欄位 | 型別 | 限制 | 說明 |
|------|------|------|------|
| `id` | `BIGSERIAL` | PK | 自增主鍵 |
| `username` | `VARCHAR(50)` | NOT NULL, UNIQUE | 使用者名稱 |
| `password_hash` | `VARCHAR(255)` | NOT NULL | bcrypt hash，不存明文 |
| `access_token` | `TEXT` | | JWT / Session access token |
| `refresh_token` | `TEXT` | | Refresh token |
| `token_expires_at` | `TIMESTAMPTZ` | | Token 到期時間 |
| `register_ip` | `INET` | | 註冊時的 IP（PostgreSQL 原生 INET 型別） |
| `last_login_ip` | `INET` | | 最後登入 IP |
| `role` | `VARCHAR(20)` | NOT NULL, DEFAULT `'USER'` | 角色，`USER` 或 `ADMIN` |
| `is_active` | `BOOLEAN` | NOT NULL, DEFAULT `TRUE` | 帳號是否啟用 |
| `last_login_at` | `TIMESTAMPTZ` | | 最後登入時間 |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | 建立時間 |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | 更新時間，由 trigger 自動維護 |

### 索引

| 索引名稱 | 欄位 | 條件 |
|----------|------|------|
| `idx_users_email` | `email` | |
| `idx_users_username` | `username` | |
| `idx_users_access_token` | `access_token` | `WHERE access_token IS NOT NULL` |

### Trigger

`trg_users_updated_at`：每次 UPDATE 前自動將 `updated_at` 設為 `NOW()`。

## scores

由 `V2__init_scores.sql` 建立。記錄每個 user 的分數，**每個 user 一筆**（方案 B）。

| 欄位 | 型別 | 限制 | 說明 |
|------|------|------|------|
| `id` | `BIGSERIAL` | PK | 自增主鍵 |
| `fk_user_id` | `BIGINT` | NOT NULL, UNIQUE | 邏輯外鍵，對應 `users(id)`；**DB 不建外鍵約束**，關聯僅由 JPA 維護 |
| `score` | `INT` | NOT NULL, DEFAULT `0` | 使用者分數 |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | 建立時間，由 JPA Auditing 維護 |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | 更新時間，由 JPA Auditing 維護 |

### 索引

| 索引名稱 | 欄位 | 條件 |
|----------|------|------|
| `idx_scores_fk_user_id` | `fk_user_id` | |
| `idx_scores_score` | `score DESC` | 排行榜查詢 |

### 備註

- JPA Entity `Score` 以 `@ManyToOne(fetch = LAZY)` 對應 `User`，撈 `Score` 不會一併撈出 `User` 內容。
- `@JoinColumn` 設 `ConstraintMode.NO_CONSTRAINT`，DB 端不產生實體外鍵約束。
