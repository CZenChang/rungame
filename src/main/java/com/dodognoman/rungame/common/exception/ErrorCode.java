package com.dodognoman.rungame.common.exception;

/**
 * 統一錯誤代碼定義
 */
public enum ErrorCode {
    // 參數與請求類 (1000-1999)
    VALIDATION_ERROR(1001, "請求參數格式不正確"),
    PARSE_ERROR(1002, "請求格式解析失敗"),

    // 資料庫與資源類 (2000-2999)
    DATA_CONFLICT(2001, "資料衝突或重複"),
    RESOURCE_NOT_FOUND(2002, "找不到指定的資源"),
    DATABASE_ERROR(2003, "資料庫操作異常"),

    // IO 與系統層類 (3000-3999)
    IO_ERROR(3001, "系統讀寫異常"),

    // 身分驗證與權限類 (4000-4999)
    AUTH_FAILED(4001, "登入驗證失敗"),
    ACCESS_DENIED(4002, "權限不足，拒絕存取"),

    // 通用系統類 (9000-9999)
    SYSTEM_ERROR(9001, "系統發生未知錯誤"),
    RUNTIME_ERROR(9002, "系統執行異常"),
    TOO_MANY_REQUESTS(9003, "請求過於頻繁，請稍後再試");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
