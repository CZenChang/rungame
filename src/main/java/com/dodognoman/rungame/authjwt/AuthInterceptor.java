package com.dodognoman.rungame.authjwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * @author Ceizer
 * @apiNote 驗證JWT，標記 @PassJwt 則免驗
 * @since 2026/6/17
 */

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res
            , @NonNull Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod hm)) return true;
        // 方法或 controller 類別上有 @PassJwt 跳過
        boolean pass = hm.hasMethodAnnotation(PassJwt.class)
                || hm.getBeanType().isAnnotationPresent(PassJwt.class);
        if (pass) return true;

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            res.sendError(401, "Missing token");
            return false;
        }
        try {
            Claims c = jwtService.parseClaims(auth.substring(7));
            req.setAttribute("userId", c.getSubject());
            return true;
        } catch (Exception e) {
            res.sendError(401, "Invalid token");
            return false;
        }
    }
}