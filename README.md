# Spring Boot 4.x · Java 25 · PostgreSQL

## 技術棧

| 元件 | 版本       | 說明 |
|------|----------|------|
| Spring Boot | 4.0.6    | AOT 支援 |
| Java | 25       | |
| PostgreSQL Driver | (BOM 管理) | |
| Hibernate | 6.x      | JPA 實作 |
| Flyway | (BOM 管理) | DB Migration |
| Testcontainers | (BOM 管理) | 整合測試 |

---

## 快速開始

### 前置需求

| 需求 |
|JDK 25，Docker|
|------|
| Docker & Docker Compose |

---

### Docker Compose + 外部 PostgreSQL

適合**主力本地開發**使用，資料持久化，環境最接近生產。

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

## 專案 Package 結構

以**業務領域**為主軸拆分 package，共用工具集中在 `common`，基礎設施配置放在 `config`。

```
com.dodognoman.rungame
│
├── RungameApplication.java          # 啟動入口
│
├── user/                            # 業務領域：使用者
│   ├── UserController.java          # REST 端點
│   ├── UserService.java             # 業務邏輯
│   ├── dto/                         # 資料傳輸物件（進出 API 的 payload）
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   └── AuthResponse.java
│   └── repo/                        # 資料存取層
│       ├── User.java                # JPA Entity
│       └── UserRepository.java      # Spring Data Repository
│
├── common/                          # 跨領域共用元件
│   └── JwtService.java              # JWT 產生 / 驗證
│
└── config/                          # 基礎設施配置
    └── JacksonConfig.java           # Jackson 全域配置
```

### Package 拆分原則

| Package | 放什麼 |
|---------|--------|
| `{domain}/` | Controller、Service（同一業務領域的進入點與邏輯） |
| `{domain}/dto/` | Request / Response 物件，只做資料搬運，不含邏輯 |
| `{domain}/repo/` | JPA Entity 與 Repository，隱藏資料存取細節 |
| `common/` | 跨多個領域共用的服務或工具（JWT、加密、通用例外處理等） |
| `config/` | Spring `@Configuration` 類，負責 Bean 宣告與環境初始化 |

新增業務功能時，以**領域名稱**建立新的頂層 package（例如 `game/`、`leaderboard/`），各自維護自己的 Controller / Service / dto / repo，避免跨領域直接相互依賴。

---

## 為什麼選 PostgreSQL？

在 Spring Boot 4.x + Java 25 這個組合下，PostgreSQL 是**支援度最高**的選擇：

1. Hibernate 6.x 對 PostgreSQL Dialect 的支援最完整
2. Flyway 的 `flyway-database-postgresql` 模組原生支援
3. 生態系成熟，社群資源豐富

---


## API 端點

```
全域 pre path /rungame
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
