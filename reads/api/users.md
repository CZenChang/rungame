# Users API

Base URL：`/rungame/api/users`

---

## POST /register

註冊新帳號。

### Request

```json
{
  "username": "player1",
  "password": "secret123"
}
```

| 欄位 | 型別 | 必填 | 限制 |
|------|------|------|------|
| `username` | string | ✓ | 3–50 字元 |
| `password` | string | ✓ | 6–100 字元 |

### Response `201 Created`

```json
{
  "userId": 1,
  "username": "player1",
  "role": "USER",
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>",
  "tokenExpiresAt": "2026-06-08T10:00:00+08:00"
}
```

### 錯誤

| Status | 原因 |
|--------|------|
| `400` | 欄位驗證失敗（格式不符、必填未填） |
| `409` | username 已被使用 |

---

## POST /login

登入取得 token。

### Request

```json
{
  "username": "player1",
  "password": "secret123"
}
```

| 欄位 | 型別 | 必填 |
|------|------|------|
| `username` | string | ✓ |
| `password` | string | ✓ |

### Response `200 OK`

```json
{
  "userId": 1,
  "username": "player1",
  "role": "USER",
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>",
  "tokenExpiresAt": "2026-06-08T10:00:00+08:00"
}
```

### 錯誤

| Status | 原因 |
|--------|------|
| `400` | 欄位驗證失敗 |
| `401` | 帳號不存在或密碼錯誤 |
| `403` | 帳號已停用 |
