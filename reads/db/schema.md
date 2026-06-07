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
