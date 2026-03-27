# Talk2Me-Forum Developer Guide

本文件写给后续开发者，目标是让任何新加入的人都能快速理解仓库结构、启动项目、运行测试，并在修改后维持一致的工程习惯。

如果你改动了以下内容，请同步更新本文件：

- 项目目录结构
- 启动命令
- 环境变量
- 测试入口
- 格式化或 lint 规则
- 前后端联调约定

## 1. 仓库总览

本仓库当前是一个前后端分离项目：

- 前端：`vue_frontend`
- 后端：`springboot_backend`
- 仓库根目录主要放公共文档和 `pre-commit` 配置

当前没有统一的 monorepo 任务入口。前后端各自独立运行，各自维护自己的启动命令和依赖。

## 2. 顶层目录说明

```text
Talk2Me-Forum/
├── AGENTS.md
├── plan.md
├── .pre-commit-config.yaml
├── vue_frontend/
└── springboot_backend/
```

说明：

- `AGENTS.md`：本维护文档
- `plan.md`：当前通知系统阶段性计划
- `.pre-commit-config.yaml`：仓库级格式化、校验和基础检查
- `vue_frontend/`：Vue 3 + Vite 前端
- `springboot_backend/`：Spring Boot 后端

## 3. 前端架构

前端位于 `vue_frontend/`，使用 Vue 3、Vue Router 4 和 Vite。

### 3.1 目录结构

```text
vue_frontend/src/
├── App.vue
├── main.js
├── router/
├── components/
├── views/
├── utils/
└── theme/
```

职责划分：

- `src/main.js`
  Vue 应用入口，挂载路由并注入主题变量
- `src/App.vue`
  顶层应用组件
- `src/router/index.js`
  路由定义和基础鉴权守卫
- `src/components/`
  通用组件，如 `Header.vue`、`AuthModal.vue`
- `src/views/`
  页面级视图，如首页、帖子详情、用户页、分区页
- `src/utils/api.js`
  HTTP API 封装，统一处理 token、错误和请求路径
- `src/utils/websocket.js`
  通知 WebSocket 客户端，当前为手写 STOMP 帧处理
- `src/theme/`
  全局样式变量和主题配置

### 3.2 当前前端设计特点

- 主要页面逻辑集中在 `views/`
- API 层是手写 fetch 封装，不是 axios 全量封装风格
- 登录态依赖 `localStorage` 中的 token
- 路由守卫逻辑较轻，复杂鉴权行为主要散落在业务层
- 通知实时能力走 `src/utils/websocket.js`

### 3.3 前端运行方式

在 `vue_frontend/` 目录下执行：

```bash
npm install
npm run dev
```

常用命令：

```bash
npm run dev
npm run build
npm run preview
npm run lint
```

当前 Vite 配置：

- 开发端口：`8900`
- 代理前缀：`/talk2me`
- 代理目标：`http://localhost:8099`

见：`vue_frontend/vite.config.js`

### 3.4 前端开发注意事项

1. API 基础路径当前硬编码在 `src/utils/api.js`：
   - `const API_BASE_URL = "/talk2me/api/v1";`
2. 通知 WebSocket 地址当前硬编码在 `src/utils/websocket.js`：
   - host 取浏览器地址
   - port 固定为 `8099`
   - path 固定为 `/talk2me/ws`
3. 这意味着前端对“后端端口 + context-path”有较强耦合，改后端部署方式时必须同步检查：
   - `vite.config.js`
   - `src/utils/api.js`
   - `src/utils/websocket.js`
4. 当前前端 README 中有部分历史描述与实际配置不一致，例如端口说明写的是 `3000`，实际配置是 `8900`。修改相关配置时优先以代码为准，并同步修正文档。
5. 写新功能时优先沿用现有目录习惯：
   - 页面逻辑进 `views/`
   - 通用能力进 `components/` 或 `utils/`
   - 主题变量放 `theme/`

### 3.5 前端代码风格和检查

前端当前有以下检查：

- ESLint：`npm run lint`
- Prettier：通过仓库根目录 `pre-commit` 执行

前端 `eslintConfig` 直接定义在 `vue_frontend/package.json` 中，当前规则较轻：

- `plugin:vue/vue3-essential`
- `eslint:recommended`
- 关闭 `vue/multi-word-component-names`

建议：

1. 提交前至少运行一次 `npm run lint`
2. 大范围改动前后运行 `pre-commit run --all-files`
3. 不要引入与现有风格完全不同的状态管理或请求层抽象，除非先做架构收敛

## 4. 后端架构

后端位于 `springboot_backend/`，使用 Spring Boot 3.2、Java 21、MyBatis-Plus、Spring Security、JWT、WebSocket 和 Redis。

### 4.1 目录结构

```text
springboot_backend/src/main/java/com/example/springboot_backend/
├── SpringbootBackendApplication.java
├── core/
│   ├── config/
│   ├── exception/
│   ├── model/
│   └── security/
└── talk2me/
    ├── controller/
    ├── model/
    │   ├── domain/
    │   └── vo/
    ├── repository/
    └── service/
        └── impl/
```

职责划分：

- `core/config`
  Spring 配置，如 CORS、WebSocket、MyBatis、OpenAPI、Redis 通知配置
- `core/security`
  JWT、认证过滤器、WebSocket STOMP 认证
- `core/model`
  通用返回体和基础实体
- `talk2me/controller`
  HTTP API 控制器
- `talk2me/service`
  业务接口
- `talk2me/service/impl`
  业务实现
- `talk2me/repository`
  MyBatis-Plus Mapper
- `talk2me/model/domain`
  数据库 DO
- `talk2me/model/vo`
  请求和响应 VO

### 4.2 当前后端分层约定

项目当前实践以以下分层为主：

1. Controller
2. Service / ServiceImpl
3. Repository
4. Domain / VO

仓库里已有规范文件：`springboot_backend/standard.md`

后续修改后端时，优先遵守这些既有约束：

- 使用构造函数注入，不用字段注入
- Controller 注入接口，不直接注入实现类
- Service 实现类显式 `implements` 接口
- 实现接口方法时加 `@Override`

注意：

- `standard.md` 中有一部分描述偏 JPA 风格，但本项目实际数据访问主要使用 MyBatis-Plus。维护时以“当前代码实际实现”优先，不要机械照搬历史文档。

### 4.3 后端运行方式

推荐优先使用 Maven Wrapper，在 `springboot_backend/` 目录下执行：

```bash
./mvnw spring-boot:run
```

常用命令：

```bash
./mvnw spring-boot:run
./mvnw test
mvn test
./mvnw validate
./mvnw -DskipTests package
```

说明：

- `validate` 会被仓库根目录 `pre-commit` 用于校验 `pom.xml`
- 打包命令已有现成批准前缀可用：`./mvnw -DskipTests package`
- 当前环境下如 `./mvnw test` 遇到 Mockito inline agent 问题，可优先使用 `mvn test`

### 4.4 后端环境配置

主要配置文件：

- `springboot_backend/src/main/resources/application.properties`
- `springboot_backend/template.env`
- `springboot_backend/.env`

默认关键点：

- `server.port` 默认是 `8099`
- `server.servlet.context-path` 默认是 `/talk2me`
- 数据库默认是内存 H2
- Redis 配置默认存在，但通知广播默认关闭
- `notification.redis.enabled=false`
- Python 联调脚本会优先读取 `springboot_backend/.env` 中的服务地址配置

注意事项：

1. 后端默认配置已对齐当前前端联调约定：`8099` + `/talk2me`。
2. 如果修改端口或 context-path，需要同步检查当前前端约定，否则前后端不会直接联通。
3. 联调前必须先统一这三个位置：
   - `springboot_backend` 运行配置
   - `vue_frontend/vite.config.js`
   - `vue_frontend/src/utils/api.js` 与 `src/utils/websocket.js`

### 4.5 通知系统相关结构

后端通知能力是当前重点模块之一，主要涉及：

- `NotificationController`
- `NotificationService`
- `NotificationRealtimeService`
- `NotificationRedisSubscriber`
- `NotificationRedisConfig`
- `WebSocketConfig`
- `StompAuthChannelInterceptor`

当前链路：

1. 业务事件产生通知
2. 通知先写数据库
3. 再走 WebSocket 实时推送
4. 如启用 Redis，则改为先发 Redis topic，再由订阅器推送 WebSocket

维护通知系统时，必须同时检查：

- 落库逻辑
- 未读数逻辑
- WS 推送结构
- Redis 开关两种模式
- 前端消费字段结构

## 5. 测试与验证

本仓库的测试不是单一入口，分为 Java 单测、Python 脚本联调、Node/CORS 辅助测试。

### 5.1 后端 Java 测试

位置：

- `springboot_backend/src/test/java/`

当前已有：

- `SpringbootBackendApplicationTests.java`
- `UserControllerTest.java`
- `UserServiceTest.java`

运行：

```bash
cd springboot_backend
./mvnw test
```

### 5.2 后端联调脚本

位置：

- `springboot_backend/test/smoke_check.py`
- `springboot_backend/test/ws_redis_notification_check.py`

作用：

- `smoke_check.py`
  覆盖注册、登录、发帖、回复、点赞、关注、通知查询与已读
- `ws_redis_notification_check.py`
  覆盖通知实时推送和 Redis 广播链路

运行：

```bash
cd springboot_backend
uv run test/smoke_check.py
uv run test/ws_redis_notification_check.py
```

说明：

- 这两个脚本是通知系统改动后的高优先级回归项
- 改动通知逻辑、认证逻辑、WS 路径、context-path 或 Redis 开关时，应该至少跑一次

### 5.3 CORS 辅助测试

位置：

- `springboot_backend/test/cors-test.js`
- `springboot_backend/test/cors-test.html`

用途：

- 验证 CORS 配置和预检请求

适用场景：

- 修改 `CorsConfig`
- 修改前端 origin
- 修改 context-path / 代理时

### 5.4 前端检查

前端当前没有单元测试框架接入。最基础的可执行检查是：

```bash
cd vue_frontend
npm run lint
npm run build
```

如果前端做了较大改动，建议最少执行：

1. `npm run lint`
2. `npm run build`
3. 手工验证关键页面
4. 配合后端联调通知路径

## 6. 代码风格与提交前检查

仓库根目录已有 `.pre-commit-config.yaml`。

当前会执行的检查包括：

- 去除尾部空格
- 文件结尾换行修复
- YAML / JSON 基础检查
- merge conflict 标记检查
- 大文件检查
- Prettier 格式化前端和 Markdown 文件
- Java 格式化
- 前端 lint
- `mvn validate`
- 资源文件中的敏感字段 grep 检查

推荐工作流：

```bash
pre-commit run --all-files
```

如果只改前端，至少执行：

```bash
cd vue_frontend
npm run lint
npm run build
```

如果只改后端，至少执行：

```bash
cd springboot_backend
./mvnw test
```

如果改动通知链路，再额外执行：

```bash
cd springboot_backend
uv run test/smoke_check.py
uv run test/ws_redis_notification_check.py
```

## 7. 常见坑和维护注意事项

### 7.1 配置与文档存在历史漂移

当前仓库里有几处文档和代码不一致：

1. 前端 README 提到端口 `3000`，实际 Vite 配置是 `8900`
2. 后端默认配置是 `8080 + 空 context-path`，但前端硬编码假设是 `8099 + /talk2me`
3. 后端 README 示例偏 Docker 部署视角，不完全等于本地开发默认值

结论：

- 联调时优先看实际代码和配置
- 修配置时同步修 README 和本文件

### 7.2 不要轻易扩大抽象层

当前代码已经有一套能运行的结构，但不算统一。

因此：

- 不要在一次普通需求里顺手引入新的请求库模式
- 不要同时混用多套状态管理思路
- 不要在后端同时引入新的 ORM 或消息中间件抽象

需要重构时，先在文档中写清“为什么要收敛”，再做逐步迁移。

### 7.3 通知相关改动要跨端验证

只改一端通常不够。凡是改动以下内容，都要做前后端联调：

- 通知字段
- token 处理
- WebSocket 连接参数
- context-path
- 用户页通知展示
- Header 未读数入口

### 7.4 历史文档优先级

建议使用以下优先级判断“哪个描述可信”：

1. 实际代码
2. 当前配置文件
3. 本文件 `AGENTS.md`
4. 模块 README
5. 更早期的设计文档

## 8. 推荐维护方式

后续维护本仓库时，建议坚持以下原则：

1. 改命令就改文档
2. 改路径就改联调说明
3. 改协议就改前后端消费说明
4. 改测试入口就改本文件和模块 README
5. 改通知系统就更新 `plan.md` 或新增对应计划文档

## 9. 最小开发者启动清单

第一次接手本仓库时，建议按这个顺序操作：

1. 阅读本文件
2. 阅读 `plan.md`
3. 启动后端
4. 启动前端
5. 确认前后端端口和 context-path 是否对齐
6. 跑前端 lint
7. 跑后端 test
8. 如果改通知系统，再跑两个通知联调脚本

建议命令：

```bash
cd springboot_backend
./mvnw spring-boot:run
```

新开一个终端：

```bash
cd vue_frontend
npm install
npm run dev
```

提交前：

```bash
pre-commit run --all-files
```
