package com.dodognoman.rungame.common.exception;

import com.dodognoman.rungame.common.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionAdvice {



    /**
     * 處理 Spring 內建的 ResponseStatusException
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.SYSTEM_ERROR;
        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            errorCode = ErrorCode.AUTH_FAILED;
        } else if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
            errorCode = ErrorCode.ACCESS_DENIED;
        } else if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        }

        ApiResponse<Object> response = ApiResponse.error(
            errorCode.getCode(),
            ex.getReason() != null ? ex.getReason() : errorCode.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    /**
     * 處理所有未定義的異常 (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.SYSTEM_ERROR;
        ApiResponse<Object> response = ApiResponse.error(
            errorCode.getCode(),
            "系統異常",
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 處理參數校驗異常 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        // 這裡我們將驗證細節放入 data 中回傳
        ApiResponse<Map<String, String>> response = new ApiResponse<>(
            errorCode.getCode(),
            errorCode.getMessage(),
            details,
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 處理 JSON 解析異常 (400 Bad Request)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.PARSE_ERROR;
        ApiResponse<Object> response = ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 處理 IO 異常 (500 Internal Server Error)
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Object>> handleIOException(IOException ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.IO_ERROR;
        ApiResponse<Object> response = ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 處理資料庫完整性衝突 (例如唯一約束違反) (409 Conflict)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.DATA_CONFLICT;
        ApiResponse<Object> response = ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 處理 JPA 實體未找到異常 (404 Not Found)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        ApiResponse<Object> response = ApiResponse.error(
            errorCode.getCode(),
            ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 處理通用資料存取異常 (500 Internal Server Error)
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataAccessException(DataAccessException ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.DATABASE_ERROR;
        ApiResponse<Object> response = ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 處理運行時異常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.RUNTIME_ERROR;
        ApiResponse<Object> response = ApiResponse.error(
            errorCode.getCode(),
            ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
