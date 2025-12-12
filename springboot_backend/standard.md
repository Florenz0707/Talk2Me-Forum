# 项目开发规范

## 1. 项目架构

### 1.1 分层结构
项目采用标准的 **Controller-Service-Repository** 三层架构：

```
Controller层 (控制器层)
    ↓
Service层 (业务逻辑层)
    ↓
Repository层 (数据访问层)
    ↓
Database (数据库)
```

### 1.2 包结构规范
代码包结构应按照以下方式组织：

```
com.example.springboot_backend.talk2me.{模块名}/
├── controller/          # 控制器层
├── service/            # 服务层（接口和实现）
│   ├── I{Service}.java # 服务接口（命名：I + 服务名）
│   └── {Service}.java  # 服务实现类
├── repository/          # 数据访问层
├── model/              # 数据模型层
│   ├── vo/            # View Object - 返回给前端的模型
│   ├── bo/            # Business Object - 业务充血模型
│   └── domain/        # Domain Object - 数据库实体模型（DO）
└── utils/             # 工具类
```

## 2. 数据模型规范

### 2.1 Model分类
Model应分为三类，各司其职：

- **VO (View Object)**: 返回模型
  - 用于Controller返回给前端的数据结构
  - 只包含前端需要的字段
  - 位于 `model/vo/` 包下

- **BO (Business Object)**: 业务充血模型
  - 包含业务逻辑的领域对象
  - 可以包含业务方法
  - 位于 `model/bo/` 包下

- **DO (Domain Object)**: 数据库模型
  - 对应数据库表的实体类
  - 使用JPA注解或MyBatis映射
  - 位于 `model/domain/` 包下
  - 命名规范：`{实体名}DO.java`，如 `UserDO.java`

### 2.2 实体类规范
- 所有DO类应继承 `BaseEntity`（包含id、createTime、updateTime等通用字段）
- 使用 `@MappedSuperclass` 注解标注 `BaseEntity`，避免继承层次问题
- DO类使用 `@Entity` 和 `@Table` 注解

## 3. 依赖注入规范

### 3.1 禁止使用 @Autowired 字段注入
**不允许**使用 `@Autowired` 进行字段注入：

```java
// ❌ 错误示例
@Autowired
private UserService userService;
```

### 3.2 使用构造函数注入
**必须**使用构造函数注入：

```java
// ✅ 正确示例
public class UserController {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }
}
```

### 3.3 Service接口注入
Controller中应注入Service接口，而不是实现类：

```java
// ✅ 正确示例
public class UserController {
    private final IUserService userService;  // 注入接口

    public UserController(IUserService userService) {
        this.userService = userService;
    }
}
```

## 4. Service层规范

### 4.1 接口命名
- Service接口命名：`I{ServiceName}`，如 `IAuthService`
- Service实现类命名：`{ServiceName}`，如 `AuthService`
- Service实现类必须实现对应的接口

### 4.2 接口定义
```java
public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
```

### 4.3 实现类
```java
@Service
public class AuthService implements IAuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        // 实现逻辑
    }
}
```

### 4.4 @Override注解规范
**必须**在实现接口方法时添加 `@Override` 注解：

- 明确标识这是接口方法的实现
- 编译时检查方法签名是否正确
- 提高代码可读性和维护性

```java
// ✅ 正确示例
@Override
public AuthResponse register(RegisterRequest request) {
    // 实现逻辑
}

// ❌ 错误示例（缺少@Override注解）
public AuthResponse register(RegisterRequest request) {
    // 实现逻辑
}
```

**注意**：
- 所有实现接口的方法都必须添加 `@Override` 注解
- 重写父类方法时也必须添加 `@Override` 注解
- 这是Java编程的最佳实践，有助于避免方法签名错误

## 5. Controller层规范

### 5.1 基本规范
- 使用 `@RestController` 注解
- 使用 `@RequestMapping` 定义基础路径
- 方法使用 `@GetMapping`、`@PostMapping` 等注解
- 使用 `@Valid` 进行参数校验

### 5.2 示例
```java
@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // 处理逻辑
    }
}
```

## 6. Repository层规范

### 6.1 JPA Repository
- 继承 `JpaRepository<Entity, ID>`
- 使用 `@Repository` 注解
- 方法命名遵循Spring Data JPA规范

```java
@Repository
public interface UserRepository extends JpaRepository<UserDO, Long> {
    Optional<UserDO> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

### 6.2 MyBatis Mapper（如使用MyBatis）
- 接口使用 `@Mapper` 注解
- XML映射文件放在 `resources/mapper/` 目录下
- 命名规范：`{Entity}Mapper.java` 和 `{Entity}Mapper.xml`

## 7. 异常处理规范

### 7.1 业务异常
- 使用 `RuntimeException` 或其子类
- 异常信息应清晰明确

```java
if (userRepository.existsByUsername(username)) {
    throw new RuntimeException("Username is already taken!");
}
```

### 7.2 Controller异常处理
- 使用 `try-catch` 捕获异常
- 返回合适的HTTP状态码和错误信息

```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    try {
        authService.register(request);
        return ResponseEntity.ok().build();
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
```

## 8. 代码风格规范

### 8.1 命名规范
- **类名**: 大驼峰命名，如 `UserController`、`AuthService`
- **方法名**: 小驼峰命名，如 `getUserById`、`registerUser`
- **变量名**: 小驼峰命名，如 `userName`、`authService`
- **常量名**: 全大写下划线分隔，如 `MAX_RETRY_COUNT`
- **包名**: 全小写，单词间用点分隔

### 8.2 注释规范
- 类和方法应添加JavaDoc注释
- 复杂业务逻辑应添加行内注释
- 使用中文注释说明业务逻辑

### 8.3 代码格式
- 使用4个空格缩进（不使用Tab）
- 每行代码不超过120个字符
- 方法之间空一行
- 导入语句按字母顺序排列

## 9. 安全规范

### 9.1 JWT认证
- 使用JWT Bearer Token进行认证
- Token放在请求头：`Authorization: Bearer <token>`
- 实现Refresh Token机制

### 9.2 密码安全
- 使用BCrypt加密存储密码
- 不在日志中输出敏感信息
- 使用HTTPS传输敏感数据

## 10. 数据库规范

### 10.1 当前实现
- 当前项目使用 **JPA** 进行数据访问
- 支持MySQL和H2数据库

### 10.2 未来迁移（如需要）
- 如需迁移到MyBatis，需要：
  1. 添加MyBatis依赖
  2. 创建Mapper接口和XML文件
  3. 移除JPA相关注解
  4. 配置MyBatis扫描路径

## 11. 其他规范

### 11.1 事务管理
- Service层方法使用 `@Transactional` 注解
- 只读操作使用 `@Transactional(readOnly = true)`

### 11.2 日志规范
- 使用SLF4J + Logback
- 日志级别：ERROR > WARN > INFO > DEBUG
- 关键业务操作记录日志

### 11.3 环境变量
- 敏感配置使用环境变量
- 提供 `template.env` 作为配置模板
- 不将 `.env` 文件提交到版本控制
