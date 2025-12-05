package com.example.springboot_backend.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * CORS跨域配置
 * 从环境变量读取配置，支持多个源、方法、请求头等配置
 */
@Configuration
public class CorsConfig {

    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    @Value("${CORS_ALLOWED_METHODS:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${CORS_ALLOWED_HEADERS:*}")
    private String allowedHeaders;

    @Value("${CORS_ALLOW_CREDENTIALS:true}")
    private boolean allowCredentials;

    @Value("${CORS_MAX_AGE:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 解析允许的源（支持多个，用逗号分隔）
        // 注意：如果 allowCredentials 为 true，不能使用 "*"，必须指定具体的源
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            List<String> trimmedOrigins = origins.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            // 如果 allowCredentials 为 true 且 origins 包含 "*"，则使用默认值
            if (allowCredentials && trimmedOrigins.contains("*")) {
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
            } else {
                configuration.setAllowedOrigins(trimmedOrigins);
            }
        } else {
            // 如果 allowCredentials 为 true，使用默认源列表；否则使用 "*"
            if (allowCredentials) {
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
            } else {
                configuration.setAllowedOrigins(Collections.singletonList("*"));
            }
        }

        // 解析允许的HTTP方法
        if (allowedMethods != null && !allowedMethods.trim().isEmpty()) {
            List<String> methods = Arrays.asList(allowedMethods.split(","));
            configuration.setAllowedMethods(methods.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList());
        } else {
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }

        // 解析允许的请求头
        if (allowedHeaders != null && !allowedHeaders.trim().isEmpty()) {
            if ("*".equals(allowedHeaders.trim())) {
                configuration.addAllowedHeader("*");
            } else {
                List<String> headers = Arrays.asList(allowedHeaders.split(","));
                configuration.setAllowedHeaders(headers.stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList());
            }
        } else {
            configuration.addAllowedHeader("*");
        }

        // 是否允许携带凭证
        configuration.setAllowCredentials(allowCredentials);

        // 预检请求缓存时间（秒）
        configuration.setMaxAge(maxAge);

        // 允许的响应头
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用CORS配置
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
