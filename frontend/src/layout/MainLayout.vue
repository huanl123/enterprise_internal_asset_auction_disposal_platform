<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <el-icon v-if="isCollapse" :size="24"><Histogram /></el-icon>
        <span v-else>资产拍卖平台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

<!-- 用户与权限管理（仅管理员） -->
<template v-if="userStore.hasAnyRole('系统管理员')">
  <el-sub-menu index="user">
    <template #title>
      <el-icon><User /></el-icon>
      <span>用户与权限</span>
    </template>
    <el-menu-item index="/user">
      <el-icon><UserFilled /></el-icon>
      <template #title>用户管理</template>
    </el-menu-item>
    <el-menu-item index="/department">
      <el-icon><OfficeBuilding /></el-icon>
      <template #title>部门管理</template>
    </el-menu-item>
  </el-sub-menu>
</template>

<!-- 资产管理（资产专员和管理员） -->
<el-sub-menu
  v-if="userStore.hasAnyRole('资产专员', '系统管理员')"
  index="asset"
>
  <template #title>
    <el-icon><Goods /></el-icon>
    <span>资产管理</span>
  </template>
  <el-menu-item index="/asset">
    <el-icon><List /></el-icon>
    <template #title>资产列表</template>
  </el-menu-item>
  <el-menu-item index="/asset/create">
    <el-icon><Plus /></el-icon>
    <template #title>新增资产</template>
  </el-menu-item>
  <el-menu-item index="/depreciation">
    <el-icon><Document /></el-icon>
    <template #title>折旧规则</template>
  </el-menu-item>
</el-sub-menu>

<!-- 拍卖管理 -->
<el-sub-menu index="auction-menu">
  <template #title>
    <el-icon><Histogram /></el-icon>
    <span>拍卖管理</span>
  </template>

  <!-- 所有已登录用户都可以查看拍卖活动并参与竞拍 -->
  <el-menu-item index="/auction">
    <el-icon><List /></el-icon>
    <template #title>拍卖活动</template>
  </el-menu-item>

  <!-- 仅普通员工可查看自己的竞拍记录 -->
  <el-menu-item
    v-if="userStore.hasAnyRole('普通员工')"
    index="/auction/my"
  >
    <el-icon><ShoppingCart /></el-icon>
    <template #title>我的竞拍</template>
  </el-menu-item>

  <!-- 仅普通员工可查看自己的交易单 -->
  <el-menu-item
    v-if="userStore.hasAnyRole('普通员工')"
    index="/transaction/my"
  >
    <el-icon><List /></el-icon>
    <template #title>我的交易单</template>
  </el-menu-item>

  <!-- 只有资产专员/管理员可以创建拍卖 -->
  <el-menu-item
    v-if="userStore.hasAnyRole('资产专员', '系统管理员')"
    index="/auction/create"
  >
    <el-icon><Plus /></el-icon>
    <template #title>创建拍卖</template>
  </el-menu-item>
</el-sub-menu>

<!-- 财务审核（财务专员和管理员） -->
<el-menu-item
  v-if="userStore.hasAnyRole('财务专员', '系统管理员')"
  index="/finance"
>
  <el-icon><Wallet /></el-icon>
  <template #title>财务审核</template>
</el-menu-item>

<!-- 资产处置（资产专员和管理员） -->
<el-menu-item
  v-if="userStore.hasAnyRole('资产专员', '系统管理员')"
  index="/disposal"
>
  <el-icon><Box /></el-icon>
  <template #title>资产处置</template>
</el-menu-item>

        <!-- 统计查询（资产/财务/管理员） -->
        <el-sub-menu
          v-if="userStore.hasAnyRole('资产专员', '财务专员', '系统管理员', '普通员工')"
          index="statistics"
        >
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>统计查询</span>
          </template>
          <el-menu-item
            v-if="userStore.hasAnyRole('资产专员', '财务专员', '系统管理员')"
            index="/statistics"
          >
            <el-icon><DataLine /></el-icon>
            <template #title>统计总览</template>
          </el-menu-item>
          <el-menu-item index="/statistics/archive">
            <el-icon><Document /></el-icon>
            <template #title>资产处置档案查询</template>
          </el-menu-item>
          <el-menu-item
            v-if="userStore.hasAnyRole('资产专员', '财务专员', '系统管理员')"
            index="/statistics/asset"
          >
            <el-icon><TrendCharts /></el-icon>
            <template #title>资产处置统计</template>
          </el-menu-item>
          <el-menu-item
            v-if="userStore.hasAnyRole('资产专员', '财务专员', '系统管理员')"
            index="/statistics/employee"
          >
            <el-icon><User /></el-icon>
            <template #title>员工竞拍记录</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon
            :size="24"
            class="collapse-icon"
            @click="isCollapse = !isCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta?.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.user?.avatar">
                {{ userStore.user?.name?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.user?.name }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { 
  Fold, 
  Expand, 
  HomeFilled, 
  User, 
  OfficeBuilding, 
  Goods, 
  List, 
  Plus, 
  Wallet, 
  Box, 
  DataAnalysis, 
  DataLine, 
  TrendCharts, 
  ShoppingCart,
  Histogram // 添加 Histogram 图标导入
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 调试信息
console.log('========== MainLayout 调试信息 ==========')
console.log('1. 用户Store:', userStore)
console.log('2. 当前用户:', userStore.user)
console.log('3. 用户角色:', userStore.user?.role)
console.log('4. 用户角色类型:', typeof userStore.user?.role)
console.log('5. hasAnyRole("系统管理员"):', userStore.hasAnyRole('系统管理员'))
console.log('6. hasRole("系统管理员"):', userStore.hasRole('系统管理员'))
console.log('7. sessionStorage user:', sessionStorage.getItem('user'))
console.log('========================================')

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      ElMessage.success('退出成功')
      router.push('/login')
    } catch (error) {
      // 用户取消
    }
  }
}
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
}

.aside {
  background-color: #304156;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #434a5e;
  cursor: pointer;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: #000;
  font-family: "SimSun", "宋体", serif;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: #263445 !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
  color: #fff;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-icon {
  cursor: pointer;
  transition: color 0.3s;
}

.collapse-icon:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #333;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
