package com.dodognoman.rungame.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;
import java.time.LocalDateTime;

/**
 * 萬用 API 回應結構
 * @param <T> 資料負載類型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    int error,           // 錯誤代碼 (ErrorCode)
    String message,      // 提示訊息
    T data,              // 業務資料 (List, DTO 等)
    String path,         // 請求路徑
    LocalDateTime timestamp, // 回應時間

    // 分頁相關 (僅在分頁查詢時出現)
    Integer page,        // 當前頁碼
    Integer size,        // 每頁筆數
    Long totalElements,  // 總筆數
    Integer totalPages,  // 總頁數

    // 追蹤相關
    String traceId       // 請求追蹤碼 (用於 Log 對照)
) {
    private static final String TRACE_ID_KEY = "traceId";

    public ApiResponse(int error, String message, T data, String path) {
        this(error, message, data, path, LocalDateTime.now(), null, null, null, null, MDC.get(TRACE_ID_KEY));
    }

    // 成功回應 - 僅訊息
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(0, message, null, null);
    }

    // 成功回應 - 帶資料
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data, null);
    }

    // 成功回應 - 分頁資料
    public static <T> ApiResponse<T> page(T data, int page, int size, long totalElements, int totalPages) {
        return new ApiResponse<>(
            0, "OK", data, null, LocalDateTime.now(),
            page, size, totalElements, totalPages, MDC.get(TRACE_ID_KEY)
        );
    }

    // 錯誤回應
    public static <T> ApiResponse<T> error(int errorCode, String message, String path) {
        return new ApiResponse<>(errorCode, message, null, path);
    }
}
