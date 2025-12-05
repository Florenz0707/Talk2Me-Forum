package com.example.springboot_backend.core.config;

import com.example.springboot_backend.core.security.JwtAccessDeniedHandler;
import com.example.springboot_backend.core.security.JwtAuthenticationEntryPoint;
import com.example.springboot_backend.core.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.DefaultLoginPageConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity
    public static class SecurityConfig {

        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
        private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CorsConfigurationSource corsConfigurationSource;

        public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                              JwtAccessDeniedHandler jwtAccessDeniedHandler,
                              JwtAuthenticationFilter jwtAuthenticationFilter,
                              CorsConfigurationSource corsConfigurationSource) {
            this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
            this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
            this.jwtAuthenticationFilter = jwtAuthenticationFilter;
            this.corsConfigurationSource = corsConfigurationSource;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
            return authConfig.getAuthenticationManager();
        }

        @Bean
        @SuppressWarnings("unchecked")
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            // 移除默认登录页面配置器
            http.removeConfigurers(DefaultLoginPageConfigurer.class);

            http.csrf(AbstractHttpConfigurer::disable)
                    .cors(cors -> cors.configurationSource(corsConfigurationSource))
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            // H2控制台允许访问（仅开发环境）
                            .requestMatchers("/h2-console", "/h2-console/**").permitAll()
                            // Actuator端点允许访问（仅开发环境，生产环境应该限制）
                            .requestMatchers("/actuator", "/actuator/**").permitAll()
                            // 公开端点：登录、注册、刷新token不需要认证
                            // 注意：Spring Security会自动处理server.servlet.context-path，这里不需要包含context-path
                            .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh").permitAll()
                            // 其他认证相关端点需要认证
                            .requestMatchers("/api/v1/auth/**").authenticated()
                            // 其他所有请求需要认证
                            .anyRequest().authenticated())
                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                            .accessDeniedHandler(jwtAccessDeniedHandler))
                    // 添加JWT过滤器
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    // 禁用默认登录机制
                    .formLogin(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .rememberMe(AbstractHttpConfigurer::disable);

            return http.build();
        }
    }
}
