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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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
                logger.info("H2 Console request detected - URI: {}, ContextPath: {}, FullPath: {}",
                        requestURI, contextPath, fullPath);

                // 记录原始请求头
                logger.info("Original User-Agent: {}", httpRequest.getHeader("User-Agent"));
                logger.info("Original Accept: {}", httpRequest.getHeader("Accept"));

                // 包装请求，修改请求头
                HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
                    // 使用 TreeMap 进行大小写不敏感的查找
                    private final Map<String, String> headerOverrides = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

                    {
                        // 设置标准的浏览器请求头（使用标准格式）
                        headerOverrides.put("User-Agent", STANDARD_USER_AGENT);
                        headerOverrides.put("Accept", STANDARD_ACCEPT);
                        headerOverrides.put("Accept-Language", STANDARD_ACCEPT_LANGUAGE);
                        headerOverrides.put("Accept-Encoding", STANDARD_ACCEPT_ENCODING);
                        logger.info("Header overrides initialized: {}", headerOverrides.keySet());
                    }

                    @Override
                    public String getHeader(String name) {
                        // 尝试大小写不敏感查找
                        String override = findHeaderOverride(name);
                        if (override != null) {
                            logger.info("Overriding header '{}' with: {}", name, override);
                            return override;
                        }
                        String original = super.getHeader(name);
                        logger.debug("Header '{}' not overridden, original value: {}", name, original);
                        return original;
                    }

                    @Override
                    public Enumeration<String> getHeaders(String name) {
                        String override = findHeaderOverride(name);
                        if (override != null) {
                            logger.info("Overriding headers '{}' with: {}", name, override);
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
                     */
                    private String findHeaderOverride(String name) {
                        // 直接查找
                        String override = headerOverrides.get(name);
                        if (override != null) {
                            return override;
                        }
                        // 如果直接查找失败，尝试大小写不敏感查找
                        for (Map.Entry<String, String> entry : headerOverrides.entrySet()) {
                            if (entry.getKey().equalsIgnoreCase(name)) {
                                return entry.getValue();
                            }
                        }
                        return null;
                    }
                };

                logger.info("Wrapped request created, proceeding with filter chain");

                // 同时包装响应，拦截并修改错误消息
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                ResponseWrapper wrappedResponse = new ResponseWrapper(httpResponse) {
                    @Override
                    public void setHeader(String name, String value) {
                        // 移除 X-Frame-Options 头，允许 H2 控制台在 iframe 中加载
                        if ("X-Frame-Options".equalsIgnoreCase(name)) {
                            logger.info("Removing X-Frame-Options header for H2 console");
                            return;
                        }
                        super.setHeader(name, value);
                    }

                    @Override
                    public void addHeader(String name, String value) {
                        // 移除 X-Frame-Options 头，允许 H2 控制台在 iframe 中加载
                        if ("X-Frame-Options".equalsIgnoreCase(name)) {
                            logger.info("Removing X-Frame-Options header for H2 console");
                            return;
                        }
                        super.addHeader(name, value);
                    }
                };

                chain.doFilter(wrappedRequest, wrappedResponse);

                // 移除 X-Frame-Options 头（如果存在），允许 H2 控制台在 iframe 中加载
                if (httpResponse.containsHeader("X-Frame-Options")) {
                    logger.info("Removing X-Frame-Options header from response for H2 console");
                    httpResponse.setHeader("X-Frame-Options", "");
                }

                // 检查响应内容，替换错误消息
                try {
                    byte[] content = wrappedResponse.getContent();
                    if (content.length > 0) {
                        String contentStr = new String(content, StandardCharsets.UTF_8);
                        if (contentStr.contains("Sorry, Lynx is not supported yet") ||
                            contentStr.contains("Lynx is not supported")) {
                            logger.warn("Detected 'Lynx is not supported' error in response, attempting to fix...");
                            // 尝试替换错误消息，或者重定向到正确的页面
                            contentStr = contentStr.replace("Sorry, Lynx is not supported yet", "");
                            contentStr = contentStr.replace("Lynx is not supported", "");
                            content = contentStr.getBytes(StandardCharsets.UTF_8);
                        }
                        httpResponse.setContentLength(content.length);
                        httpResponse.getOutputStream().write(content);
                        httpResponse.getOutputStream().flush();
                    }
                } catch (IOException e) {
                    logger.error("Error processing H2 console response", e);
                    throw new ServletException("Error processing H2 console response", e);
                }
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    /**
     * 响应包装器，用于拦截和修改响应内容
     */
    private static class ResponseWrapper extends HttpServletResponseWrapper {
        private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private PrintWriter writer;
        private ServletOutputStream outputStream;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (writer == null) {
                writer = new PrintWriter(new java.io.OutputStreamWriter(buffer, StandardCharsets.UTF_8), true);
            }
            return writer;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (outputStream == null) {
                outputStream = new ServletOutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        buffer.write(b);
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setWriteListener(WriteListener listener) {
                        // Not needed
                    }
                };
            }
            return outputStream;
        }

        @Override
        public void flushBuffer() throws IOException {
            if (writer != null) {
                writer.flush();
            }
            if (outputStream != null) {
                outputStream.flush();
            }
        }

        public byte[] getContent() {
            try {
                flushBuffer();
            } catch (IOException e) {
                // Ignore flush errors
            }
            return buffer.toByteArray();
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
