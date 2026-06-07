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

## 為什麼選 PostgreSQL？

在 Spring Boot 4.x + Java 25 + GraalVM Native 這個組合下，PostgreSQL 是**支援度最高**的選擇：

1. **pgjdbc 官方維護** GraalVM reachability metadata（`org.postgresql:postgresql` 已內建）
2. Hibernate 6.x 對 PostgreSQL Dialect 的 AOT 處理最完整
3. Flyway 的 `flyway-database-postgresql` 模組原生支援
4. 相較 MySQL/MariaDB，Native 編譯時反射配置缺失問題最少

---

## 快速開始

### 前置需求
- GraalVM Community 25+（需含 `native-image` 元件）
- Docker & Docker Compose

### 1. 啟動 PostgreSQL

```bash
docker compose up -d
```

### 2. 一般 JVM 模式運行

```bash
./mvnw spring-boot:run
```

### 3. 編譯 Native Binary

```bash
# 需要 GraalVM native-image 工具
./mvnw -Pnative -DskipTests package

# 執行 native binary（啟動時間 < 100ms）
./target/demo
```

### 4. Native Binary + Docker 打包

```bash
docker build -t demo:native .
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  demo:native
```

---

## API 端點

```
GET    /api/products              列出所有產品
GET    /api/products?search=java  關鍵字搜尋
GET    /api/products?minPrice=10&maxPrice=50  價格區間
GET    /api/products/{id}         取得單一產品
POST   /api/products              建立產品
PUT    /api/products/{id}         更新產品
DELETE /api/products/{id}         刪除產品

GET    /actuator/health           健康檢查
GET    /actuator/metrics          指標
```

### 範例 Request

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"My Product","description":"Desc","price":29.99}'
```

---

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
