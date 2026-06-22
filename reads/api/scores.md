# Scores API

Base URL：`/rungame/api/scores`

每位使用者只有一筆分數（覆蓋制）。除特別標註外皆需 JWT（`Authorization: Bearer <accessToken>`），使用者由 token 識別。
回應皆包在共用的 `ApiResponse` 結構（`error` / `message` / `data` / `traceId` …），以下僅列出 `data` 內容。

---

## PUT /me

更新（覆蓋）目前登入者的分數，回傳更新後的分數。首次更新會自動建立紀錄。

🔒 需 JWT

### Request

```json
{
  "score": 120
}
```

| 欄位 | 型別 | 必填 | 限制 |
|------|------|------|------|
| `score` | int | ✓ | >= 0 |

### Response `200 OK`

```json
{
  "data": 120
}
```

`data` 為更新後的分數。

### 錯誤

| Status | 原因 |
|--------|------|
| `400` | 欄位驗證失敗（score 為負或格式不符） |
| `401` | 未帶 token 或 token 無效 |

---

## GET /me

查詢目前登入者的分數，尚無紀錄時回傳 `0`。

🔒 需 JWT

### Response `200 OK`

```json
{
  "data": 42
}
```

### 錯誤

| Status | 原因 |
|--------|------|
| `401` | 未帶 token 或 token 無效 |

---

## GET /leaderboard

排行榜前十名，依分數由高到低排序。

🌐 公開（免帶 token）

### Response `200 OK`

```json
{
  "data": [
    { "username": "alice", "score": 300 },
    { "username": "bob", "score": 200 }
  ]
}
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| `username` | string | 使用者名稱 |
| `score` | int | 分數 |
