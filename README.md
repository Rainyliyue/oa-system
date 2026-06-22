# OA 办公系统

这是一个课程设计取向的 OA 办公系统微服务源码，版本固定为：

- Java 17
- Spring Boot 3.0.2
- Spring Cloud 2022.0.0
- Spring Cloud Alibaba 2022.0.0.0
- Nacos 2.2.2
- MySQL + Redis + Spring Cloud Gateway + OpenFeign
- Seata + Sentinel
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

可选组件按 profile 启动：

```bash
docker compose --profile sentinel up -d sentinel-dashboard
docker compose --profile tx up -d seata-service
```

- Sentinel Dashboard：`http://localhost:18958`
- Seata Server：`127.0.0.1:8091`，控制台端口 `7091`

Compose 文件没有写死宿主机局域网 IP。容器之间通过 Docker 服务名通信；宿主机上运行的 Java 服务通过 `127.0.0.1` 访问暴露端口，所以开关机导致局域网 IP 变化也不用改配置。

MySQL 容器首次创建数据卷时会自动执行：

```sql
source sql/init.sql;
```

如果你已经有旧的 MySQL 数据卷，初始化脚本不会自动重复执行，可以手动导入 `sql/init.sql`。

如果是在旧库基础上升级图片证据功能，不需要重建数据库，执行：

```sql
source sql/alter_add_evidence_image.sql;
```

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

项目根目录已经提供 `.env` 统一维护本地端口和中间件地址。Docker Compose 会自动读取 `.env`；如果用命令行启动 Java 服务，先在当前窗口加载一次：

CMD：

```bat
call scripts\load-env.cmd
```

PowerShell：

```powershell
.\scripts\load-env.ps1
```

之后在同一个窗口里执行 `mvn -pl ... spring-boot:run`，MySQL、Redis、Nacos、Sentinel、Seata 等配置都会从 `.env` 继承。IDEA 启动时可把 `.env` 中需要的键值放到各服务 Run Configuration 的 Environment variables 中。

申请证据图片默认保存到 `oa-web` 运行目录下的 `uploads/images`，可通过环境变量覆盖：

```bash
OA_UPLOAD_IMAGE_DIR=C:/Users/87152/Documents/OA/uploads/images
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

旧数据库升级时先执行新增字段/表脚本：

```bash
docker cp sql/alter_add_evidence_image.sql oa-mysql:/tmp/alter_add_evidence_image.sql
docker exec oa-mysql mysql -uroot -p123456 -e "SOURCE /tmp/alter_add_evidence_image.sql;"
docker cp sql/alter_add_workflow_notice_log.sql oa-mysql:/tmp/alter_add_workflow_notice_log.sql
docker exec oa-mysql mysql -uroot -p123456 -e "SOURCE /tmp/alter_add_workflow_notice_log.sql;"
docker cp sql/alter_add_seata_undo_log.sql oa-mysql:/tmp/alter_add_seata_undo_log.sql
docker exec oa-mysql mysql -uroot -p123456 -e "SOURCE /tmp/alter_add_seata_undo_log.sql;"
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

- 普通用户：注册、登录、请假申请、出差申请、报销申请、图片证据上传、上班/下班打卡。
- 管理员：用户管理、角色权限管理、考勤管理、工资管理、操作日志、请假/出差/报销审批。
- 工作台：首页统计、未读消息提醒、审批历史明细。
- 申请状态：`PENDING`、`APPROVED`、`REJECTED`、`FINISHED`。
- Gateway 会拦截未登录请求，并限制普通用户访问 `/admin/**` 和 `/api/admin/**`。

## Nginx

可参考 `deploy/nginx.conf`，将 `80` 端口反向代理到 Gateway 的 `9000` 端口。

如果希望 Nginx 也用 Docker 启动，先启动 Java Gateway，再执行：

```bash
docker compose --profile proxy up -d nginx-service
```

## OpenFeign 增强

`oa-web` 已统一配置 OpenFeign：

- 超时：默认连接超时 `3000ms`，读取超时 `5000ms`。
- 日志：Feign 客户端包 `com.oa.web.feign` 开启 `DEBUG`，默认 `basic` 级别。
- 重试：连接或读取异常默认最多尝试 `2` 次。
- 降级：所有 FeignClient 均配置 `fallbackFactory`，依赖 Resilience4j CircuitBreaker。
- 链路追踪：Gateway 会生成或透传 `X-Trace-Id`，Web 通过 Feign 继续透传到业务服务。

可通过环境变量覆盖：

```bash
FEIGN_CONNECT_TIMEOUT=3000
FEIGN_READ_TIMEOUT=5000
FEIGN_LOGGER_LEVEL=basic
```

验证 traceId：

```bash
curl -I http://localhost:9000/login
```

响应头中应包含 `X-Trace-Id`。业务服务日志中也会显示同一个 `traceId`。

## Sentinel

已接入 Sentinel：

- Gateway 路由 `oa-web` 入口限流，触发后返回 `429` 和统一 `code/msg`。
- Web 热点接口限流：登录、图片上传、审批。
- 审批接口配置异常比例降级规则。
- Feign 已配置 fallbackFactory，远程服务不可用时会自动降级并返回友好提示。

启动 Dashboard 后重启 `oa-gateway`、`oa-web`，再访问系统，即可在 `http://localhost:18958` 看到应用资源。默认热点规则在代码中初始化，即使不打开 Dashboard 也会生效。

验收方式：

```bat
call scripts\load-env.cmd
docker compose --profile sentinel up -d sentinel-dashboard
```

重启 `oa-gateway`、`oa-web` 后访问系统，再打开 Sentinel Dashboard。应用列表应能看到 `oa-gateway`、`oa-web`，资源列表中应出现 `auth:login`、`file:upload:image`、`application:approve` 或网关路由 `oa-web`。

快速触发登录热点限流：

```bat
for /l %i in (1,1,10) do curl -s -X POST http://localhost:9000/doLogin -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}"
```

预期能看到部分请求返回“登录请求过于频繁，请稍后再试”。Dashboard 的实时监控中对应资源 QPS 会变化。

## Seata

已预留并实现一个跨服务事务演示：

- 管理员审批“报销申请”并最终通过时，`oa-web` 会开启 Seata 全局事务。
- 事务内先调用 `oa-application-service` 完成审批、通知、日志。
- 再调用 `oa-payroll-service` 将报销金额计入申请人当月工资奖金。
- 如果工资联动失败，Seata 会回滚本次审批链路。

默认 `SEATA_ENABLED=false`，不开 Seata 时原有审批功能照常运行且不会触发工资联动。演示分布式事务时：

```bash
docker compose --profile tx up -d seata-service
docker cp sql/alter_add_seata_undo_log.sql oa-mysql:/tmp/alter_add_seata_undo_log.sql
docker exec oa-mysql mysql -uroot -p123456 -e "SOURCE /tmp/alter_add_seata_undo_log.sql;"
```

然后启动 `oa-web`、`oa-application-service`、`oa-payroll-service` 时增加：

```bash
SEATA_ENABLED=true
SEATA_SERVER_ADDR=127.0.0.1:8091
```

Windows PowerShell 示例：

```powershell
$env:SEATA_ENABLED="true"
$env:SEATA_SERVER_ADDR="127.0.0.1:8091"
mvn -pl oa-web spring-boot:run
```

如果使用项目 `.env`，在启动三个参与事务的服务前执行：

```bat
call scripts\load-env.cmd
```

验收方式：

1. 确认 `undo_log` 表存在：

```bat
docker exec oa-mysql mysql -uroot -p123456 -D oa_system -e "SHOW TABLES LIKE 'undo_log';"
```

2. 启动 `oa-web`、`oa-application-service`、`oa-payroll-service` 时确保 `SEATA_ENABLED=true`。
3. 新增一条报销申请，并由管理员最终审批通过。
4. 查询工资表，确认报销金额计入当月奖金：

```bat
docker exec oa-mysql mysql -uroot -p123456 -D oa_system -e "SELECT user_id,username,salary_month,bonus,total_salary,remark FROM oa_salary ORDER BY update_time DESC LIMIT 5;"
```

5. 回滚验收：再新增一条报销申请，临时停止 `oa-payroll-service`，然后审批通过。预期页面提示工资联动失败，报销记录仍保持 `PENDING`，工资表没有新增对应奖金。
