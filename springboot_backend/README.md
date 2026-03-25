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

- API Base URL: http://localhost:8099/talk2me
- Swagger UI: http://localhost:8099/talk2me/swagger-ui/index.html
- H2 Console: http://localhost:8099/talk2me/h2-console

## 功能模块

- 用户认证（注册/登录/JWT刷新）
- 分区管理
- 帖子CRUD（支持软删除）
- 回复系统（楼层制）
- 点赞功能（帖子/回复）
- 关注/取关
- 通知系统（点赞/回复/关注）
- 通知实时推送（WebSocket + Redis Pub/Sub）

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
uv run test/smoke_check.py
uv run test/ws_redis_notification_check.py
```

## API文档

启动应用后访问Swagger UI查看完整API文档。
