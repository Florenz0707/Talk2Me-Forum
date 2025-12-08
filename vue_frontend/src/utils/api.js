// API服务层 - 处理与后端的所有HTTP请求

// API基础URL
const API_BASE_URL = 'http://127.0.0.1:8099/talk2me/api/v1'

// 存储键名
const TOKEN_KEY = 'auth_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_INFO_KEY = 'user_info';

// 错误类型定义
export class ApiError extends Error {
    constructor(message, statusCode = null, errorCode = null) {
        super(message);
        this.name = 'ApiError';
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
}

// 创建通用的请求函数
async function request(endpoint, method, data = null, useAuth = true) {
    const url = `${API_BASE_URL}${endpoint}`;
    const headers = {
        'Content-Type': 'application/json'
    };

    // 如果需要身份验证且有令牌，则添加Authorization头
    if (useAuth) {
        const token = localStorage.getItem(TOKEN_KEY);
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
    }

    const options = {
        method,
        headers,
        credentials: 'include' // 包含cookie
    };

    // 如果有数据，则添加到请求体
    if (data) {
        options.body = JSON.stringify(data);
    }

    try {
        console.log(url, method, data);
        const response = await fetch(url, options);

        let responseData;
        try {
            responseData = await response.json();
        } catch (jsonError) {
            // 如果响应不是JSON格式，创建一个简单的响应对象
            responseData = {
                message: response.statusText
            };
        }

        // 检查响应状态
        if (!response.ok) {
            // 处理不同的错误状态码
            let errorMessage = responseData.message || `请求失败: ${response.status}`;

            switch (response.status) {
                case 401:
                    // 未授权，清除令牌
                    authApi.logout();
                    errorMessage = responseData.message || '登录已过期，请重新登录';
                    break;
                case 403:
                    errorMessage = responseData.message || '没有权限执行此操作';
                    break;
                case 404:
                    errorMessage = responseData.message || '请求的资源不存在';
                    break;
                case 500:
                    errorMessage = responseData.message || '服务器内部错误，请稍后重试';
                    break;
            }

            throw new ApiError(errorMessage, response.status, responseData.errorCode);
        }

        return responseData;
    } catch (error) {
        if (error.name !== 'ApiError') {
            // 处理网络错误或其他非API错误
            throw new ApiError('网络错误，请检查您的网络连接', null, 'NETWORK_ERROR');
        }
        throw error;
    }
}

// 认证相关API
export const authApi = {
    // 登录
    async login(username, password) {
        try {
            const response = await request('/auth/login', 'POST', { username, password }, false);

            // 存储令牌和用户信息
            if (response.access_token) {
                localStorage.setItem(TOKEN_KEY, response.access_token);
            }
            if (response.refresh_token) {
                localStorage.setItem(REFRESH_TOKEN_KEY, response.refresh_token);
            }
            // 检查是否有用户信息，如果有则存储
            if (response.user) {
                localStorage.setItem(USER_INFO_KEY, JSON.stringify(response.user));
            } else if (response.username) {
                // 兼容可能的不同返回格式
                localStorage.setItem(USER_INFO_KEY, JSON.stringify({ username: response.username }));
            }

            // 触发登录事件
            this._emitAuthChangeEvent(true);

            return response;
        } catch (error) {
            console.error('登录失败:', error);
            throw error;
        }
    },

    // 注册
    async register(registrationData) {
        try {
            return await request('/auth/register', 'POST', registrationData, false);
        } catch (error) {
            console.error('注册失败:', error);
            throw error;
        }
    },

    // 刷新令牌
    async refreshToken() {
        try {
            const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

            if (!refreshToken) {
                throw new ApiError('没有刷新令牌', null, 'NO_REFRESH_TOKEN');
            }

            // 严格按照API文档规范，使用refresh_token作为参数名
            const response = await request('/auth/refresh', 'POST', { refresh_token: refreshToken }, false);

            // 更新令牌
            if (response.access_token) {
                localStorage.setItem(TOKEN_KEY, response.access_token);
            }
            if (response.refresh_token) {
                localStorage.setItem(REFRESH_TOKEN_KEY, response.refresh_token);
            }

            return response;
        } catch (error) {
            console.error('令牌刷新失败:', error);
            // 刷新失败，清除令牌
            this.logout();
            throw error;
        }
    },

    // 验证身份
    async verifyAuth() {
        try {
            // 严格按照API文档规范，使用POST方法和/verification端点
            const response = await request('/auth/verification', 'POST', null, true);

            return response;
        } catch (error) {
            console.error('身份验证失败:', error);
            throw error;
        }
    },

    // 登出
    logout() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        localStorage.removeItem(USER_INFO_KEY);

        // 触发登出事件
        this._emitAuthChangeEvent(false);
    },

    // 检查是否已登录
    isAuthenticated() {
        return !!localStorage.getItem(TOKEN_KEY);
    },

    // 获取当前令牌
    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },

    // 获取用户信息
    getUserInfo() {
        const userInfo = localStorage.getItem(USER_INFO_KEY);
        return userInfo ? JSON.parse(userInfo) : null;
    },

    // 更新用户信息
    updateUserInfo(userInfo) {
        localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
    },

    // 清除用户信息
    clearUserInfo() {
        localStorage.removeItem(USER_INFO_KEY);
    },

    // 触发认证状态变化事件
    _emitAuthChangeEvent(isAuthenticated) {
        const event = new CustomEvent('authChange', {
            detail: {
                isAuthenticated,
                userInfo: isAuthenticated ? this.getUserInfo() : null
            }
        });
        window.dispatchEvent(event);
    },

    // 监听认证状态变化
    onAuthChange(callback) {
        window.addEventListener('authChange', (event) => {
            callback(event.detail.isAuthenticated, event.detail.userInfo);
        });
    }
};

// 自动刷新令牌 - 可以根据需要使用
let refreshTimeout;

// 设置令牌自动刷新
export function setupAutoRefresh() {
    // 清除现有的刷新定时器
    if (refreshTimeout) {
        clearTimeout(refreshTimeout);
    }

    // 设置新的刷新定时器（在令牌过期前5分钟刷新）
    const refreshInterval = (25 * 60 * 1000); // 25分钟
    refreshTimeout = setTimeout(async () => {
        try {
            await authApi.refreshToken();
            // 刷新成功后，重新设置定时器
            setupAutoRefresh();
        } catch (error) {
            console.error('自动刷新令牌失败:', error);
        }
    }, refreshInterval);
}

// 取消自动刷新
export function cancelAutoRefresh() {
    if (refreshTimeout) {
        clearTimeout(refreshTimeout);
        refreshTimeout = null;
    }
}
