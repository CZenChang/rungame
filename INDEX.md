# 專案檔案索引 (INDEX.md)

本文件依據專案規範維護，記錄專案內各檔案之路徑、功能說明、關鍵字與核心依賴，以加速索引定位並減少未來的實體檔案讀取。

| 檔案路徑 | 功能說明 | 關鍵字 | 主要 Class/Method 名稱或核心依賴 |
| :--- | :--- | :--- | :--- |
| `reads/gcpdevelop.md` | GCP 部署指引：記載 VM 建置、Cloud SQL Proxy、JDK 25 安裝、Swap 配置、iptables 轉向、systemd 服務、Ops Agent 與 Cloud Logging 整合流程與手動測試驗證 | GCP, Deployment, Cloud SQL Proxy, systemd, iptables, Cloud Logging, Ops Agent | `systemd`, `cloud-sql-proxy`, `iptables`, `logback-spring.xml` |
| `README.md` | 專案主要說明文件：包含技術棧 (Spring Boot 4.0.6, Java 25, PostgreSQL)、本地 Docker Compose 啟動、Package 架構、API/gRPC 端點、環境變數與 Actuator 驗簽機制 | Readme, Architecture, API, gRPC, Actuator, Environment Variables | `RungameApplication`, `ScoreGrpcEndpoint`, `ActuatorAuthFilter`, `TraceIdFilter` |
| `.gitignore` | Git 版本控制忽略清單：排除編譯產物、IDE 設定檔與暫存檔案 | Git, Ignore, Target, IDE | `target/`, `.idea/`, `.vscode/` |
| `GEMINI.md` | 專案規範與架構決策指南：規範 Java 25、Spring Boot、Testcontainers 測試規範、gRPC Client 最佳實踐、SSE 串流等 | Guidelines, Tech Stack, Testcontainers, gRPC, SSE, Conventions | Spring Boot 4.0.6, Java 25, PostgreSQL, Testcontainers |
