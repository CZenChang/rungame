# Frontend Log API

Base URL：`/rungame/api/frontend-log`

讓前端把 log 寫進後端的獨立 log 檔（`logs/frontend.log`，每日滾動、保留 30 天）。
**內容格式由前端自行定義**，後端只負責落地，不解析欄位。

---

## POST /

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
