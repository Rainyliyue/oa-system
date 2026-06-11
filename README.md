# OA 办公系统

这是一个课程设计取向的 OA 办公系统微服务源码，版本固定为：

- Java 17
- Spring Boot 3.0.2
- Spring Cloud 2022.0.0
- Spring Cloud Alibaba 2022.0.0.0
- Nacos 2.2.2
- MySQL + Redis + Spring Cloud Gateway + OpenFeign
- Thymeleaf + 内置 LayUI 风格静态资源

## 模块

- `oa-common`：公共实体、DTO、统一返回、JWT、Feign token 透传。
- `oa-gateway`：统一入口，端口 `9000`。
- `oa-web`：页面服务，端口 `8001`。
- `oa-user-service`：登录注册、用户、角色、权限，端口 `9010`。
- `oa-application-service`：请假、出差、报销，端口 `9020`。
- `oa-attendance-service`：考勤，端口 `9030`。
- `oa-payroll-service`：工资，端口 `9040`。

## 初始化数据库

推荐直接使用项目根目录的 Docker Compose 启动基础设施：

```bash
docker compose up -d
```

这会启动：

- `mysql-service`：MySQL 8.0，暴露 `3306`
- `redis-service`：Redis 7.4.5，默认暴露宿主机 `7379`，容器内仍是 `6379`
- `nacos-service`：Nacos 2.2.2 standalone，暴露 `8848/9848/9849`

Compose 文件没有写死宿主机局域网 IP。容器之间通过 Docker 服务名通信；宿主机上运行的 Java 服务通过 `127.0.0.1` 访问暴露端口，所以开关机导致局域网 IP 变化也不用改配置。

MySQL 容器首次创建数据卷时会自动执行：

```sql
source sql/init.sql;
```

如果你已经有旧的 MySQL 数据卷，初始化脚本不会自动重复执行，可以手动导入 `sql/init.sql`。

默认账号由 `oa-user-service` 首次启动时自动写入，并使用 BCrypt 加密：

- 管理员：`admin / 123456`
- 普通用户：`user / 123456`

## 本地配置默认值

- MySQL：`127.0.0.1:3306/oa_system`，账号 `root`，密码 `123456`
- Redis：`127.0.0.1:7379`
- Nacos：`127.0.0.1:8848`

如本机密码不同，修改各模块 `src/main/resources/application.yml`。
也可以通过环境变量覆盖：

```bash
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DATABASE=oa_system
MYSQL_USERNAME=root
MYSQL_PASSWORD=123456
REDIS_HOST=127.0.0.1
REDIS_PORT=7379
NACOS_SERVER_ADDR=127.0.0.1:8848
```

## 启动顺序

1. 启动基础设施：

```bash
docker compose up -d
```

2. 编译：

```bash
mvn clean package
```

3. 依次启动：

```bash
mvn -pl oa-user-service spring-boot:run
mvn -pl oa-application-service spring-boot:run
mvn -pl oa-attendance-service spring-boot:run
mvn -pl oa-payroll-service spring-boot:run
mvn -pl oa-web spring-boot:run
mvn -pl oa-gateway spring-boot:run
```

4. 浏览器访问：

```text
http://localhost:9000/login
```

## 功能

- 普通用户：注册、登录、请假申请、出差申请、报销申请、上班/下班打卡。
- 管理员：用户管理、角色权限管理、考勤管理、工资管理、请假/出差/报销审批。
- 申请状态：`PENDING`、`APPROVED`、`REJECTED`、`FINISHED`。
- Gateway 会拦截未登录请求，并限制普通用户访问 `/admin/**` 和 `/api/admin/**`。

## Nginx

可参考 `deploy/nginx.conf`，将 `80` 端口反向代理到 Gateway 的 `9000` 端口。

如果希望 Nginx 也用 Docker 启动，先启动 Java Gateway，再执行：

```bash
docker compose --profile proxy up -d nginx-service
```
