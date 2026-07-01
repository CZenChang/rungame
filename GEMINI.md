# Rungame 專案規範與指南

本文件定義了 `rungame` 專案的開發規範、架構決策與工作流程，供 Gemini CLI 參考。

## 技術棧 (Technology Stack)
- **Framework:** Spring Boot 4.0.6
- **Language:** Java 25
- **Database:** PostgreSQL (透過 Flyway 進行 Migration)
- **Build Tool:** Maven (使用 `mvnw` wrapper)
- **Testing:** Testcontainers (PostgreSQL)

## 開發規範 (Conventions)
- **語言:** 溝通與說明請使用 **繁體中文**。
- **Package 結構:** 以業務領域為主軸。
  - `{domain}/`: Controller, Service
  - `{domain}/dto/`: Request/Response 物件
  - `{domain}/repo/`: JPA Entity 與 Repository
  - `common/`: 跨領域共用元件
  - `config/`: 基礎設施配置
- **資料庫遷移:** 腳本位於 `src/main/resources/db/migration/`，命名格式 `V{版本}__{描述}.sql`。
- **異常處理 (Exception Handling):** 使用 `GlobalExceptionAdvice` 統一處理異常，並透過 `ApiResponse` 提供標準化回應格式。
  - 使用 `ErrorCode` 列舉定義錯誤代碼（如 9001, 1001 等）。
  - `ApiResponse` 的 `error` 欄位回傳自定義錯誤代碼，`message` 隱藏具體 Exception 類型。
  - **TraceId 追蹤**: 透過 `TraceIdFilter` 為每個請求生成唯一的 `traceId`，並透過 MDC 整合進日誌與回應中，方便全域追蹤。
- **防止重複提交 (Anti-Repeat Submission)**: 透過 `PreventRepeatFilter` 攔截 1 秒內的連續 POST/PUT/DELETE 請求。底層使用 Caffeine 快取（可擴展至 Redis），過期時間在 `application.yaml` 中配置。

## 常用指令 (Common Commands)
- **啟動應用程式 (Local Profile):**
  `.\mvnw.cmd spring-boot:run -Plocal -Dspring-boot.run.profiles=local`
- **執行測試:**
  `.\mvnw.cmd test`

## 注意事項 (Notes)
- 整合測試需要 Docker 環境 (Testcontainers)。專案使用 Testcontainers JDBC URL 的特殊語法，當連線字串為 `jdbc:tc:postgresql:17-alpine:///...` 且指定 driver 為 `org.testcontainers.jdbc.ContainerDatabaseDriver` 時，Testcontainers 會在幕後自動攔截連線請求，並啟動對應版本的 PostgreSQL 容器。
  - **重要**: 若使用 `@DataJpaTest` 進行 Repository 測試，因其預設會嘗試替換為嵌入式資料庫（如 H2），請務必加上 `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`，以確保測試能正確使用 Testcontainers 啟動的 PostgreSQL。
- `application.yaml` 管理基本設定，敏感資訊應使用環境變數。
- **gRPC 測試規範:** 對於 gRPC 服務端點（例如 [ScoreGrpcEndpoint](file:///c:/ceizer/git/rungame/src/main/java/com/dodognoman/rungame/score/ScoreGrpcEndpoint.java)），建議優先使用 JUnit 5 與 Mockito 進行輕量級的單元測試，模擬 `StreamObserver` 的行為。若需進行整合測試：
  1. 搭配 Testcontainers (`jdbc:tc:postgresql:17-alpine:///`) 自動啟動資料庫環境。
  2. 為了避免 Client 解析隨機 Port 的時序問題，建議使用固定 Port（`spring.grpc.server.port=9090`）並透過設定檔綁定位址（`spring.grpc.client.channels.local.address=static://127.0.0.1:9090`），或是在程式碼中直接使用 `ManagedChannelBuilder` 傳入 `@Value("${local.grpc.port}")` 手動建立連線。
- **JVM 警告訊息:** 執行應用程式或測試時，若出現 `WARNING: sun.misc.Unsafe::allocateMemory has been called by io.grpc.netty.shaded...` 等警告，這是因為 gRPC 底層的 Netty 框架使用了 Java 的內部 API 來優化記憶體效能。在較新的 Java 版本（如本專案的 Java 25）中，這類內部 API 已被標記為即將移除，因此會拋出警告。此警告 **不會影響程式正常運作，可以安全地忽略**。
