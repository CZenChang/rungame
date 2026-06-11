package com.dodognoman.rungame.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 請求追蹤過濾器，為每個請求生成唯一的 traceId 並放入 MDC
 */
@Component
@Order(1)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 優先嘗試從 Header 取得 (用於微服務串聯)，若無則生成新的
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        // 存入 MDC (ThreadLocal)
        MDC.put(TRACE_ID_KEY, traceId);

        // 同時將 traceId 放入 Response Header，方便前端偵錯
        response.setHeader("X-Trace-Id", traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 重要：請求結束後清除 MDC，防止執行緒池污染
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
