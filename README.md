# Spring Boot 4.x · Java 25 · GraalVM Native · PostgreSQL

## 技術棧

| 元件 | 版本       | 說明 |
|------|----------|------|
| Spring Boot | 4.0.6    | AOT + Native 支援 |
| Java | 25       | GraalVM Community Edition |
| GraalVM Native Plugin | 0.10.6   | native-maven-plugin |
| PostgreSQL Driver | (BOM 管理) | 最佳 Native 相容性 |
| Hibernate | 6.x      | JPA 實作 |
| Flyway | (BOM 管理) | DB Migration |
| Testcontainers | (BOM 管理) | 整合測試 |

---

## 快速開始

### 前置需求

| 方案 | 需求 |
|------|------|
| 方案一（Docker Compose） | Docker & Docker Compose |
| 方案二（Local Profile） | 無（embedded-postgres 自動啟動） |
| 方案三 / 四（Native） | GraalVM Community 25+（含 `native-image`） |

---

### 方案一：Docker Compose + 外部 PostgreSQL

適合需要完整 PostgreSQL 環境或接近生產設定時使用。

**1. 啟動 PostgreSQL**

```bash
docker compose up -d
```

**2. 啟動應用程式**

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```
---

### 方案二：Local Profile（embedded-postgres，不需 Docker）

適合本地開發快速啟動，無需安裝或啟動 Docker。資料存於專案目錄下的 `dbdata/`，重啟後保留。

**1. 在 IntelliJ IDEA 設定 Run Configuration**

- Active profiles 填入：`local`
- ![img.png](img.png)
- 右側 Maven 面板 → Profiles → 勾選 `local`

**2. 或用指令啟動**

```bash
# Linux / macOS
./mvnw spring-boot:run -Plocal -Dspring-boot.run.profiles=local

# Windows
mvnw.cmd spring-boot:run -Plocal -Dspring-boot.run.profiles=local
```

> **注意**：停止應用程式前若有 postgres process 殘留（port 15432），重新啟動前需先手動終止：
> ```powershell
> netstat -ano | findstr :15432
> taskkill /PID <PID> /F
> ```

---

### 方案三：編譯 Native Binary

**Windows 額外需求**：GraalVM Native Image 需要 Visual Studio 2022 的 C++ 工具鏈。

1. 安裝 [Visual Studio 2022](https://visualstudio.microsoft.com/downloads/)（Community 版即可）
2. 安裝時勾選 `Desktop development with C++` 工作負載
3. **必須在 x64 Native Tools Command Prompt for VS 2022 裡執行**（開始選單搜尋），不能用一般 PowerShell

```cmd
# Windows（在 x64 Native Tools Command Prompt 執行）
mvnw.cmd -Pnative -DskipTests package

# Linux / macOS
./mvnw -Pnative -DskipTests package
```

```bash
# 執行 native binary（啟動時間 < 100ms）
./target/rungame
```

### 方案四：Native Binary + Docker 打包

```bash
docker build -t rungame:native .
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  rungame:native
```

---
## 為什麼選 PostgreSQL？

在 Spring Boot 4.x + Java 25 + GraalVM Native 這個組合下，PostgreSQL 是**支援度最高**的選擇：

1. **pgjdbc 官方維護** GraalVM reachability metadata（`org.postgresql:postgresql` 已內建）
2. Hibernate 6.x 對 PostgreSQL Dialect 的 AOT 處理最完整
3. Flyway 的 `flyway-database-postgresql` 模組原生支援
4. 相較 MySQL/MariaDB，Native 編譯時反射配置缺失問題最少

---


## API 端點

```

GET    /actuator/health           健康檢查
GET    /actuator/metrics          指標
```

## 環境變數

| 變數 | 預設值 | 說明 |
|------|--------|------|
| `DB_HOST` | `localhost` | PostgreSQL 主機 |
| `DB_PORT` | `5432` | PostgreSQL 埠號 |
| `DB_NAME` | `demo` | 資料庫名稱 |
| `DB_USER` | `postgres` | 使用者名稱 |
| `DB_PASS` | `postgres` | 密碼 |

---

## Native Image 注意事項

1. **AOT 處理**：`spring-boot:process-aot` 會在 compile phase 自動執行
2. **反射 Hints**：額外的反射需求在 `NativeHintsConfig` 中註冊
3. **GraalVM Metadata Repository**：`pom.xml` 已啟用，自動拉取第三方 metadata
4. **禁用 OSIV**：`open-in-view: false` 避免 native proxy 問題
5. **HikariCP**：`register-mbeans: false` 關閉 JMX（native 不支援）
