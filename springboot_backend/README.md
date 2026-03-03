# Talk2Me Forum Backend

Spring Boot论坛系统后端API

## 技术栈

- Java 21
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- Spring Security + JWT
- H2 Database (开发环境)
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

## API文档

启动应用后访问Swagger UI查看完整API文档。
