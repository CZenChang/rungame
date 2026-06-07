# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 專案概覽

`rungame` 是一個以 **Spring Boot 4.x + Java 25 + GraalVM Native + PostgreSQL** 為核心技術棧的後端服務，使用 Maven 管理依賴。

- **Group ID**：`com.dodognoman`（注意：Java 套件路徑為 `com.dogodnoman`，兩者拼法不同，日後統一時需一併修正）
- **主程式進入點**：`src/main/java/com/dogodnoman/rungame/RungameApplication.java`

---

## 常用指令

所有 Maven 指令請在 `rungame/` 子目錄下執行（即 `pom.xml` 所在位置）。

```bash
# 編譯並啟動（JVM 模式）
./mvnw spring-boot:run

# 執行所有測試
./mvnw test

# 執行單一測試類別
./mvnw test -Dtest=RungameApplicationTests

# 打包（JVM jar）
./mvnw package

# 打包為 GraalVM Native 可執行檔（需安裝 GraalVM JDK 25）
./mvnw -Pnative package

# 以 Native 模式執行測試
./mvnw -Pnative test
```

Windows 環境請將 `./mvnw` 替換為 `mvnw.cmd`。

---

## 技術架構

### 核心依賴

| 功能 | 套件 |
|------|------|
| Web API | `spring-boot-starter-web` |
| ORM | `spring-boot-starter-data-jpa` |
| 資料庫 | PostgreSQL（執行期） |
| DB 遷移 | Flyway（`flyway-core` + `flyway-database-postgresql`） |
| 輸入驗證 | `spring-boot-starter-validation` |
| 健康檢查 / 指標 | `spring-boot-starter-actuator` |
| 整合測試 DB | Testcontainers（`org.testcontainers:postgresql`） |

### GraalVM Native Profile

啟用 `-Pnative` 時，`native-maven-plugin` 會在 `package` 階段呼叫 GraalVM 將應用程式編譯為原生二進位檔，主要設定：

- 輸出檔名：`rungame`
- GC 策略：Serial GC（適合輕量服務）
- 優化等級：`-O2`
- 無 fallback 模式（`--no-fallback`）
- 自動引用 GraalVM Reachability Metadata Repository

新增反射、序列化相關元件時，需確認是否已涵蓋於 Metadata Repository，否則要手動補充 `reflect-config.json`。

### 資料庫遷移（Flyway）

遷移腳本放置於 `src/main/resources/db/migration/`，命名慣例為 `V{版本}__{描述}.sql`。測試環境透過 Testcontainers 自動啟動 PostgreSQL 容器，不需本機安裝資料庫。

### 設定檔

`src/main/resources/application.yaml` 目前僅定義應用程式名稱。資料庫連線、Flyway 等設定應在此擴充，建議以 `spring.datasource.*` 和環境變數（`${DB_URL}`）管理敏感資訊。

---

## 文件目錄

| 路徑 | 說明 |
|------|------|
| `reads/db/schema.md` | 資料表欄位定義與說明 |
| `reads/db/erd.md` | 資料表關聯與 ER 圖 |
| `reads/db/decisions.md` | 資料庫設計決策紀錄 |

---

## 開發注意事項

- **Spring Boot 版本為 4.0.6**，部分 API 與 3.x 有差異，查閱文件時請確認版本。
- **Java 版本為 25**，可使用最新語言特性（Records、Pattern Matching、Virtual Threads 等）。
- 整合測試依賴 Docker（Testcontainers），執行測試前請確保 Docker 服務已啟動。
- AOT（Ahead-of-Time）處理由 `spring-boot-maven-plugin` 的 `process-aot` goal 負責，Native 編譯前會自動執行。
