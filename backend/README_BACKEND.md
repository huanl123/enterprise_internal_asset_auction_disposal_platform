# 企业废旧资产内部拍卖与处置平台 - 后端文档

## 项目简介

本系统是一个企业内部资产拍卖与处置管理平台，采用前后端分离架构，后端使用Spring Boot框架实现，实现资产的完整生命周期管理，从创建、定价评估、拍卖、交易到最终处置。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security + JWT
- MySQL 8.0
- Maven

## 系统功能模块

### 1. 用户与权限管理模块

**角色划分：**
- 系统管理员：管理用户、部门，创建资产专员和财务专员账号
- 资产专员：管理资产、折旧规则，创建拍卖活动
- 财务专员：审核资产定价，审核交易单，查看财务统计
- 普通员工：查看和参与竞拍，查看个人竞拍记录

**主要功能：**
- 用户注册与登录（JWT认证）
- 账号启用/禁用
- 部门管理
- 密码管理

### 2. 资产管理模块

**主要功能：**
- 资产折旧规则管理（创建、编辑、启用/停用）
- 资产信息管理（创建、编辑、删除）
- 资产自动定价评估（基于折旧规则计算当前价值和起拍价）
- 财务审核定价（调整起拍价、设置保留价）
- 资产状态流转：待审核 → 待拍卖 → 拍卖中 → 已成交/流拍 → 待处置 → 已处置

**资产价值计算公式：**
```
若实际使用年限 ≤ 预计使用年限：
  当前价值 = 原值 × [1 - (1 - 残值率) × (实际使用年限 ÷ 预计使用年限)]

若实际使用年限 > 预计使用年限：
  当前价值 = 原值 × 残值率

起拍价 = 当前价值 × 0.8
```

### 3. 拍卖管理模块

**主要功能：**
- 拍卖活动发布（设置开始/结束时间、加价幅度、部门范围）
- 员工竞价（一键出价/自定义出价）
- 实时显示当前最高价及竞价记录
- 防秒杀机制：结束前5分钟内出价，自动延长5分钟
- 拍卖结果自动判定（定时任务每分钟检查）
- 异常处理：中标者放弃成交，扣除3个月竞拍资格

**拍卖状态：**
- not_started：未开始
- in_progress：进行中
- 已成交：达到保留价且有人中标
- 流拍：未达到保留价或无人出价
- 已取消：交易取消

### 4. 资产处置管理模块

**主要功能：**
- 成交确认（中标者确认成交，生成资产交易单）
- 交易单审核（财务专员确认收款，上传付款凭证）
- 资产处置（资产专员与中标者交割，上传处置凭证）
- 资产归档（形成完整的生命周期档案）
- 违约惩罚机制

**交易单状态：**
- confirmStatus: pending → confirmed
- paymentStatus: pending → approved / rejected
- disposalStatus: pending → completed

### 5. 统计查询模块

**主要功能：**
- 资产处置档案查询（完整的流转信息、凭证）
- 员工竞拍记录（参与场次、中标次数、违约次数、成交明细）
- 资产处置统计（按部门、时间筛选）
- 仪表盘数据展示

## API接口说明

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/info` - 获取当前用户信息

### 用户管理
- `GET /api/users` - 获取用户列表
- `POST /api/users` - 创建用户（管理员）
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户
- `PUT /api/users/{id}/status` - 启用/禁用用户
- `PUT /api/users/{id}/password` - 重置密码

### 部门管理
- `GET /api/department/list` - 获取所有部门
- `POST /api/department/create` - 创建部门
- `PUT /api/department/update` - 更新部门
- `DELETE /api/department/{id}` - 删除部门

### 折旧规则管理
- `GET /api/depreciation-rules` - 获取折旧规则列表
- `GET /api/depreciation-rules/active` - 获取启用的折旧规则
- `POST /api/depreciation-rules` - 创建折旧规则
- `PUT /api/depreciation-rules/{id}` - 更新折旧规则
- `PUT /api/depreciation-rules/{id}/status` - 启用/停用
- `DELETE /api/depreciation-rules/{id}` - 删除折旧规则

### 资产管理
- `GET /api/assets` - 获取资产列表
- `GET /api/assets/{id}` - 获取资产详情
- `GET /api/assets/{id}/history` - 获取资产历史
- `POST /api/assets` - 创建资产
- `PUT /api/assets/{id}` - 更新资产
- `DELETE /api/assets/{id}` - 删除资产
- `POST /api/assets/{id}/recalculate` - 重新计算价值

### 拍卖管理
- `GET /api/auction` - 获取拍卖列表
- `GET /api/auction/{id}` - 获取拍卖详情
- `GET /api/auction/{id}/bids` - 获取竞价记录
- `POST /api/auction` - 创建拍卖
- `DELETE /api/auction/{id}` - 删除拍卖
- `POST /api/auction/{id}/bid` - 参与竞价
- `POST /api/auction/{id}/quick-bid` - 一键出价
- `POST /api/auction/{id}/confirm` - 确认成交

### 财务管理
- `GET /api/finance/assets/pending` - 获取待审核资产
- `POST /api/finance/assets/{id}/approve` - 审核资产定价
- `GET /api/finance/transactions` - 获取交易单列表
- `GET /api/finance/transactions/{id}` - 获取交易单详情
- `POST /api/finance/transactions/{id}/payment` - 审核收款
- `GET /api/finance/statistics` - 获取财务统计

### 资产处置
- `GET /api/disposal/assets` - 获取待处置资产
- `POST /api/disposal/assets/{id}/confirm` - 确认处置完成
- `GET /api/disposal/assets/disposed` - 获取已处置资产
- `GET /api/disposal/assets/{id}` - 获取已处置资产详情
- `GET /api/disposal/transactions` - 获取待处置交易单

### 统计查询
- `GET /api/statistics/dashboard` - 获取仪表盘统计
- `GET /api/statistics/bids` - 获取员工竞拍记录
- `GET /api/statistics/bids/summary` - 获取所有员工竞拍汇总
- `GET /api/statistics/disposal` - 获取资产处置统计
- `GET /api/statistics/disposal/department` - 获取部门处置统计

### 文件上传
- `POST /api/files/upload` - 上传单个文件
- `POST /api/files/upload/images` - 批量上传图片

## 数据库设计

### 主要数据表

**用户表 (user)**
- id, username, password, name, phone, department_id, role, status, avatar, create_time, update_time

**部门表 (department)**
- id, name, manager_name, description, create_time, update_time

**折旧规则表 (depreciation_rule)**
- id, name, useful_life, salvage_rate, description, status, create_time, update_time

**资产表 (asset)**
- id, code, name, specification, images, department_id, purchase_date, original_value,
  current_value, start_price, depreciation_rule_id, reserve_price, has_reserve_price, status,
  create_time, update_time

**拍卖表 (auction)**
- id, name, asset_id, start_price, current_price, increment_amount, has_reserve_price,
  reserve_price, start_time, end_time, status, bid_count, description, department_ids,
  winner_id, final_price, create_time, update_time

**竞价记录表 (bid)**
- id, auction_id, bidder_id, price, bid_time, create_time

**交易单表 (transaction)**
- id, code, auction_id, asset_id, winner_id, final_price, confirm_status, confirm_time,
  payment_status, payment_time, payment_voucher, payment_remark,
  disposal_status, disposal_time, disposal_voucher, disposal_remark, payment_deadline,
  create_time, update_time

**资产历史表 (asset_history)**
- id, asset_id, operator_id, operation, content, create_time

## 配置说明

### application.yml配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/waidp-springboot-master
    username: root
    password: your_password

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

server:
  port: 8081

file:
  upload-dir: ./uploads
  access-url: http://localhost

jwt:
  secret: YOUR_SECRET_KEY
  expiration: 86400000  # 24小时
```

## 初始化数据

系统启动时会自动初始化以下数据：

1. **系统管理员账号**
   - 用户名：admin
   - 密码：admin123

2. **示例部门**
   - 技术部、财务部、人力资源部、市场部、行政部

3. **示例折旧规则**
   - 电子设备折旧规则（5年，5%残值率）
   - 办公设备折旧规则（8年，5%残值率）
   - 车辆折旧规则（10年，5%残值率）

## 运行说明

### 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 启动步骤

1. 创建数据库
```sql
CREATE DATABASE waidp-springboot-master CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改配置文件
修改 `application.yml` 中的数据库连接信息

3. 创建uploads目录
```bash
mkdir uploads
```

4. 编译运行
```bash
mvn clean install
mvn spring-boot:run
```

或直接运行主类：
```bash
java -jar target/waidp-backend-1.0.0.jar
```

### 访问地址
- 后端API：http://localhost:8081
- Swagger文档（如配置）：http://localhost:8081/swagger-ui.html

## 定时任务

系统配置了以下定时任务：

1. **拍卖状态检查** - 每分钟执行一次
   - 检查已结束的拍卖
   - 自动更新拍卖结果（成交/流拍）
   - 更新资产状态

2. **过期交易检查** - 每天凌晨2点执行
   - 检查超时未确认的交易
   - 处理违约情况

## 安全说明

### JWT认证
- 使用JWT进行用户认证
- Token有效期：24小时
- 在请求头中携带：`Authorization: Bearer {token}`

### 权限控制
- 使用Spring Security进行权限控制
- 支持基于角色的访问控制（RBAC）
- 使用`@PreAuthorize`注解进行方法级权限控制

## 注意事项

1. **文件上传**
   - 文件保存在 `./uploads` 目录下
   - 支持的文件类型：jpg, png, pdf等
   - 单文件最大10MB，总请求最大50MB

2. **资产状态流转**
   - 资产状态只能按既定流程流转
   - 已处置的资产不可编辑或删除
   - 流拍资产可以重新进入拍卖流程

3. **保留价机制**
   - 保留价对竞拍者不可见
   - 只有达到保留价的出价才能成交
   - 财务专员可以调整保留价

4. **防秒杀机制**
   - 拍卖结束前5分钟内出价，自动延长5分钟
   - 防止最后时刻的恶意竞争

## 开发建议

1. 使用Postman或Swagger进行API测试
2. 查看日志了解系统运行状态
3. 使用MySQL Workbench查看和管理数据库
4. 前端开发可参考API文档对接接口

## 联系方式

如有问题，请联系开发团队。
