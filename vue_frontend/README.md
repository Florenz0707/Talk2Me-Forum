# Talk2Me Forum Frontend

这是Talk2Me论坛的前端Vue.js项目。

## 项目结构

```
src/
├── components/        # Vue组件目录
├── views/            # 页面视图目录
│   ├── LoginView.vue # 登录页面
│   └── RegisterView.vue # 注册页面
├── router/           # 路由配置
│   └── index.js      # 路由定义
├── utils/            # 工具函数
│   └── api.js        # API请求封装
├── assets/           # 静态资源
├── App.vue           # 根组件
└── main.js           # 入口文件
```

## 技术栈

- Vue.js 3
- Vue Router 4
- Vite (构建工具)
- ESLint

## 安装和运行

### 1. 安装依赖

```bash
npm install
```

### 2. 开发模式运行

```bash
npm run dev
```

项目将在固定端口 **3000** 上启动（配置在 `vite.config.js` 文件中）

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产版本

```bash
npm run preview
```

### 5. 代码检查

```bash
npm run lint
```

## 组件规范

- 所有组件遵循template-script-style三段式结构
- 组件命名采用PascalCase格式
- script标签内使用export default语法并包含name属性
- template中使用正确的Vue指令和数据绑定语法
- style标签根据需要添加scoped属性
- JavaScript代码符合ESLint规范和Vue最佳实践

## API封装

API请求封装在`src/utils/api.js`中，提供了以下功能：

- 统一的请求处理
- 身份验证管理
- 错误处理
- 令牌管理

## 配置说明

### 固定端口配置

项目使用Vite作为构建工具，并在`vite.config.js`文件中固定了开发服务器端口为3000：

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8900 // 固定端口为3000
  }
})
```

### 路由配置

路由配置在`src/router/index.js`中，定义了以下路由：

- `/login` - 登录页面
- `/register` - 注册页面

## 注意事项

- 确保后端服务在`http://127.0.0.1:8099`运行
- 项目使用了Font Awesome图标库，通过CDN引入
- 登录和注册功能需要后端API支持
