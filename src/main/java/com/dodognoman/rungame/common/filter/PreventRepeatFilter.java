package com.dodognoman.rungame.common.filter;

import com.dodognoman.rungame.common.dto.ApiResponse;
import com.dodognoman.rungame.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 防止重複提交過濾器
 * 利用 CacheManager (Caffeine) 攔截短時間內的相同請求
 */
@Component
public class PreventRepeatFilter extends OncePerRequestFilter {

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    public PreventRepeatFilter(CacheManager cacheManager, ObjectMapper objectMapper) {
        this.cacheManager = cacheManager;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String key = generateKey(request);
        Cache cache = cacheManager.getCache("preventRepeat");

        if (cache != null) {
            // 嘗試獲取，如果已存在則阻斷
            if (cache.get(key) != null) {
                renderErrorResponse(response, request.getRequestURI());
                return;
            }
            // 否則存入 (Caffeine 會根據配置自動在 1s 後過期)
            cache.put(key, true);
        }


        filterChain.doFilter(request, response);
    }

    private String generateKey(HttpServletRequest request) {
        // 簡單組合：IP + URI + Method (進階可加入 Token 或特定參數)
        return request.getRemoteAddr() + ":" + request.getRequestURI() + ":" + request.getMethod();
    }

    private void renderErrorResponse(HttpServletResponse response, String path) throws IOException {
        ErrorCode errorCode = ErrorCode.TOO_MANY_REQUESTS;
        ApiResponse<Object> apiResponse = ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage(),
            path
        );

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
