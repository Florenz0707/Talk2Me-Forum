# Talk2Me Forum Backend

Spring Boot论坛系统后端API

## 技术栈

- Java 21
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- Spring Security + JWT
- H2 Database (开发环境)
- Redis（通知广播）
- WebSocket STOMP（通知实时推送）
- OpenAPI/Swagger

## 快速开始

### Docker运行

```bash
docker build -t springboot-backend .
docker run -d -p 8099:8099 --env-file .env --name springboot-backend springboot-backend
```

### 访问地址

- API Base URL: "http://{YOUR_ADDRESS}/talk2me"
- Swagger UI: "http://{YOUR_ADDRESS}/talk2me/swagger-ui/index.html"
- H2 Console: "http://{YOUR_ADDRESS}/talk2me/h2-console"

默认开发配置已对齐以上地址：

- `server.port={PORT}`
- `server.servlet.context-path=/talk2me`

## 功能模块

- 用户认证（注册/登录/JWT刷新）
- 分区管理
- 帖子CRUD（支持软删除）
- 回复系统（楼层制）
- 点赞功能（帖子/回复）
- 关注/取关
- 通知系统（点赞/回复/关注/关注者发帖）
- 通知实时推送（WebSocket + Redis Pub/Sub）
- 用户自定义配置（JSON）

## 用户自定义配置（Preferences）

用户配置存储在 `users.preferences`（JSON 字符串）字段。

接口：

- `GET /api/v1/users/preferences`：获取当前用户完整配置（含默认值补齐）
- `PUT /api/v1/users/preferences`：全量更新配置
- `PATCH /api/v1/users/preferences`：局部更新配置（基于当前配置做 merge）

当前默认结构：

```json
{
  "theme": "system",
  "language": "zh-CN",
  "notification": {
    "enableWs": true,
    "muteLike": false,
    "muteReply": false,
    "muteFollow": false,
    "muteFolloweePost": false
  }
}
```

约束：

- 配置仅允许白名单字段
- `theme` 仅支持：`system` / `light` / `dark`
- `notification` 子字段必须为布尔值
- 配置体最大长度由 `USER_PREFERENCES_MAX_LENGTH` 控制（默认 8192 bytes）

当前约定（重要）：

- `notification` 下的静音开关（如 `muteLike` / `muteReply` / `muteFollow` / `muteFolloweePost`）目前仅作为前端展示与交互配置。
- 通知“是否创建/是否实时推送”当前仍按后端既有业务规则执行，不由这些配置项在后端侧拦截。

## 通知实时推送（WS + Redis）

### 关键配置

```properties
# 是否启用Redis广播（默认false，单机直推）
NOTIFICATION_REDIS_ENABLED=true
# Redis通知topic
NOTIFICATION_REDIS_TOPIC=talk2me:notification:events
```

说明：

- `NOTIFICATION_REDIS_ENABLED=false`：通知写库后本机直接推送到 WebSocket。
- `NOTIFICATION_REDIS_ENABLED=true`：通知写库后先发 Redis topic，再由订阅器推送 WebSocket（适合多实例）。

### WebSocket 接入

- 握手端点：`/ws`（支持 SockJS）
- 用户订阅地址：`/user/queue/notifications`
- STOMP `CONNECT` 需携带 Header：`Authorization: Bearer <access_token>`

### WebSocket 通知语义（重要）

通知 WS 是“增量事件流”，不是“通知列表快照”：

- 连接成功后不会自动全量回放历史通知。
- 首屏应先调用 `GET /api/v1/notifications` 拉取列表，再消费 WS 增量。
- 同一个通知主键 `id` 可收到不同 `eventType`，用于表达状态变化。

`eventType` 语义：

- `CREATED`：通知创建/生效，前端应插入或更新该通知。
- `DELETED`：通知失效（如取消点赞、删除回复导致通知撤销），前端应删除该通知。

幂等处理建议：

- 以通知 `id` 作为唯一键去重。
- 对 `CREATED` 执行 upsert，对 `DELETED` 执行 remove（不存在时忽略）。

与用户偏好配置的关系：

- 当前阶段，通知过滤效果应在前端实现（例如前端根据 `users.preferences.notification.*` 决定展示或静音）。
- 后端暂不根据用户偏好跳过通知创建或跳过 WS 推送。

### 通知相关接口

- `GET /api/v1/notifications`
- `GET /api/v1/notifications/unread-count`
- `POST /api/v1/notifications/{notificationId}/read`
- `POST /api/v1/notifications/read-all`

## 测试脚本

在 `springboot_backend/test` 下：

- HTTP冒烟：`smoke_check.py`
  - 覆盖发帖、回复、点赞、关注、通知查询与已读
- WS+Redis实时通知：`ws_redis_notification_check.py`
  - 覆盖点赞通知、评论通知、关注通知
  - 覆盖取关接口与“取关后未读数不增加”断言

运行示例：

```bash
cd springboot_backend
mvn test
uv run test/smoke_check.py
uv run test/ws_redis_notification_check.py
```

Python 测试脚本默认读取 `springboot_backend/.env` 中的服务配置，当前默认值为：

```env
SERVER_PORT=8099
SERVER_CONTEXT_PATH=/talk2me
API_BASE_PATH=/api/v1
WEBSOCKET_ENDPOINT=/ws
```

## API文档

启动应用后访问Swagger UI查看完整API文档。
