package com.example.springboot_backend.core.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.*;

/**
 * H2 控制台请求头过滤器配置
 * 解决 H2 控制台显示 "Sorry, Lynx is not supported yet" 的问题
 *
 * H2 控制台会检查请求头（User-Agent、Accept 等），如果检测到是文本浏览器或缺少必要的请求头，
 * 就会显示错误。此过滤器为 H2 控制台请求设置标准的浏览器请求头。
 */
@Configuration
@ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true", matchIfMissing = false)
public class H2ConsoleUserAgentFilterConfig {

    private static final Logger logger = LoggerFactory.getLogger(H2ConsoleUserAgentFilterConfig.class);
    private static final String H2_CONSOLE_PATH = "/h2-console";
    private static final String STANDARD_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String STANDARD_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";
    private static final String STANDARD_ACCEPT_LANGUAGE = "zh-CN,zh;q=0.9,en;q=0.8";
    private static final String STANDARD_ACCEPT_ENCODING = "gzip, deflate, br";

    /**
     * H2 控制台请求头过滤器
     */
    public static class H2ConsoleUserAgentFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String requestURI = httpRequest.getRequestURI();
            String contextPath = httpRequest.getContextPath();
            String fullPath = contextPath != null ? contextPath + requestURI : requestURI;

            // 检查是否是 H2 控制台请求
            if (requestURI != null && fullPath.contains(H2_CONSOLE_PATH)) {
                logger.debug("H2 Console request detected - URI: {}, ContextPath: {}, FullPath: {}",
                        requestURI, contextPath, fullPath);

                // 包装请求，修改请求头
                HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
                    // 使用 TreeMap 进行大小写不敏感的查找
                    private final Map<String, String> headerOverrides = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

                    {
                        // 设置标准的浏览器请求头
                        headerOverrides.put("User-Agent", STANDARD_USER_AGENT);
                        headerOverrides.put("Accept", STANDARD_ACCEPT);
                        headerOverrides.put("Accept-Language", STANDARD_ACCEPT_LANGUAGE);
                        headerOverrides.put("Accept-Encoding", STANDARD_ACCEPT_ENCODING);
                    }

                    @Override
                    public String getHeader(String name) {
                        String override = findHeaderOverride(name);
                        if (override != null) {
                            logger.debug("Overriding header '{}'", name);
                            return override;
                        }
                        return super.getHeader(name);
                    }

                    @Override
                    public Enumeration<String> getHeaders(String name) {
                        String override = findHeaderOverride(name);
                        if (override != null) {
                            logger.debug("Overriding headers '{}'", name);
                            return Collections.enumeration(Collections.singletonList(override));
                        }
                        return super.getHeaders(name);
                    }

                    @Override
                    public Enumeration<String> getHeaderNames() {
                        List<String> names = Collections.list(super.getHeaderNames());
                        // 添加所有覆盖的请求头名称
                        for (String headerName : headerOverrides.keySet()) {
                            if (!names.contains(headerName)) {
                                names.add(headerName);
                            }
                        }
                        return Collections.enumeration(names);
                    }

                    /**
                     * 大小写不敏感地查找请求头覆盖
                     * TreeMap 使用 CASE_INSENSITIVE_ORDER，所以直接 get 即可
                     */
                    private String findHeaderOverride(String name) {
                        return headerOverrides.get(name);
                    }
                };

                // 包装响应，移除 X-Frame-Options 头以允许 H2 控制台在 iframe 中加载
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(httpResponse) {
                    @Override
                    public void setHeader(String name, String value) {
                        if ("X-Frame-Options".equalsIgnoreCase(name)) {
                            logger.debug("Removing X-Frame-Options header for H2 console");
                            return;
                        }
                        super.setHeader(name, value);
                    }

                    @Override
                    public void addHeader(String name, String value) {
                        if ("X-Frame-Options".equalsIgnoreCase(name)) {
                            logger.debug("Removing X-Frame-Options header for H2 console");
                            return;
                        }
                        super.addHeader(name, value);
                    }
                };

                chain.doFilter(wrappedRequest, wrappedResponse);

                // 确保移除 X-Frame-Options 头（如果仍然存在）
                if (httpResponse.containsHeader("X-Frame-Options")) {
                    logger.debug("Removing X-Frame-Options header from response for H2 console");
                    httpResponse.setHeader("X-Frame-Options", "");
                }
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    /**
     * 注册过滤器，确保在 Spring Security 之前执行
     */
    @Bean
    public FilterRegistrationBean<H2ConsoleUserAgentFilter> h2ConsoleUserAgentFilterRegistration() {
        FilterRegistrationBean<H2ConsoleUserAgentFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new H2ConsoleUserAgentFilter());
        registration.addUrlPatterns("/h2-console/*");
        // 支持 context-path
        registration.addUrlPatterns("/*/h2-console/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("h2ConsoleUserAgentFilter");
        logger.info("H2 Console User-Agent Filter registered with highest precedence");
        return registration;
    }
}
