package com.dodognoman.rungame.frontendlog;

import com.dodognoman.rungame.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端 Log 接收端點。
 * 規格（內容格式）由前端自行定義，後端只負責把字串寫進獨立的 FRONTEND log 檔。
 * <p>
 * 安全底線（即使讓前端自由定義內容也務必保留）：
 * 1. 限長：避免單筆超大字串灌爆磁碟。
 * 2. 清洗換行/控制字元：防 log injection（攻擊者塞 \n 偽造一整行 log）。
 */
@RestController
@RequestMapping("/api/frontend-log")
public class FrontendLogController {

    /** 獨立 logger，於 logback-spring.xml 導向單獨檔案，方便保留策略與隔離 */
    private static final Logger frontendLog = LoggerFactory.getLogger("FRONTEND");

    /** 單筆內容上限，超出截斷 */
    private static final int MAX_LENGTH = 4000;

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public ApiResponse<Void> write(
            @RequestParam("level") String level,
            @RequestBody(required = false) String content, HttpServletRequest request) {

        switch (level) {
            case "INFO":
                frontendLog.info("ip={} | {}", request.getRemoteAddr(), sanitize(content));
                break;
            case "WARN":
                frontendLog.warn("ip={} | {}", request.getRemoteAddr(), sanitize(content));
                break;
            case "ERROR":
                frontendLog.error("ip={} | {}", request.getRemoteAddr(), sanitize(content));
                break;
            default:
                break;
        }
        return ApiResponse.ok("OK");
    }

    /** 移除換行與控制字元（防 log injection），並限制長度 */
    private String sanitize(String raw) {

        if (raw == null || raw.isBlank()) {
            return "";
        }

        String safe = raw.replaceAll("[\\r\\n\\t\\p{Cntrl}]", " ");
        if (safe.length() > MAX_LENGTH) {
            safe = safe.substring(0, MAX_LENGTH) + "...(truncated)";
        }
        return safe;
    }

    /** 更高效的 sanitize 實作，避免 regex 與 substring 產生過多中間物件
    private String sanitize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        int limit = Math.min(raw.length(), MAX_LENGTH);
        StringBuilder sb = new StringBuilder(limit + 16);
        for (int i = 0; i < limit; i++) {
            char c = raw.charAt(i);
            // 換行/Tab/控制字元一律換成空格，防 log injection
            sb.append(Character.isISOControl(c) ? ' ' : c);
        }
        if (raw.length() > MAX_LENGTH) {
            sb.append("...(truncated)");
        }
        return sb.toString();
    }
    **/
}
