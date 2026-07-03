# Rungame API

所有 API 共用以下說明，個別端點僅補充差異。

---

## ApiResponse（共用回應結構）

除特別標註外，所有端點回應皆包在共用的 `ApiResponse` 結構中。`null` 欄位不會輸出（`@JsonInclude(NON_NULL)`）。

```json
{
  "error": 0,
  "message": "OK",
  "data": {},
  "path": "/rungame/api/...",
  "timestamp": "2026-06-23T14:32:10",

  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,

  "traceId": "a1b2c3..."
}
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| `error` | int | 錯誤代碼，`0` 表示成功 |
| `message` | string | 提示訊息，成功時通常為 `"OK"` |
| `data` | any | 業務資料（DTO、List、純值等），無資料時不輸出 |
| `path` | string | 請求路徑，通常僅錯誤時出現 |
| `timestamp` | datetime | 回應時間 |
| `page` / `size` / `totalElements` / `totalPages` | number | 分頁資訊，僅分頁查詢時出現 |
| `traceId` | string | 請求追蹤碼，用於對照後端 log |

> 以下各端點的 Response 範例僅列出 `data` 內容，實際會包在上述結構中。

### error code 對照表

`error` 欄位為應用層自訂代碼，`0` 表示成功；非 0 表示失敗，並依分類編號。前端應以 `error` 判斷結果，HTTP status 僅供參考。

| error | 名稱 | 意義 | 常見 HTTP status |
|-------|------|------|----------------|
| `0` | 成功 | 請求成功 | `200` / `201`  |
| `1001` | VALIDATION_ERROR | 請求參數格式不正確（`@Valid` 驗證失敗，`data` 帶各欄位錯誤訊息） | `400`          |
| `1002` | PARSE_ERROR | 請求格式解析失敗（JSON 格式錯誤、無法反序列化） | `400`          |
| `2001` | DATA_CONFLICT | 資料衝突或重複（唯一約束違反，如 username 重複） | `500`           |
| `2002` | RESOURCE_NOT_FOUND | 找不到指定的資源 | `404`          |
| `2003` | DATABASE_ERROR | 資料庫操作異常 | `500`          |
| `3001` | IO_ERROR | 系統讀寫異常 | `500`          |
| `4001` | AUTH_FAILED | 登入驗證失敗（帳密錯誤、token 無效或缺失） | `401`          |
| `4002` | ACCESS_DENIED | 權限不足，拒絕存取 | `403`          |
| `9001` | SYSTEM_ERROR | 系統發生未知錯誤 | `500`          |
| `9002` | RUNTIME_ERROR | 系統執行異常 | `500`          |
| `9003` | TOO_MANY_REQUESTS | 請求過於頻繁，請稍後再試 | `429`          |

編號分類：`1xxx` 參數與請求、`2xxx` 資料庫與資源、`3xxx` IO 與系統層、`4xxx` 身分驗證與權限、`9xxx` 通用系統。

---

## Token（身分驗證）

需要驗證的端點請在 **HTTP Header** 帶上 JWT：

```
Authorization: Bearer <accessToken>
```

`accessToken` 由 `/api/users/register` 或 `/api/users/login` 取得，使用者身分由 token 識別，無需另外傳 userId。

各端點是否需要 token 以下列標記表示：

- 🔒 **需 JWT**：必須帶有效 token，否則回 `401`。
- 🌐 **公開**：免帶 token。

---

# Users API

Base URL：`/rungame/api/users`

| 端點 | 驗證 |
|------|------|
| `POST /register` | 🌐 公開 |
| `POST /login` | 🌐 公開 |

## POST /register 🌐

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
| `username` | string | ✓ | 3–20 字元 |
| `password` | string | ✓ | 6–100 字元 |

### Response `201 Created`（`data`）

```json
{
  "userId": 1,
  "username": "player1",
  "role": "USER",
  "accessToken": "<jwt>",
  "tokenExpiresAt": "2026-06-08T10:00:00+08:00"
}
```

### username 已存在時的行為

> 帳號重複**不回錯誤**，而是回 `201`、`error: 0`，但 `data` 內所有欄位皆為 `null`。

```json
{
  "error": 0,
  "message": "OK",
  "data": {
    "userId": null,
    "username": null,
    "role": null,
    "accessToken": null,
    "tokenExpiresAt": null
  },
  "timestamp": "2026-06-24T10:00:00"
}
```

前端應以 **`data.accessToken !== null`** 判斷是否真的註冊成功，而非僅看 HTTP status 或 `error`。

### 錯誤

| Status | 原因 |
|--------|------|
| `400` | 欄位驗證失敗（格式不符、必填未填） |

---

## POST /login 🌐

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

### Response `200 OK`（`data`）

```json
{
  "userId": 1,
  "username": "player1",
  "role": "USER",
  "accessToken": "<jwt>",
  "tokenExpiresAt": "2026-06-08T10:00:00+08:00"
}
```

### 錯誤

| Status | 原因 |
|--------|------|
| `400` | 欄位驗證失敗 |
| `401` | 帳號不存在或密碼錯誤 |
| `403` | 帳號已停用 |

---

# Scores API

Base URL：`/rungame/api/scores`

每位使用者只有一筆分數（覆蓋制），使用者由 token 識別。

| 端點 | 驗證 |
|------|------|
| `PUT /me` | 🔒 需 JWT |
| `GET /me` | 🔒 需 JWT |
| `GET /leaderboard` | 🌐 公開 |
| `GET /leaderboard/stream` | 🌐 公開 |

## PUT /me 🔒

更新（覆蓋）目前登入者的分數，回傳更新後的分數。首次更新會自動建立紀錄。

### Request

```json
{
  "score": 120
}
```

| 欄位 | 型別 | 必填 | 限制 |
|------|------|------|------|
| `score` | int | ✓ | >= 0 |

### Response `200 OK`（`data`）

```json
120
```

`data` 為更新後的分數。

### 錯誤

| Status | 原因 |
|--------|------|
| `400` | 欄位驗證失敗（score 為負或格式不符） |
| `401` | 未帶 token 或 token 無效 |

---

## GET /me 🔒

查詢目前登入者的分數，尚無紀錄時回傳 `0`。

### Response `200 OK`（`data`）

```json
42
```

### 錯誤

| Status | 原因 |
|--------|------|
| `401` | 未帶 token 或 token 無效 |

---

## GET /leaderboard 🌐

排行榜，依分數由高到低排序，支援分頁。

### Query 參數

| 參數 | 型別 | 必填 | 預設 | 限制 | 說明 |
|------|------|------|------|------|------|
| `size` | int | ✗ | `10` | >= 1 | 每頁筆數 |
| `page` | int | ✗ | `0` | >= 0 | 頁碼，從 `0` 起算 |

範例：`GET /rungame/api/scores/leaderboard?size=20&page=1`

### Response `200 OK`（`data`）

```json
[
  { "username": "alice", "score": 300 },
  { "username": "bob", "score": 200 }
]
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| `username` | string | 使用者名稱 |
| `score` | int | 分數 |

### 錯誤

| Status | 原因 |
|--------|------|
| `400` | 參數驗證失敗（`size < 1` 或型別不符） |

---

## GET /leaderboard/stream 🌐

與 `GET /leaderboard` 相同的排行榜資料，但改以 **Server-Sent Events（SSE）** 逐筆串流回傳，供前端邊接收邊呈現。

- Content-Type：`text/event-stream`
- 逐筆送出 `LeaderboardEntry`，全部送完後補送一個空物件 `{}` 作為結束標記（避免瀏覽器 SSE 預設自動重連），隨後關閉連線。

### Query 參數

同 `GET /leaderboard`：

| 參數 | 型別 | 必填 | 預設 | 限制 | 說明 |
|------|------|------|------|------|------|
| `size` | int | ✗ | `10` | >= 1 | 每頁筆數 |
| `page` | int | ✗ | `0` | >= 0 | 頁碼，從 `0` 起算 |

### Response `200 OK`（event stream）

```
data:{"username":"alice","score":300}

data:{"username":"bob","score":200}

data:{}
```

> 每筆為一個 SSE `data:` 事件；最後的 `data:{}` 代表資料已送完。

### 錯誤

| Status | 原因 |
|--------|------|
| `400` | 參數驗證失敗（`size < 1` 或型別不符） |

---

# Frontend Log API

Base URL：`/rungame/api/frontend-log`

讓前端把 log 寫進後端的獨立 log 檔（`logs/frontend.log`，每日滾動、保留 30 天）。
**內容格式由前端自行定義**，後端只負責落地，不解析欄位。

| 端點 | 驗證 |
|------|------|
| `POST /` | 🌐 公開 |

## POST / 🌐

寫入一筆前端 log。

### Request

- Content-Type：`text/plain`
- Body：純字串（格式前端自訂，例如自己拼成 JSON 字串或 `level|path|message`）

```
ERROR | /home | TypeError: undefined is not a function
```

| 項目 | 說明 |
|------|------|
| Body | 純文字字串，可為空 |

### 後端處理（安全底線）

| 處理 | 說明 |
|------|------|
| 限長 | 超過 **4000** 字元截斷，結尾補 `...(truncated)`，避免灌爆磁碟 |
| 清洗 | 移除換行/Tab/控制字元（防 log injection，攻擊者塞 `\n` 偽造整行 log） |
| 補充 | 後端自動加上來源 `ip` 與 `traceId`（取自 MDC），前端無需傳 |

落地格式範例：

```
2026-06-13 14:32:10 [a1b2c3...] ip=203.0.113.5 | ERROR | /home | TypeError...
```

### Response `200 OK`

```json
{
  "error": 0,
  "message": "OK",
  "timestamp": "2026-06-13T14:32:10",
  "traceId": "a1b2c3..."
}
```

### 備註

- 端點受全域 `PreventRepeatFilter` 保護：同 IP + 同路徑於極短時間內重複會回 `429`。
- 寫檔為**非同步**（logback AsyncAppender），不阻塞請求。
