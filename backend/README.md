# 企业废旧资产内部拍卖与处置平台 - 后端项目

## 项目简介

这是一个基于 Spring Boot 3 + MySQL 的企业废旧资产内部拍卖与处置平台后端项目。

## 技术栈

- **Spring Boot 3.2.0** - 应用框架
- **Spring Data JPA** - 数据持久化
- **Spring Security** - 安全认证
- **JWT** - Token 认证
- **MySQL 8.0** - 数据库
- **Lombok** - 简化开发
- **Maven** - 项目构建

## 功能模块

### 1. 用户与权限管理
- 用户登录/注册
- 角色权限控制（系统管理员、资产专员、财务专员、普通员工）
- 用户管理（启用/禁用、重置密码）
- 部门管理

### 2. 资产管理
- 资产CRUD操作
- 折旧规则管理
- 资产定价计算（自动计算当前价值和起拍价）
- 资产审核（财务审核定价）
- 重新定价

### 3. 拍卖管理
- 创建拍卖活动
- 拍卖列表查询
- 员工竞价（自动延时、保留价支持）
- 拍卖结果记录
- 自动结束拍卖
- 确认成交

### 4. 资产处置管理
- 成交确认
- 交易单审核（财务审核）
- 资产处置确认（资产专员确认）
- 上传凭证
- 流拍处理

### 5. 统计查询
- 资产处置统计
- 员工竞拍记录
- 资产历史记录

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- IDE: IntelliJ IDEA / Eclipse

### 2. 数据库配置

在 `application.yml` 中配置数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/waidp-springboot-master?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 3. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS `waidp-springboot-master` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 启动项目

```bash
# 使用 Maven 编译
mvn clean install

# 运行主类
mvn spring-boot:run
```

或在 IDE 中直接运行 `WaidpBackendApplication.java`

### 5. 访问应用

启动成功后，访问：

**http://localhost:8081**

## API 接口文档

### 认证相关

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/info` - 获取当前用户信息

### 部门相关

- `GET /api/department/list` - 获取所有部门
- `GET /api/department/{id}` - 根据ID获取部门
- `POST /api/department/create` - 创建部门
- `PUT /api/department/update` - 更新部门
- `DELETE /api/department/{id}` - 删除部门

### 资产相关

- `GET /api/asset/list` - 分页查询资产
- `GET /api/asset/{id}` - 根据ID获取资产
- `POST /api/asset/create` - 创建资产
- `PUT /api/asset/update` - 更新资产
- `DELETE /api/asset/{id}` - 删除资产
- `POST /api/asset/{id}/recalculate` - 重新计算资产价值
- `POST /api/asset/approve` - 审核资产定价

### 拍卖相关

- `GET /api/auction/list` - 分页查询拍卖
- `GET /api/auction/{id}` - 根据ID获取拍卖
- `POST /api/auction/create` - 创建拍卖
- `PUT /api/auction/update` - 更新拍卖
- `DELETE /api/auction/{id}` - 删除拍卖
- `POST /api/auction/bid` - 参与竞价
- `POST /api/auction/{id}/confirm` - 确认成交

## 项目结构

```
backend/
├── src/main/java/com/waidp/
│   ├── WaidpBackendApplication.java    # 主启动类
│   ├── common/                        # 公共类
│   │   ├── Result.java               # 统一响应结果
│   │   └── PageResult.java           # 分页结果
│   ├── config/                        # 配置类
│   │   ├── SecurityConfig.java         # Spring Security 配置
│   │   └── CorsConfig.java           # 跨域配置
│   ├── controller/                     # 控制层
│   │   ├── AuthController.java         # 认证控制器
│   │   ├── DepartmentController.java    # 部门控制器
│   │   ├── AssetController.java        # 资产控制器
│   │   └── AuctionController.java       # 拍卖控制器
│   ├── entity/                        # 实体类
│   │   ├── User.java                 # 用户实体
│   │   ├── Department.java            # 部门实体
│   │   ├── DepreciationRule.java     # 折旧规则实体
│   │   ├── Asset.java                # 资产实体
│   │   ├── Auction.java              # 拍卖实体
│   │   ├── Bid.java                 # 竞价记录实体
│   │   ├── Transaction.java           # 交易单实体
│   │   └── AssetHistory.java         # 资产历史实体
│   ├── repository/                    # 数据访问层
│   │   ├── UserRepository.java         # 用户 Repository
│   │   ├── DepartmentRepository.java  # 部门 Repository
│   │   ├── DepreciationRuleRepository.java
│   │   ├── AssetRepository.java       # 资产 Repository
│   │   ├── AuctionRepository.java      # 拍卖 Repository
│   │   ├── BidRepository.java         # 竞价 Repository
│   │   ├── TransactionRepository.java  # 交易单 Repository
│   │   └── AssetHistoryRepository.java
│   ├── service/                       # 业务逻辑层
│   │   ├── AuthService.java           # 认证服务接口
│   │   ├── DepartmentService.java      # 部门服务接口
│   │   ├── AssetService.java          # 资产服务接口
│   │   ├── AuctionService.java        # 拍卖服务接口
│   │   └── impl/                    # 服务实现
│   │       ├── AuthServiceImpl.java
│   │       ├── DepartmentServiceImpl.java
│   │       ├── AssetServiceImpl.java
│   │       └── AuctionServiceImpl.java
│   └── util/                          # 工具类
│       └── JwtUtil.java             # JWT 工具类
└── src/main/resources/
    └── application.yml                    # 应用配置文件
```

## 数据库表结构

系统启动时会自动创建以下表：

1. `department` - 部门表
2. `user` - 用户表
3. `depreciation_rule` - 折旧规则表
4. `asset` - 资产表
5. `auction` - 拍卖表
6. `bid` - 竞价记录表
7. `transaction` - 交易单表
8. `asset_history` - 资产历史表

## 核心业务逻辑

### 资产价值计算

根据折旧规则自动计算资产当前价值：

```
若实际使用年限 ≤ 预计使用年限：
  当前价值 = 原值 × [1 - (1 - 残值率) × (实际使用年限 ÷ 预计使用年限)]

若实际使用年限 > 预计使用年限：
  当前价值 = 原值 × 残值率

起拍价 = 当前价值 × 0.8
```

### 拍卖延时机制

在拍卖结束前 5 分钟内有新的出价，自动延长 5 分钟。

### 保留价机制

可设置保留价，只有达到或超过保留价的出价才能中标。

### 违约惩罚

中标者 24 小时内未确认成交，或确认后 48 小时内未付款，将被扣除 3 个月竞拍资格。

## 注意事项

1. 确保 MySQL 服务已启动
2. 确保数据库 `waidp-springboot-master` 已创建
3. 首次启动会自动创建所有表（JPA DDL Auto）
4. 默认端口为 8081，可在 `application.yml` 中修改
5. JWT 密钥请在生产环境中修改为更安全的值

## 联系方式

如有问题，请联系开发团队。
