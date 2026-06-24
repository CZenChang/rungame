# GCP 部署與日誌（Cloud Logging）

> 本文件記錄 `rungame` 部署到 GCP 的完整流程：上傳 Jar、VM 環境建置、systemd 服務設定、Cloud Logging 串接。
> 專案總覽、環境變數、Actuator 等請看 [README.md](../README.md)。
> 環境: gcp compute engine + cloud sql + cloud logging，OS: Debian 12 (Bookworm)。

---

## 部署流程總覽

```
步驟一：本地打包 jar → gcloud scp 上傳
步驟二：VM 安裝 JDK + Cloud SQL Proxy + Swap
步驟三：iptables 80→8080 Port 轉向
步驟四：systemd 服務設定（sql-proxy + rungame）
步驟五：安裝 Ops Agent + Cloud Logging 串接
步驟六：啟動服務 + 健康檢查驗證
```

---

## 步驟一：本地端操作與上傳

在本地終端機（Windows/Mac）執行，將打包好的 Jar 上傳至 GCP VM。

```bash
# 1. 上傳 jar 至 VM 家目錄
gcloud compute scp "C:\ceizer\git\rungame\target\rungame-0.0.1-SNAPSHOT.jar" \
  <VM名稱>:. --zone=<區域>

# 2. SSH 登入 VM
gcloud compute ssh <VM名稱> --zone=<區域>
```

---

## 步驟二：VM 基礎環境建置

登入 VM 後安裝 JDK 25、Cloud SQL Proxy，並設定 Swap 防止 OOM。

### 安裝 Java 25 (OpenJDK)

```bash
wget -O openjdk-25.tar.gz "https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/adoptium"
tar -xvf openjdk-25.tar.gz
```

解壓後目錄名稱類似 `jdk-25.0.3+9`，`ExecStart` 路徑需對應實際版本。

### 下載 Cloud SQL Proxy

```bash
wget https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.11.0/cloud-sql-proxy.linux.amd64 -O cloud-sql-proxy
chmod +x cloud-sql-proxy
```

### 設定 2GB Swap

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
# 開機自動掛載
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

---

## 步驟三：Port 轉向設定（80 → 8080）

讓外部 HTTP（Port 80）流量轉入 Java 服務（Port 8080）。

```bash
sudo apt-get update

# 清空舊的 nat 規則
sudo iptables -t nat -F

# 外部流量 80 → 8080
sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080

# 本機 localhost 80 → 8080
sudo iptables -t nat -A OUTPUT -p tcp -o lo --dport 80 -j REDIRECT --to-port 8080

# 安裝永久儲存工具（提示皆選 Yes）
sudo apt-get install -y iptables-persistent

# 未來若修改過 iptables，手動儲存：
# sudo netfilter-persistent save
```

---

## 步驟四：systemd 服務設定

### Cloud SQL Proxy 服務

執行 
```ini
sudo nano /etc/systemd/system/sql-proxy.service
```
並貼入：

```ini
[Unit]
Description=Google Cloud SQL Proxy
After=network.target

[Service]
User=<VM使用者名稱>
Type=simple
ExecStart=/home/<VM使用者名稱>/cloud-sql-proxy <專案ID>:<區域>:<Cloud SQL實例名稱>
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

### Java App 服務

執行 
```ini 
sudo nano /etc/systemd/system/rungame.service
``` 
並貼入：

```ini
[Unit]
Description=RunGame Java 25 Application
After=network.target

[Service]
User=<VM使用者名稱>
WorkingDirectory=/home/<VM使用者名稱>
Environment="SPRING_PROFILES_ACTIVE=gcp"
Environment="DB_USER=<DB使用者>"
Environment="DB_PASS=<DB密碼>"
Environment="ACTUATOR_PUBLIC_KEY=<Base64 ECDSA P-256 公鑰>"
ExecStart=/home/<VM使用者名稱>/jdk-25.0.3+9/bin/java \
  -Xms512m -Xmx512m \
  -jar /home/<VM使用者名稱>/rungame.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

> 機敏環境變數（`DB_PASS`、`ACTUATOR_PUBLIC_KEY`）寫在 systemd unit 內，確保檔案權限為 `600`、`root:root`。

---

## 步驟五：安裝 Ops Agent 與 Cloud Logging 串接

### 安裝 Ops Agent

先確認是否已安裝：

```bash
sudo systemctl status google-cloud-ops-agent
```

- `active (running)` → 已安裝，跳過安裝步驟
- `could not be found` / command not found → 尚未安裝，執行：

```bash
curl -sSO https://dl.google.com/cloudagents/add-google-cloud-ops-agent.sh
sudo bash add-google-cloud-ops-agent.sh --also-install
```

### 建立日誌目錄

```bash
sudo mkdir -p /var/log/rungame
sudo chown <VM使用者名稱>:<VM使用者名稱> /var/log/rungame
```

### 設定 Ops Agent 讀取日誌檔

 ```bash
 sudo nano /etc/google-cloud-ops-agent/config.yaml
 ```
貼上
```yaml
logging:
  receivers:
    rungame_app:
      type: files
      include_paths:
        - /var/log/rungame/app.json
  processors:
    rungame_json:
      type: parse_json
    rungame_severity:
      type: modify_fields
      fields:
        severity: { move_from: jsonPayload.severity }
    rungame_trace:
      type: modify_fields
      fields:
        trace: { move_from: 'jsonPayload."logging.googleapis.com/trace"' }
  service:
    pipelines:
      rungame:
        receivers: [rungame_app]
        processors: [rungame_json, rungame_severity, rungame_trace]
```

```bash
sudo systemctl restart google-cloud-ops-agent
```

- project id 在 Compute Engine 上由 metadata server 自動偵測；保險可設 `SPRING_CLOUD_GCP_LOGGING_PROJECT_ID=<專案ID>`。

---

## 步驟六：啟動與驗證

### 載入並啟動服務

```bash
sudo systemctl daemon-reload

sudo systemctl start sql-proxy
sudo systemctl enable sql-proxy

sudo systemctl start rungame
sudo systemctl enable rungame
```

### 健康檢查

```bash
# 直接打 8080
curl http://localhost:8080/rungame/actuator/health

# 透過 iptables 轉向後的 80
curl http://localhost/rungame/actuator/health
```

### 常用維護指令

```bash
# 停止服務
sudo systemctl stop rungame

# 查看 Java App 日誌（最新 50 行）
sudo journalctl -u rungame -n 50 --no-pager

# 查看 SQL Proxy 日誌（最新 50 行）
sudo journalctl -u sql-proxy -n 50 --no-pager
```

---

## 日誌格式與 Profile

日誌格式由 [`logback-spring.xml`](../src/main/resources/logback-spring.xml) 依 **Spring profile** 切換，所有 log 皆帶請求追蹤碼 `traceId`（由 [`TraceIdFilter`](../src/main/java/com/dodognoman/rungame/common/filter/TraceIdFilter.java) 在每個請求寫入 MDC）。

| Profile | 輸出 | 用途 |
|---------|------|------|
| `local` / `default` | 彩色純文字 console（含 `[traceId]`） | 本地開發，人讀友善 |
| `gcp` | 結構化 JSON **寫到檔案** `/var/log/rungame/app.json`（每日滾動、保留 30 天） | 部署到 GCP，含 trace/span 關聯 |

> `traceId` 只在 **HTTP 請求執行緒**中有值；啟動階段或背景執行緒的 log 會是空的，屬正常現象。

> **為什麼寫檔而不是 stdout？** stdout 會被 journald/rsyslog 收走並在每行前加上 `時間 主機 java[pid]:` 前綴，Ops Agent 以 syslog 接收器讀取時會把「前綴 + JSON」整行當成純文字字串，導致 Cloud Logging **無法解析 JSON**（severity、trace 都抽不出來）。直接寫專屬檔案可繞過此問題。

---

## 清理舊的 stdout 日誌

改成寫檔後，journald / syslog 不會再長新的應用日誌，但**之前累積的舊檔需手動清**：

```bash
# journald
journalctl --disk-usage
sudo journalctl --vacuum-time=2d        # 只留最近 2 天
# 或 sudo journalctl --vacuum-size=100M

# rsyslog 的 /var/log/syslog（用 truncate，不要 rm 正開著的檔）
sudo truncate -s 0 /var/log/syslog
sudo rm -f /var/log/syslog.*.gz /var/log/syslog.[0-9]*
```

（可選）長期防爆，於 `/etc/systemd/journald.conf` 設 `SystemMaxUse=100M` / `MaxRetentionSec=2day` 後 `sudo systemctl restart systemd-journald`。

> `/var/log/rungame/app.json` 由 logback rolling policy 自動輪替（保留 30 天），不需手動清。
