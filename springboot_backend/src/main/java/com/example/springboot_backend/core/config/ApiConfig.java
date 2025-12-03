package com.example.springboot_backend.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * API配置类
 * 统一管理API路径前缀等配置
 */
@Configuration
public class ApiConfig {

    /**
     * API基础路径前缀
     * 默认值为 /api/v1，可通过环境变量 API_BASE_PATH 配置
     */
    @Value("${API_BASE_PATH:/api/v1}")
    private String apiBasePath;

    public String getApiBasePath() {
        return apiBasePath;
    }

    /**
     * 获取完整的API路径
     * @param path 相对路径，如 /login
     * @return 完整路径，如 /api/v1/login
     */
    public String getApiPath(String path) {
        if (path.startsWith("/")) {
            return apiBasePath + path;
        }
        return apiBasePath + "/" + path;
    }
}
