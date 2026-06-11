# Rungame 專案規範與指南

本文件定義了 `rungame` 專案的開發規範、架構決策與工作流程，供 Gemini CLI 參考。

## 技術棧 (Technology Stack)
- **Framework:** Spring Boot 4.0.6 (支援 AOT + Native)
- **Language:** Java 25 (GraalVM Community Edition)
- **Database:** PostgreSQL (透過 Flyway 進行 Migration)
- **Build Tool:** Maven (使用 `mvnw` wrapper)
- **Native:** GraalVM Native Image (native-maven-plugin 0.10.6)
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

## 常用指令 (Common Commands)
- **啟動應用程式 (Local Profile):**
  `.\mvnw.cmd spring-boot:run -Plocal -Dspring-boot.run.profiles=local`
- **執行測試:**
  `.\mvnw.cmd test`
- **編譯 Native Binary:**
  `.\mvnw.cmd -Pnative -DskipTests package`

## 注意事項 (Notes)
- 整合測試需要 Docker 環境 (Testcontainers)。
- 進行 Native 編譯時，Windows 需要 Visual Studio 2022 的 C++ 工具鏈，並在 x64 Native Tools Command Prompt 下執行。
- `application.yaml` 管理基本設定，敏感資訊應使用環境變數。
