# Database ERD

資料表的關聯總覽。欄位細節見 [schema.md](schema.md)，設計取捨見 [decisions.md](decisions.md)。

## ER 圖

```mermaid
erDiagram
    users ||--o| scores : "has (logical FK)"

    users {
        bigserial   id PK
        varchar20   username UK "NOT NULL"
        varchar255  password_hash "NOT NULL, bcrypt"
        text        access_token
        timestamptz token_expires_at
        inet        register_ip
        inet        last_login_ip
        varchar20   role "NOT NULL, DEFAULT USER"
        boolean     is_active "NOT NULL, DEFAULT TRUE"
        timestamptz last_login_at
        timestamptz created_at "NOT NULL, DEFAULT NOW()"
        timestamptz updated_at "NOT NULL, DEFAULT NOW()"
    }

    scores {
        bigserial   id PK
        bigint      fk_user_id UK "NOT NULL, 邏輯外鍵 → users.id"
        int         score "NOT NULL, DEFAULT 0"
        timestamptz created_at "NOT NULL, JPA Auditing"
        timestamptz updated_at "NOT NULL, JPA Auditing"
    }
```

## 關聯說明

| 關聯 | 基數 | 說明 |
|------|------|------|
| `users` → `scores` | 1 : 0..1 | 每個 user **最多一筆**分數（`scores.fk_user_id` 唯一）；首次更新分數時才建立該筆紀錄。 |

### 重點

- **不建實體外鍵約束**：`scores.fk_user_id` 是**邏輯外鍵**，DB 端不產生 FK constraint（JPA `@JoinColumn` 設 `ConstraintMode.NO_CONSTRAINT`），關聯僅由應用層維護。理由見 [decisions.md](decisions.md)。
- **每 user 一筆分數（覆蓋制）**：`fk_user_id` 加 `UNIQUE`，分數採覆蓋而非歷史累積。
- JPA Entity `Score` 以 `@ManyToOne(fetch = LAZY)` 對應 `User`，撈 `Score` 不會一併載入 `User`。
