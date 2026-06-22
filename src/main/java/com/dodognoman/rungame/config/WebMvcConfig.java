package com.dodognoman.rungame.config;

import com.dodognoman.rungame.authjwt.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Ceizer
 * @apiNote 註冊 JWT 驗證攔截器；預設攔截所有 /api/**，actuator 由 ActuatorAuthFilter 另行保護故排除
 * @since 2026/6/17
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 路徑相對於 servlet（context-path /rungame 已被去除）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**");
    }
}
