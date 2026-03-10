# 企业废旧资产内部拍卖与处置平台 - 前端项目

## 项目简介

这是一个基于 Vue.js + Element Plus 的企业废旧资产内部拍卖与处置平台前端项目，实现企业内部资产的拍卖、竞价、处置等全流程管理。

## 技术栈

- **前端框架**: Vue 3
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router 4
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **日期处理**: dayjs

## 功能模块

### 1. 用户与权限管理
- 用户登录/注册
- 角色权限控制（普通员工、资产专员、财务专员、系统管理员）
- 部门管理

### 2. 资产管理
- 资产列表查询
- 资产新增/编辑
- 资产详情查看
- 折旧规则管理
- 资产定价计算

### 3. 拍卖管理
- 拍卖活动创建
- 拍卖列表查看
- 员工竞价（一键出价/自定义出价）
- 拍卖详情（实时更新）
- 我的竞拍记录

### 4. 资产处置管理
- 成交确认
- 交易单审核（财务）
- 资产处置确认（资产专员）

### 5. 统计查询
- 资产处置统计
- 员工竞拍记录
- 综合统计看板

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

项目将在 `http://localhost:3000` 启动

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口
│   │   ├── index.js      # 接口定义
│   │   └── request.js    # Axios封装
│   ├── assets/           # 资源文件
│   ├── components/       # 公共组件
│   ├── layout/           # 布局组件
│   │   └── MainLayout.vue
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # 状态管理
│   │   └── user.js
│   ├── views/            # 页面组件
│   │   ├── Login.vue
│   │   ├── Dashboard.vue
│   │   ├── Profile.vue
│   │   ├── asset/        # 资产管理
│   │   ├── auction/      # 拍卖管理
│   │   ├── department/   # 部门管理
│   │   ├── disposal/     # 资产处置
│   │   ├── finance/      # 财务审核
│   │   ├── statistics/   # 统计查询
│   │   └── user/         # 用户管理
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

## 环境配置

### 开发环境

前端运行在 `http://localhost:3000`，通过 Vite 的代理配置转发 API 请求到后端服务。

```javascript
// vite.config.js
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端服务地址
      changeOrigin: true
    }
  }
}
```

### 生产环境

生产环境下，需要配置后端 API 的真实地址。

## 角色权限说明

- **系统管理员 (admin)**: 全部权限，包括用户管理、部门管理、资产审核等
- **资产专员 (asset_specialist)**: 资产管理、拍卖活动创建、资产处置
- **财务专员 (finance_specialist)**: 资产审核、财务审核、付款确认
- **普通员工 (employee)**: 查看拍卖信息、参与竞拍、确认成交

## API接口说明

所有 API 请求都通过 `/api` 前缀发送，具体接口定义见 `src/api/index.js`。

主要接口：
- `/auth/login` - 用户登录
- `/auth/register` - 用户注册
- `/asset/*` - 资产相关
- `/auction/*` - 拍卖相关
- `/department/*` - 部门相关
- `/finance/*` - 财务相关
- `/disposal/*` - 资产处置相关
- `/statistics/*` - 统计相关

## 注意事项

1. 前端项目需要后端 API 支持，确保后端服务正常运行
2. 所有图片上传功能需要后端提供上传接口
3. 统计图表功能需要集成 ECharts（当前为占位符）
4. 部分功能（如导出）需要根据实际需求完善

## 开发规范

- 使用 Vue 3 Composition API
- 使用 `<script setup>` 语法糖
- 组件命名采用 PascalCase
- 文件命名采用 PascalCase（组件）或 camelCase（工具类）
- 遵循 ESLint 代码规范

## 浏览器支持

- Chrome (推荐)
- Firefox
- Edge
- Safari

## 联系方式

如有问题，请联系开发团队。
