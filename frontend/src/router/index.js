import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      // 用户与权限管理
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/user/Index.vue'),
        meta: { title: '用户管理', roles: ['系统管理员'] }
      },
      {
        path: 'department',
        name: 'DepartmentManagement',
        component: () => import('@/views/department/Index.vue'),
        meta: { title: '部门管理', roles: ['系统管理员'] }
      },
      // 资产管理
      {
        path: 'asset',
        name: 'AssetManagement',
        component: () => import('@/views/asset/Index.vue'),
        meta: { title: '资产管理', roles: ['资产专员', '系统管理员'] }
      },
      {
        path: 'asset/create',
        name: 'AssetCreate',
        component: () => import('@/views/asset/Create.vue'),
        meta: { title: '新增资产', roles: ['资产专员', '系统管理员'] }
      },
      {
        path: 'asset/:id',
        name: 'AssetDetail',
        component: () => import('@/views/asset/Detail.vue'),
        meta: { title: '资产详情' }
      },
      {
        path: 'asset/:id/edit',
        name: 'AssetEdit',
        component: () => import('@/views/asset/Edit.vue'),
        meta: { title: '编辑资产', roles: ['资产专员', '系统管理员'] }
      },
      {
        path: 'depreciation',
        name: 'DepreciationRules',
        component: () => import('@/views/asset/DepreciationRules.vue'),
        meta: { title: '折旧规则管理', roles: ['资产专员', '系统管理员'] }
      },
      // 拍卖管理
      {
        path: 'auction',
        name: 'AuctionManagement',
        component: () => import('@/views/auction/Index.vue'),
        meta: { title: '拍卖管理' }
      },
      {
        path: 'auction/create',
        name: 'AuctionCreate',
        component: () => import('@/views/auction/Create.vue'),
        meta: { title: '创建拍卖活动', roles: ['资产专员', '系统管理员'] }
      },
      {
        path: 'auction/:id',
        name: 'AuctionDetail',
        component: () => import('@/views/auction/Detail.vue'),
        meta: { title: '拍卖详情' }
      },
      {
        path: 'auction/my',
        name: 'MyAuctions',
        component: () => import('@/views/auction/MyAuctions.vue'),
        meta: { title: '我的竞拍', roles: ['普通员工'] }
      },
      // 财务审核
      {
        path: 'finance',
        name: 'FinanceReview',
        component: () => import('@/views/finance/Index.vue'),
        meta: { title: '财务审核', roles: ['财务专员', '系统管理员'] }
      },
      // 资产处置
      {
        path: 'disposal',
        name: 'AssetDisposal',
        component: () => import('@/views/disposal/Index.vue'),
        meta: { title: '资产处置', roles: ['资产专员', '系统管理员'] }
      },
      {
        path: 'disposal/:id',
        name: 'DisposalDetail',
        component: () => import('@/views/disposal/Detail.vue'),
        meta: { title: '处置详情' }
      },
      // 统计查询
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Index.vue'),
        meta: { title: '统计总览', roles: ['资产专员', '财务专员', '系统管理员'] }
      },
      {
        path: 'statistics/archive',
        name: 'DisposalArchiveStatistics',
        component: () => import('@/views/statistics/Archive.vue'),
        meta: { title: '资产处置档案查询', roles: ['资产专员', '财务专员', '系统管理员', '普通员工'] }
      },
      {
        path: 'statistics/asset',
        name: 'AssetStatistics',
        component: () => import('@/views/statistics/Asset.vue'),
        meta: { title: '资产处置统计', roles: ['资产专员', '财务专员', '系统管理员'] }
      },
      {
        path: 'statistics/employee',
        name: 'EmployeeStatistics',
        component: () => import('@/views/statistics/Employee.vue'),
        meta: { title: '员工竞拍记录', roles: ['资产专员', '财务专员', '系统管理员'] }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'transaction/my',
        name: 'MyTransactions',
        component: () => import('@/views/transaction/MyTransactions.vue'),
        meta: { title: '我的交易单', roles: ['普通员工'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  let userStore
  try {
    userStore = useUserStore()
  } catch (error) {
    console.error('获取用户存储失败:', error)
    next('/login')
    return
  }
  const token = userStore?.token || ''

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    // 检查角色权限
    if (to.meta.roles) {
      const userRole = userStore.user?.role

      // 角色映射：将中文角色名映射到对应的英文角色名
      const chineseToEnglishMap = {
        '系统管理员': ['SYSTEM_ADMIN', 'ADMIN', 'admin'],
        '资产专员': ['ASSET_SPECIALIST', 'asset_specialist'],
        '财务专员': ['FINANCE_SPECIALIST', 'finance_specialist'],
        '普通员工': ['EMPLOYEE', 'NORMAL_USER', 'employee']
      }
      
      // 获取用户的英文角色列表
      const englishUserRoles = chineseToEnglishMap[userRole] || [userRole]
      
      // 检查用户角色是否在允许的角色列表中
      const hasPermission = to.meta.roles.some(requiredRole => {
        const requiredEnglishRoles = chineseToEnglishMap[requiredRole] || [requiredRole]
        return englishUserRoles.some(userEngRole => 
          requiredEnglishRoles.includes(userEngRole) || userEngRole.toUpperCase() === requiredRole.toUpperCase()
        )
      })

      if (!hasPermission) {
        console.log('路由权限检查失败 - 用户角色:', userRole, '需要角色:', to.meta.roles)
        // 如果没有权限，重定向到首页
        next('/')
        return
      }
    }
    next()
  }
})

export default router
