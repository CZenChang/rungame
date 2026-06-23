# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> 人類向的專案介紹、快速開始、部署、環境變數、Actuator / Logging 等，請看 [README.md](README.md)。
> 本檔只記錄「AI 工作時需特別注意的規則與慣例」，避免與 README 重複。

---

## 專案一句話

`rungame` 是 **Spring Boot 4.0.6 + Java 25 + PostgreSQL** 的後端服務（Maven、Group ID `com.dodognoman`，進入點 `RungameApplication`）。技術棧細節見 [README.md](README.md)。

---

## 動手前必讀的雷區

- **Spring Boot 4.0.6**：許多 API 與 3.x 不同，且**自動配置被拆成獨立模組**——某些功能需額外引入對應的 `spring-boot-<feature>` 模組才會自動配置（例如 Flyway 需 `spring-boot-flyway`）。查文件務必確認是 4.x。
- **Java 25**：可放心使用最新語言特性（Records、Pattern Matching、Virtual Threads 等）；本專案已啟用 virtual threads。
- **測試需 Docker**：整合測試用 Testcontainers 起 PostgreSQL，跑測試前要先啟動 Docker。
- **DB schema 用 Flyway 管**：`spring.jpa.hibernate.ddl-auto=validate`，**不會自動建表**。改 schema 一律新增 `src/main/resources/db/migration/V{版本}__{描述}.sql`，不要改既有腳本。

---

## 程式碼慣例

- **Package 以「功能」拆分**：一個功能一個頂層 package，含業務功能（`user/`、`score/`）與系統功能（`authjwt/`、`filter/`）。新增功能時比照辦理，各自維護 Controller / Service / dto / repo，避免跨功能直接相依。詳見 [README.md](README.md) 的 package 章節。
- **API 回應**：統一包在 `common/dto/ApiResponse`，錯誤碼用 `common/exception/ErrorCode`。
- **設定與機敏資訊**：寫在 `src/main/resources/application.yaml`，敏感值一律走環境變數（如 `${DB_HOST}`、`${JWT_SECRET}`），不要寫死。

---

## 常用指令

於 `pom.xml` 所在目錄執行；Windows 用 `mvnw.cmd`，其餘平台用 `./mvnw`。

```bash
./mvnw spring-boot:run                      # 啟動（本機開發，吃 default profile）
./mvnw test                                 # 全部測試
./mvnw test -Dtest=ScoreServiceTest         # 單一測試類別
./mvnw package                              # 打包 jar
```

> 部署、profile（`gcp`）、環境變數等請參照 [README.md](README.md)。

---

## 文件索引

| 路徑 | 說明 |
|------|------|
| [README.md](README.md) | 人類向總覽：快速開始、部署、環境變數、Actuator、Logging |
| `reads/db/schema.md` | 資料表欄位定義與說明 |
| `reads/db/erd.md` | 資料表關聯與 ER 圖 |
| `reads/db/decisions.md` | 資料庫設計決策紀錄 |
| `reads/api/api.md` | API 總文件（ApiResponse、Token、Users / Scores / Frontend Log） |
