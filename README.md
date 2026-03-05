# 企业内部资产拍卖处置平台

一个面向企业内部的固定资产全流程处置系统，覆盖资产建档、估值审核、内部竞拍、成交确认、付款审核、处置归档与统计分析。

## 项目简介
该项目用于解决企业闲置/报废资产在内部处置时“流程分散、记录不完整、责任边界不清晰”的问题。

通过角色分工（系统管理员、资产专员、财务专员、普通员工）和状态流转机制，实现：
- 资产从登记到处置归档的闭环管理
- 竞拍过程透明可追溯
- 交易与处置凭证可留痕
- 统计看板支撑业务分析

## 核心功能
- 用户与权限管理：登录注册、角色权限控制、用户状态管理
- 部门管理：部门信息维护
- 资产管理：资产建档、编辑、明细、图片、折旧规则
- 财务审核：资产定价审核、成交付款审核、审核历史
- 拍卖管理：创建拍卖、竞价/一键出价、竞价记录、中标确认
- 处置管理：待处置交易、处置确认、处置档案
- 统计分析：看板、竞拍统计、处置统计、归档查询

## 技术栈
- 后端：Spring Boot 3.2.0、Spring Security、Spring Data JPA、MySQL、JWT
- 前端：Vue 3、Vite 5、Element Plus、Pinia、Vue Router、Axios、ECharts
- 运行环境：JDK 17、Maven 3.8+、Node.js 18+、MySQL 8.0+

## 系统架构
- `frontend`：管理端前端（默认 `http://localhost:5173`）
- `backend`：REST API 服务（默认 `http://localhost:8084`）
- `waidp_springboot_master.sql`：数据库结构与示例数据
- `uploads`：运行期上传文件目录（资产图、付款/处置凭证）

## 快速开始
### 1. 初始化数据库
1. 创建数据库：`waidp_springboot_master`
2. 导入根目录 SQL 文件：`waidp_springboot_master.sql`

### 2. 修改后端配置
编辑 `backend/src/main/resources/application.yml`：
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `upload.path`（建议改成你本机可写路径）

注意：当前配置中包含本地开发密码与 Windows 绝对路径，上传 GitHub 前建议脱敏。

### 3. 启动项目
方式 A（推荐，Windows 一键启动）：
```bat
start_project_with_check.bat
```

方式 B（手动启动）：
```bash
# 后端
cd backend
mvn spring-boot:run

# 前端（新终端）
cd frontend
npm install
npm run dev
```

### 4. 默认账号
系统启动时会自动创建/重置管理员账号：
- 用户名：`admin`
- 密码：`admin123`

## 目录结构（简化）
```text
enterprise_internal_asset_auction_disposal_platform/
├─ backend/                # Spring Boot 后端
├─ frontend/               # Vue 前端
├─ docs/                   # 项目文档
├─ uploads/                # 上传文件
├─ waidp_springboot_master.sql
└─ start_project_with_check.bat
```

## 业务流程
1. 资产专员录入资产（待审核）
2. 财务专员审核并确定起拍价（待拍卖）
3. 资产专员创建拍卖并开放竞价
4. 竞拍结束后系统自动生成交易，员工确认成交
5. 财务审核付款，资产专员确认处置并归档

## 文档
- 详细使用手册：[`docs/USER_GUIDE.md`](docs/USER_GUIDE.md)

## 上传到 GitHub 前建议
- 检查并清理敏感信息（数据库密码、密钥、手机号等）
- 不提交运行时目录（如 `uploads/`、临时日志）
- 不提交依赖目录（如 `node_modules/`）
- 提交前执行：`git status`，确认只包含预期文件

## 许可证
当前仓库未声明许可证。如需开源，建议补充 `LICENSE` 文件（例如 MIT）。
