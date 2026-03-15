// API服务层 - 处理与后端的所有HTTP请求

// API基础URL
const API_BASE_URL = "http://127.0.0.1:8099/talk2me/api/v1";

// 解码 JWT payload（不验签，仅用于读取 subject 等公开信息）
function decodeJwtPayload(token) {
  try {
    const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(atob(base64));
  } catch {
    return null;
  }
}

// 存储键名
const TOKEN_KEY = "auth_token";
const REFRESH_TOKEN_KEY = "refresh_token";
const USER_INFO_KEY = "user_info";

// 错误类型定义
export class ApiError extends Error {
  constructor(message, statusCode = null, errorCode = null) {
    super(message);
    this.name = "ApiError";
    this.statusCode = statusCode;
    this.errorCode = errorCode;
  }
}

// 创建通用的请求函数
async function request(endpoint, method, data = null, useAuth = true) {
  const url = `${API_BASE_URL}${endpoint}`;
  const headers = {
    "Content-Type": "application/json",
  };

  // 如果需要身份验证且有令牌，则添加Authorization头
  if (useAuth) {
    const token = localStorage.getItem(TOKEN_KEY);
    console.log("Token from localStorage:", token ? "存在" : "不存在");
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
      console.log("Authorization header:", headers["Authorization"]);
    }
  }

  const options = {
    method,
    headers,
    credentials: "include", // 包含cookie
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
        message: response.statusText,
      };
    }

    // 检查响应状态
    if (!response.ok) {
      // 处理不同的错误状态码
      let errorMessage = responseData.message || `请求失败: ${response.status}`;

      switch (response.status) {
        case 401:
          // 未授权，清除令牌
          console.log("收到401错误，清除token");
          localStorage.removeItem(TOKEN_KEY);
          localStorage.removeItem(REFRESH_TOKEN_KEY);
          localStorage.removeItem(USER_INFO_KEY);
          errorMessage = responseData.message || "登录已过期，请重新登录";
          break;
        case 403:
          errorMessage = responseData.message || "没有权限执行此操作";
          break;
        case 404:
          errorMessage = responseData.message || "请求的资源不存在";
          break;
        case 500:
          errorMessage = responseData.message || "服务器内部错误，请稍后重试";
          break;
      }

      throw new ApiError(errorMessage, response.status, responseData.errorCode);
    }

    // 返回后端返回的完整响应数据
    return responseData;
  } catch (error) {
    if (error.name !== "ApiError") {
      // 处理网络错误或其他非API错误
      throw new ApiError("网络错误，请检查您的网络连接", null, "NETWORK_ERROR");
    }
    throw error;
  }
}

// ==================== 一、auth-controller (认证授权控制器) ====================
export const authApi = {
  // 1. 获取身份验证码 - POST /api/v1/auth/verification
  // 用于注册、登录、找回密码等场景
  async getVerification(data) {
    try {
      const response = await request("/auth/verification", "POST", data, false);
      return response;
    } catch (error) {
      console.error("获取验证码失败:", error);
      throw error;
    }
  },

  // 2. 用户账号注册 - POST /api/v1/auth/register
  async register(registrationData) {
    try {
      return await request("/auth/register", "POST", registrationData, false);
    } catch (error) {
      console.error("注册失败:", error);
      throw error;
    }
  },

  // 3. 刷新用户身份凭证 - POST /api/v1/auth/refresh
  async refreshToken() {
    try {
      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

      if (!refreshToken) {
        throw new ApiError("没有刷新令牌", null, "NO_REFRESH_TOKEN");
      }

      const response = await request(
        "/auth/refresh",
        "POST",
        { refresh_token: refreshToken },
        false,
      );

      // 更新令牌 - 根据后端返回的数据结构
      if (response.data && response.data.access_token) {
        localStorage.setItem(TOKEN_KEY, response.data.access_token);
      } else if (response.access_token) {
        localStorage.setItem(TOKEN_KEY, response.access_token);
      }

      if (response.data && response.data.refresh_token) {
        localStorage.setItem(REFRESH_TOKEN_KEY, response.data.refresh_token);
      } else if (response.refresh_token) {
        localStorage.setItem(REFRESH_TOKEN_KEY, response.refresh_token);
      }

      return response;
    } catch (error) {
      console.error("令牌刷新失败:", error);
      this.logout();
      throw error;
    }
  },

  // 4. 用户登录接口 - POST /api/v1/auth/login
  async login(username, password) {
    try {
      const response = await request(
        "/auth/login",
        "POST",
        { username, password },
        false,
      );

      // 根据后端返回的数据结构存储令牌和用户信息
      // 后端返回格式: { code, message, data: { access_token, refresh_token, user } }
      console.log("登录响应:", response);
      if (response.data) {
        if (response.data.access_token) {
          localStorage.setItem(TOKEN_KEY, response.data.access_token);
          console.log(
            "Token已存储:",
            response.data.access_token.substring(0, 20) + "...",
          );
        }
        if (response.data.refresh_token) {
          localStorage.setItem(REFRESH_TOKEN_KEY, response.data.refresh_token);
        }
        if (response.data.user) {
          localStorage.setItem(
            USER_INFO_KEY,
            JSON.stringify(response.data.user),
          );
        }
      } else {
        // 兼容直接返回token的情况
        if (response.access_token) {
          localStorage.setItem(TOKEN_KEY, response.access_token);
          console.log(
            "Token已存储:",
            response.access_token.substring(0, 20) + "...",
          );
        }
        if (response.refresh_token) {
          localStorage.setItem(REFRESH_TOKEN_KEY, response.refresh_token);
        }
        if (response.user) {
          localStorage.setItem(USER_INFO_KEY, JSON.stringify(response.user));
        } else if (response.username) {
          localStorage.setItem(
            USER_INFO_KEY,
            JSON.stringify({ username: response.username }),
          );
        }
      }

      this._emitAuthChangeEvent(true);
      return response;
    } catch (error) {
      console.error("登录失败:", error);
      throw error;
    }
  },

  // 5. 登出
  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_INFO_KEY);
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

  // 从 JWT token 中解码用户名（token 由后端签发，sub 字段为 username）
  getUsernameFromToken() {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) return null;
    const payload = decodeJwtPayload(token);
    return payload ? payload.sub : null;
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
    const event = new CustomEvent("authChange", {
      detail: {
        isAuthenticated,
        userInfo: isAuthenticated ? this.getUserInfo() : null,
      },
    });
    window.dispatchEvent(event);
  },

  // 监听认证状态变化
  onAuthChange(callback) {
    window.addEventListener("authChange", (event) => {
      callback(event.detail.isAuthenticated, event.detail.userInfo);
    });
  },
};

// ==================== 二、post-controller (帖子管理控制器) ====================
export const postApi = {
  // 1. 查询获取单条帖子的详细信息 - GET /api/v1/posts/{id}
  // 返回格式: { code, message, data: PostDO }
  async getPostById(id) {
    try {
      const response = await request(`/posts/${id}`, "GET", null, true);
      // 返回完整的响应数据，让调用者处理data字段
      return response;
    } catch (error) {
      console.error("获取帖子详情失败:", error);
      throw error;
    }
  },

  // 2. 全量更新/修改对应帖子的内容 - PUT /api/v1/posts/{id}
  // 请求体: { title, content }
  // 返回格式: { code, message, data: PostDO }
  async updatePost(id, postData) {
    try {
      const response = await request(`/posts/${id}`, "PUT", postData, true);
      return response;
    } catch (error) {
      console.error("更新帖子失败:", error);
      throw error;
    }
  },

  // 3. 删除对应的帖子资源 - DELETE /api/v1/posts/{id}
  // 返回格式: { code, message }
  async deletePost(id) {
    try {
      const response = await request(`/posts/${id}`, "DELETE", null, true);
      return response;
    } catch (error) {
      console.error("删除帖子失败:", error);
      throw error;
    }
  },

  // 4. 批量查询帖子列表 - GET /api/v1/posts
  // 查询参数: sectionId, page, size
  // 返回格式: { code, message, data: PagePostDO }
  async getPosts(params = {}) {
    try {
      // 构建查询参数
      const queryParams = new URLSearchParams();
      if (params.page) queryParams.append("page", params.page);
      if (params.size) queryParams.append("size", params.size);
      if (params.sectionId) queryParams.append("sectionId", params.sectionId);
      if (params.sortBy) queryParams.append("sortBy", params.sortBy);

      const endpoint = queryParams.toString()
        ? `/posts?${queryParams.toString()}`
        : "/posts";

      const response = await request(endpoint, "GET", null, true);
      return response;
    } catch (error) {
      console.error("获取帖子列表失败:", error);
      throw error;
    }
  },

  // 5. 创建/发布新帖子 - POST /api/v1/posts
  // 请求体: { sectionId, title, content }
  // 返回格式: { code, message, data: PostDO }
  async createPost(postData) {
    try {
      const response = await request("/posts", "POST", postData, true);
      return response;
    } catch (error) {
      console.error("创建帖子失败:", error);
      throw error;
    }
  },
};

// ==================== 三、reply-controller (回复/评论控制器) ====================
export const replyApi = {
  // 1. 查询该帖子下的全部回复/评论列表 - GET /api/v1/posts/{postId}/replies
  // 查询参数: page, size
  // 返回格式: { code, message, data: PageReplyDO }
  async getRepliesByPostId(postId, params = {}) {
    try {
      // 构建查询参数
      const queryParams = new URLSearchParams();
      if (params.page) queryParams.append("page", params.page);
      if (params.size) queryParams.append("size", params.size);

      const endpoint = queryParams.toString()
        ? `/posts/${postId}/replies?${queryParams.toString()}`
        : `/posts/${postId}/replies`;

      const response = await request(endpoint, "GET", null, true);
      return response;
    } catch (error) {
      console.error("获取回复列表失败:", error);
      throw error;
    }
  },

  // 2. 发布新的评论/回复 - POST /api/v1/posts/{postId}/replies
  // 请求体: { content }
  // 返回格式: { code, message, data: ReplyDO }
  async createReply(postId, replyData) {
    try {
      const response = await request(
        `/posts/${postId}/replies`,
        "POST",
        replyData,
        true,
      );
      return response;
    } catch (error) {
      console.error("创建回复失败:", error);
      throw error;
    }
  },

  // 3. 删除对应的评论/回复内容 - DELETE /api/v1/replies/{id}
  // 返回格式: { code, message }
  async deleteReply(id) {
    try {
      const response = await request(`/replies/${id}`, "DELETE", null, true);
      return response;
    } catch (error) {
      console.error("删除回复失败:", error);
      throw error;
    }
  },
};

// ==================== 四、section-controller (板块/分区控制器) ====================
export const sectionApi = {
  // 1. 获取全部分区/板块的列表 - GET /api/v1/sections
  // 返回格式: { code, message, data: SectionDO[] }
  async getAllSections() {
    try {
      const response = await request("/sections", "GET", null, true);
      return response;
    } catch (error) {
      console.error("获取板块列表失败:", error);
      throw error;
    }
  },

  // 2. 获取单个板块的详细信息 - GET /api/v1/sections/{id}
  // 返回格式: { code, message, data: SectionDO }
  async getSectionById(id) {
    try {
      const response = await request(`/sections/${id}`, "GET", null, false);
      return response;
    } catch (error) {
      console.error("获取板块详情失败:", error);
      throw error;
    }
  },
};

// ==================== 五、like-controller (点赞控制器) ====================
export const likeApi = {
  // 点赞 - POST /api/v1/likes
  // 请求体: { targetType, targetId }
  // targetType: "post" | "reply"
  async like(likeData) {
    try {
      const response = await request("/likes", "POST", likeData, true);
      return response;
    } catch (error) {
      console.error("点赞失败:", error);
      throw error;
    }
  },

  // 取消点赞 - DELETE /api/v1/likes
  // 查询参数: targetType, targetId
  async unlike(targetType, targetId) {
    try {
      const queryParams = new URLSearchParams();
      queryParams.append("targetType", targetType);
      queryParams.append("targetId", targetId);

      const endpoint = `/likes?${queryParams.toString()}`;
      const response = await request(endpoint, "DELETE", null, true);
      return response;
    } catch (error) {
      console.error("取消点赞失败:", error);
      throw error;
    }
  },
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
  const refreshInterval = 25 * 60 * 1000; // 25分钟
  refreshTimeout = setTimeout(async () => {
    try {
      await authApi.refreshToken();
      // 刷新成功后，重新设置定时器
      setupAutoRefresh();
    } catch (error) {
      console.error("自动刷新令牌失败:", error);
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

// 默认导出所有API
export default {
  auth: authApi,
  post: postApi,
  reply: replyApi,
  section: sectionApi,
  like: likeApi,
};
