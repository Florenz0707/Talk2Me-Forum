# CORS 跨域访问测试

此目录包含用于测试 Spring Boot 后端 CORS 跨域访问配置的测试脚本。

## 文件说明

- `cors-test.html` - 浏览器端测试页面，可以在浏览器中打开进行可视化测试
- `cors-test.js` - Node.js 命令行测试脚本
- `README.md` - 本文件

## 使用方法

### 方法一：浏览器测试（推荐）

1. 启动 Spring Boot 应用
2. 在浏览器中打开 `test/cors-test.html` 文件
3. 配置 API URL 和其他参数
4. 点击"发送注册请求"按钮
5. 查看结果和 CORS 响应头

**注意：**
- 如果 HTML 文件通过 `file://` 协议打开，某些浏览器可能会限制跨域请求
- 建议使用 HTTP 服务器提供 HTML 文件，例如：
  ```bash
  # 使用 Python 3
  python -m http.server 3000

  # 或使用 Node.js http-server
  npx http-server -p 3000
  ```
- 然后在浏览器中访问 `http://localhost:3000/test/cors-test.html`

### 方法二：Node.js 命令行测试

1. 确保已安装 Node.js（推荐 18+ 版本，内置 fetch API）
2. 如果使用较旧版本的 Node.js，需要安装 `node-fetch`：
   ```bash
   npm install node-fetch
   ```
3. 运行测试脚本：
   ```bash
   node test/cors-test.js
   ```

4. 可以通过环境变量自定义配置：
   ```bash
   API_BASE_URL=http://localhost:8080 \
   CONTEXT_PATH=/talk2me \
   TEST_USERNAME=myuser \
   TEST_PASSWORD=mypass \
   node test/cors-test.js
   ```

## 测试内容

### 1. 注册请求测试 (POST)
- 发送 POST 请求到 `/api/v1/auth/register`
- 检查响应状态码
- 验证 CORS 响应头：
  - `Access-Control-Allow-Origin`
  - `Access-Control-Allow-Methods`
  - `Access-Control-Allow-Headers`
  - `Access-Control-Allow-Credentials`
  - `Access-Control-Max-Age`
  - `Access-Control-Expose-Headers`

### 2. 预检请求测试 (OPTIONS)
- 发送 OPTIONS 请求（CORS 预检请求）
- 验证服务器是否正确响应预检请求
- 检查 CORS 响应头

## 预期结果

### 成功情况
- HTTP 状态码：200 或 400（如果用户名已存在）
- 响应头中包含正确的 CORS 头信息
- 响应体包含注册结果或错误信息

### 失败情况
- CORS 错误：浏览器控制台显示 CORS 相关错误
- 网络错误：服务器未运行或 URL 配置错误
- 认证错误：如果请求需要认证但未提供 token

## 环境变量配置

确保后端应用的 CORS 配置正确设置（参考 `template.env`）：

```env
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=*
CORS_ALLOW_CREDENTIALS=true
CORS_MAX_AGE=3600
```

## 故障排除

### 问题：CORS 错误
- 检查 `CORS_ALLOWED_ORIGINS` 是否包含请求的源
- 检查 `CORS_ALLOW_CREDENTIALS` 设置（如果为 true，不能使用 `*` 作为源）
- 确认 `CorsConfig` 类已正确配置并注入到 `SecurityConfig`

### 问题：404 Not Found
- 检查 API URL 是否正确
- 检查 `CONTEXT_PATH` 配置是否正确
- 确认后端应用已启动

### 问题：401 Unauthorized
- 注册接口应该不需要认证，检查 `SecurityConfig` 中的 `permitAll()` 配置

## 相关文件

- `src/main/java/com/example/springboot_backend/core/config/CorsConfig.java` - CORS 配置类
- `src/main/java/com/example/springboot_backend/core/security/SecurityConfig.java` - Spring Security 配置
- `template.env` - 环境变量配置模板
