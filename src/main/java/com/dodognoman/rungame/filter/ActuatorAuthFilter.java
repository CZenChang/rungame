package com.dodognoman.rungame.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 保護 /actuator/** 端點：
 * 客戶端用 ECDSA 私鑰簽章 timestamp，Server 用公鑰驗簽。
 * 即使 HTTP 封包被攔截，攻擊者無法偽造新請求（缺少私鑰）。
 *
 * 必要 Headers:
 *   X-Timestamp : 毫秒級 Unix epoch（System.currentTimeMillis()）
 *   X-Signature : Base64(ECDSA_SHA256_sign(timestamp_string, privateKey))
 */
@Component
@Order(2)
public class ActuatorAuthFilter extends OncePerRequestFilter {

    private static final long CLOCK_SKEW_MS = 10_000; // ±30 秒防重放
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_SIGNATURE = "X-Signature";
    private static final String ACTUATOR_PATTERN = "/rungame/actuator/**";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final PublicKey publicKey;

    public ActuatorAuthFilter(@Value("${actuator.public-key}") String publicKeyBase64) {
        PublicKey loaded = null;
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            KeyFactory kf = KeyFactory.getInstance("EC");
            loaded = kf.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            logger.error("無法載入 actuator.public-key（請執行 ActuatorKeyGen 並設定 ACTUATOR_PUBLIC_KEY）；"
                    + "所有 /actuator/** 請求將一律被拒絕", e);
        }
        this.publicKey = loaded;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !PATH_MATCHER.match(ACTUATOR_PATTERN, request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 公鑰未載入 → 此保護功能未正確配置，一律拒絕（fail-closed），不放行任何 actuator 請求
        if (publicKey == null) {
            logger.warn("拒絕 /actuator 請求：actuator.public-key 未載入");
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Actuator auth not configured");
            return;
        }

        String timestamp = request.getHeader(HEADER_TIMESTAMP);
        String signatureB64 = request.getHeader(HEADER_SIGNATURE);

        if (timestamp == null || signatureB64 == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing auth headers");
            return;
        }

        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid timestamp");
            return;
        }

        // 防重放：時間戳必須在秒內
        if (Math.abs(System.currentTimeMillis() - ts) > CLOCK_SKEW_MS) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Timestamp expired");
            return;
        }

        try {
            byte[] sigBytes = Base64.getDecoder().decode(signatureB64);
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(timestamp.getBytes(StandardCharsets.UTF_8));

            if (!sig.verify(sigBytes)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid signature");
                return;
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Signature verification failed");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
