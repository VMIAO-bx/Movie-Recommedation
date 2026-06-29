# 影视推荐系统 - 后端开发文档

> **技术栈**: Spring Boot 2.7.18 + MyBatis Plus 3.5.5 + MySQL + JWT
> **开发日期**: 2026-06-12
> **运行状态**: ✅ 正常运行于 `http://localhost:8080`

---

## 📁 项目结构

```
movie-backend/
├── pom.xml
├── mvnw / mvnw.cmd                         # Maven Wrapper
├── start.sh                                # 一键启动脚本
├── .mvn/wrapper/
│   ├── maven-wrapper.jar
│   └── maven-wrapper.properties
└── src/
    ├── main/resources/
    │   ├── application.yml                 # 端口8080 + MySQL + JWT配置
    │   └── sql/init.sql                    # 建库建表SQL脚本
    └── main/java/com/moviebackend/
        ├── MovieBackendApplication.java    # 启动类
        ├── common/
        │   ├── Result.java                 # 统一响应 {code, message, data}
        │   └── GlobalExceptionHandler.java # 全局异常处理
        ├── config/
        │   ├── CorsConfig.java             # 跨域（允许所有来源）
        │   ├── MyBatisPlusConfig.java      # 分页插件
        │   ├── PasswordConfig.java         # BCrypt加密
        │   ├── WebMvcConfig.java           # 拦截器注册
        │   └── MyMetaObjectHandler.java    # createTime自动填充
        ├── interceptor/
        │   └── LoginInterceptor.java       # JWT认证拦截器
        ├── util/
        │   ├── JwtUtil.java                # JWT生成/解析/校验
        │   └── CurrentUserHolder.java      # ThreadLocal用户上下文
        ├── entity/
        │   ├── User.java                   # 用户实体
        │   ├── Favorite.java               # 收藏实体
        │   └── Watchlist.java              # 想看/看过实体
        ├── dto/
        │   ├── RegisterRequest.java        # 注册请求
        │   ├── LoginRequest.java           # 登录请求
        │   ├── FavoriteRequest.java        # 收藏请求
        │   └── WatchRequest.java           # 标记请求
        ├── mapper/
        │   ├── UserMapper.java
        │   ├── FavoriteMapper.java
        │   └── WatchlistMapper.java
        ├── service/
        │   ├── UserService.java / impl/UserServiceImpl.java
        │   ├── FavoriteService.java / impl/FavoriteServiceImpl.java
        │   └── WatchlistService.java / impl/WatchlistServiceImpl.java
        └── controller/
            ├── IndexController.java        # 根路径 + 全局错误页
            ├── UserController.java         # 注册/登录/个人信息
            ├── FavoriteController.java     # 收藏增删查
            └── WatchlistController.java    # 标记增改查
```

---

## 🗄️ 数据库设计

**数据库**: `movie_recommendation`（UTF-8编码）

### user 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) UNIQUE | 用户名 |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| nickname | VARCHAR(100) | 昵称 |
| avatar | VARCHAR(500) | 头像URL |
| create_time | DATETIME | 注册时间 |

### favorite 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| user_id | BIGINT | 用户ID |
| movie_id | VARCHAR(50) | 电影ID |
| movie_title | VARCHAR(200) | 电影标题 |
| movie_poster | VARCHAR(500) | 海报URL |
| create_time | DATETIME | 收藏时间 |

> 唯一约束: `(user_id, movie_id)` — 同一用户不能重复收藏同一电影

### watchlist 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| user_id | BIGINT | 用户ID |
| movie_id | VARCHAR(50) | 电影ID |
| movie_title | VARCHAR(200) | 电影标题 |
| movie_poster | VARCHAR(500) | 海报URL |
| status | VARCHAR(20) | 想看 / 看过 |
| create_time | DATETIME | 创建时间 |

> 唯一约束: `(user_id, movie_id)` — 同一用户对同一电影只有一条标记记录

---

## 📡 API 接口文档

### 统一响应格式

```json
{
  "code": 200,       // 200成功 / 400参数错误 / 401未认证 / 404不存在 / 500服务器错误
  "message": "xxx",  // 提示信息
  "data": {}         // 响应数据
}
```

### 用户模块

| 方法 | 路径 | 认证 | 请求体 | 响应说明 |
|------|------|------|--------|----------|
| POST | `/api/user/register` | ❌ | `{username, password, nickname}` | 注册成功返回用户信息 |
| POST | `/api/user/login` | ❌ | `{username, password}` | 返回 `{token, user}` |
| GET | `/api/user/info` | ✅ | — | 返回当前用户信息 |
| PUT | `/api/user/info` | ✅ | `{nickname?, avatar?}` | 返回更新后的用户信息 |

### 收藏模块

| 方法 | 路径 | 认证 | 请求体 | 响应说明 |
|------|------|------|--------|----------|
| POST | `/api/favorite/add` | ✅ | `{movieId, movieTitle, moviePoster}` | 返回收藏记录 |
| POST | `/api/favorite/remove` | ✅ | `{movieId}` | 取消收藏成功 |
| GET | `/api/favorite/list` | ✅ | — | 返回收藏列表（按时间倒序） |

### 标记模块

| 方法 | 路径 | 认证 | 请求体 | 响应说明 |
|------|------|------|--------|----------|
| POST | `/api/watch/add` | ✅ | `{movieId, movieTitle, moviePoster, status}` | status: `想看` 或 `看过` |
| PUT | `/api/watch/update` | ✅ | `{movieId, movieTitle, moviePoster, status}` | 更新标记状态 |
| GET | `/api/watch/list` | ✅ | — | 返回标记列表（按时间倒序） |

### 系统接口

| 方法 | 路径 | 响应 |
|------|------|------|
| GET | `/` | `{app, version, status: "running"}` |
| ANY | `/任意不存在路径` | `{code: 404, message: "接口不存在: /xxx"}` |

---

## 🔑 关键设计说明

### 身份认证流程

```
请求 → LoginInterceptor.preHandle()
       ├── OPTIONS 请求 → 直接放行（CORS预检）
       ├── 非Controller方法（404/静态资源）→ 直接放行
       ├── Authorization header 不存在 → 401
       ├── Token 无效/过期 → 401
       └── Token 有效 → currentUserId存入CurrentUserHolder → 请求处理 → afterCompletion清理
```

### 密码安全

- 使用 **BCrypt** 加密存储（`spring-security-crypto`）
- User 实体的 password 字段标注 `@JsonIgnore`，永不暴露在 JSON 响应中

### 跨域配置

- `CorsConfig.java` — 基于 `CorsFilter`
- 允许所有来源（`addAllowedOriginPattern("*")`）
- 允许所有请求方法和请求头
- 允许携带凭证（Cookies / Authorization Header）

### JWT 配置

```yaml
jwt:
  secret: MovieRecommendationSystemSecretKey2024ForJwtTokenGeneration
  expiration: 604800000  # 7天
```

- Token 中携带 `userId` 和 `username`
- 签名算法: **HS512**

---

## 🚀 启动步骤

### 1. 初始化数据库

执行 `src/main/resources/sql/init.sql`：

```bash
mysql -u root -p < src/main/resources/sql/init.sql
```

### 2. 修改数据库密码

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    username: root
    password: 你的MySQL密码
```

### 3. 启动应用

```bash
# 方式一：Maven（推荐）
mvn spring-boot:run -Dhttps.protocols=TLSv1.2

# 方式二：IDEA
# 直接运行 MovieBackendApplication.main()
```

### 4. 验证

```bash
curl http://localhost:8080/
# 返回: {"code":200,"data":{"app":"Movie Recommendation Backend","status":"running","version":"1.0.0"}}
```

---

## 🧪 测试结果

所有 10 个 API 接口均已通过测试：

| # | 接口 | 测试结果 |
|---|------|----------|
| 1 | `POST /api/user/register` | ✅ 注册成功，createTime 正常 |
| 2 | `POST /api/user/login` | ✅ 返回 token + 用户信息 |
| 3 | `GET /api/user/info` | ✅ 正确返回当前用户 |
| 4 | `PUT /api/user/info` | ✅ nickname 更新成功 |
| 5 | `POST /api/favorite/add` | ✅ 收藏成功 |
| 6 | `POST /api/favorite/remove` | ✅ 取消收藏成功 |
| 7 | `GET /api/favorite/list` | ✅ 按时间倒序排列 |
| 8 | `POST /api/watch/add` | ✅ 中文"想看"/"看过"正常 |
| 9 | `PUT /api/watch/update` | ✅ 状态更新成功 |
| 10 | `GET /api/watch/list` | ✅ 按时间倒序排列 |

错误处理测试：

| 场景 | HTTP Code | 消息 |
|------|-----------|------|
| 重复注册 | 200 | `用户名已存在` |
| 密码错误 | 200 | `用户名或密码错误` |
| 无 Token | 401 | `未提供认证令牌` |
| 无效 Token | 401 | `认证令牌无效或已过期` |
| 404路径 | 404 | `接口不存在: /path` |
| 重复收藏 | 200 | `该电影已收藏` |

---

## 🔧 启动过程中修复的问题

| 序号 | 问题 | 解决方案 |
|------|------|----------|
| 1 | 系统未安装 Maven | PowerShell 下载 Maven 3.9.6 到临时目录 |
| 2 | Java 8 SSL 证书错误 | 添加 `-Dhttps.protocols=TLSv1.2` JVM 参数 |
| 3 | Maven 下载速度慢 | 配置阿里云镜像 `~/.m2/settings.xml` |
| 4 | `@NotBlank`/`@Valid` 编译失败 | pom.xml 添加 `spring-boot-starter-validation` 依赖 |
| 5 | insert 返回 `createTime: null` | 添加 `MyMetaObjectHandler` 自动填充 `LocalDateTime.now()` |
| 6 | 根路径 `/` 返回白页 404 | 添加 `IndexController` 返回 JSON 欢迎页 |
| 7 | 不存在的 `/api/*` 被拦截器拦成 401 | `LoginInterceptor` 增加 `handler instanceof HandlerMethod` 判断，非 Controller 方法直接放行 |

---

## 📦 依赖清单

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| MyBatis Plus | 3.5.5 | ORM |
| MySQL Connector | 8.x (runtime) | 数据库驱动 |
| spring-security-crypto | (随Spring Boot) | BCrypt密码加密 |
| jjwt | 0.9.1 | JWT令牌 |
| jaxb-api | 2.3.1 | jjwt的Java 9+兼容 |
| Lombok | (optional) | 简化代码 |
| spring-boot-starter-validation | (随Spring Boot) | Bean校验 |

---

## ⚙️ application.yml 配置

```yaml
server:
  port: 8080
  error:
    whitelabel:
      enabled: false          # 禁用默认白页错误页

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/movie_recommendation?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto

jwt:
  secret: MovieRecommendationSystemSecretKey2024ForJwtTokenGeneration
  expiration: 604800000    # 7天
```
