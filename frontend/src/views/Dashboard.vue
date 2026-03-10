<template>
  <!-- 仪表盘页面 -->
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome">
        <div>
          <div class="welcome-title">欢迎回来，{{ userName }}</div>
        </div>
        <el-tag type="info">{{ roleLabel }}</el-tag>
      </div>
    </el-card>

    <!-- 功能入口区域（根据角色显示不同模块） -->
    <el-row :gutter="20" class="entry-row" :class="{ single: isSingleEntry }">
      <!-- 遍历可见的功能模块 -->
      <el-col
        v-for="section in visibleEntrySections"
        :key="section.key"
        :xs="24"
        :sm="isSingleEntry ? 24 : 12"
        :lg="isSingleEntry ? 24 : 8"
      >
        <!-- 功能卡片 -->
        <el-card class="entry-card" shadow="hover">
          <template #header>
            <!-- 卡片头部：标题 + 标签 -->
            <div class="card-header">
              <div class="entry-title">
                <el-icon class="entry-icon"><component :is="section.icon" /></el-icon>
                <span>{{ section.title }}</span>
              </div>
              <el-tag v-if="section.tag" size="small" effect="plain">{{ section.tag }}</el-tag>
            </div>
          </template>
          <!-- 功能按钮组 -->
          <div class="entry-actions">
            <el-button
              v-for="action in section.items"
              :key="action.label"
              :type="action.type || 'primary'"
              :plain="action.plain"
              size="small"
              @click="goTo(action.path, action.query)"
            >
              {{ action.label }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 概览统计区域 -->
    <el-row :gutter="20" class="overview-row">
      <!-- 左侧卡片：我的竞拍/工作台概览 -->
      <el-col :xs="24" :lg="8">
        <el-card class="overview-card">
          <template #header>
            <div class="card-header">
              <span>{{ isEmployee ? '我的竞拍' : '工作台概览' }}</span>
            </div>
          </template>

          <!-- 员工视图：竞拍统计 -->
          <template v-if="isEmployee">
            <div class="bid-summary">
              <!-- 提示信息 -->
              <div class="bid-hint">查看你参与的竞拍情况与进度，确认成交后请在 48 小时内完成付款。</div>
              <!-- 快捷操作 -->
              <div class="bid-actions">
                <el-button type="primary" size="small" @click="goTo('/auction/my')">我的竞拍</el-button>
                <el-button type="success" plain size="small" @click="goTo('/auction')">去参与竞拍</el-button>
              </div>
              <!-- 竞拍统计数据（可点击跳转） -->
              <div class="bid-stats">
                <div class="bid-item clickable" @click="goTo('/auction/my', { status: 'in_progress' })">
                  <div class="bid-label">进行中竞拍</div>
                  <div class="bid-value">{{ formatNumber(bidStats.inProgress) }}</div>
                </div>
                <div class="bid-item clickable" @click="goTo('/auction/my', { status: 'ended' })">
                  <div class="bid-label">已结束竞拍</div>
                  <div class="bid-value">{{ formatNumber(bidStats.ended) }}</div>
                </div>
                <div class="bid-item clickable" @click="goTo('/auction/my', { status: 'won' })">
                  <div class="bid-label">中标次数</div>
                  <div class="bid-value">{{ formatNumber(bidStats.wins) }}</div>
                </div>
                <div class="bid-item clickable" @click="goTo('/auction/my', { status: 'won', confirmStatus: 'pending' })">
                  <div class="bid-label">待确认成交</div>
                  <div class="bid-value">{{ formatNumber(bidStats.pendingConfirm) }}</div>
                </div>
                <div class="bid-item clickable" @click="goTo('/transaction/my', { confirmStatus: 'confirmed', paymentStatus: 'pending' })">
                  <div class="bid-label">待财务确认收款</div>



                  <div class="bid-value">{{ formatNumber(bidStats.pendingPayment) }}</div>
                </div>
              </div>
              <div class="bid-list" v-if="myBidList.length">
                <div class="bid-list-title">进行中竞拍明细（领先/落后）</div>
                <div class="bid-row" v-for="item in myBidList" :key="item.id">
                  <div class="bid-name">{{ item.assetName || item.name || '-' }}</div>
                  <div class="bid-price">¥{{ formatAmount(item.currentPrice) }}</div>
                  <el-tag size="small" :type="item.isHighest ? 'success' : 'info'">
                    {{ item.isHighest ? '领先' : '落后' }}
                  </el-tag>
                </div>
              </div>
              <el-empty v-else description="暂无进行中竞拍" />
            </div>
          </template>

          <!-- 管理员/专员视图：待办事项 + 关键统计 -->
          <template v-else>
            <!-- 待办事项列表 -->
            <div v-if="todoList.length" class="todo-list">
              <div v-for="item in todoList" :key="item.label" class="todo-item">
                <div class="todo-main">
                  <div class="todo-title">{{ item.label }}</div>
                  <el-tag size="small" :type="item.tagType || 'warning'">
                    {{ formatTodoCount(item.count) }}
                  </el-tag>
                </div>
                <el-button text type="primary" @click="goTo(item.path, item.query)">去处理</el-button>
              </div>
            </div>
            <el-empty v-else description="暂无待办" />

            <el-divider content-position="left">关键统计</el-divider>
            <div v-if="showStats" class="stat-grid">
              <div class="stat-item">
                <div class="stat-label">本月处置数量</div>
                <div class="stat-value">{{ formatNumber(stats.monthDisposals) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">成交金额</div>
                <div class="stat-value">¥{{ formatAmount(stats.totalAmount) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">流拍率</div>
                <div class="stat-value">{{ formatRate(stats.failRate) }}</div>
              </div>
            </div>
          </template>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card class="overview-card">
          <template #header>
            <div class="card-header">
              <span>最新动态</span>
              <el-button text type="primary" @click="goTo('/auction')">查看拍卖</el-button>
            </div>
          </template>
              <!-- 最新动态表格 -->
              <el-table v-if="recentAuctions.length" :data="recentAuctions" stripe>
            <el-table-column prop="assetName" label="资产名称" min-width="160" />
            <el-table-column prop="currentPrice" label="当前价格" width="120" align="right">
              <template #default="{ row }">
                ¥{{ formatAmount(row.currentPrice) }}
              </template>
            </el-table-column>
            <el-table-column label="开始时间" width="180">
              <template #default="{ row }">
                {{ formatTime(getAuctionStartTime(row)) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无拍卖动态" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
// 导入依赖
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import {
  Goods, Histogram, Box, DataAnalysis, User, Wallet
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getDashboardStats,
  getAuctions,
  getMyPendingPaymentCount,
  getFinanceTransactions,
  getPendingApprovals,
  getPendingDisposalTransactions,
  getAssets
} from '@/api'

// 初始化路由和状态管理
const router = useRouter()
const userStore = useUserStore()

// 计算属性：用户名和角色
const userName = computed(() => userStore.user?.name || userStore.user?.realName || '用户')
const roleLabel = computed(() => {
  const role = userStore.user?.role
  if (!role) return '未设置角色'
  const roleMap = {
    'SYSTEM_ADMIN': '系统管理员',
    'ADMIN': '系统管理员',
    'admin': '系统管理员',
    'ASSET_SPECIALIST': '资产专员',
    'asset_specialist': '资产专员',
    'FINANCE_SPECIALIST': '财务专员',
    'finance_specialist': '财务专员',
    'EMPLOYEE': '普通员工',
    'NORMAL_USER': '普通员工',
    'employee': '普通员工'
  }
  return roleMap[role] || role
})

const showStats = computed(() => !userStore.hasAnyRole('普通员工'))
const isEmployee = computed(() => userStore.hasAnyRole('普通员工'))

const stats = reactive({
  monthDisposals: 0,
  totalAmount: 0,
  failRate: 0
})

const bidStats = reactive({
  inProgress: 0,
  ended: 0,
  wins: 0,
  pendingConfirm: 0,
  pendingPayment: 0
})

const myBidList = ref([])

const todoStats = reactive({
  pendingAssets: 0,
  pendingFinanceApprovals: 0,
  pendingPayments: 0,
  pendingDisposals: 0
})

const recentAuctions = ref([])

const entrySections = computed(() => [
  {
    key: 'asset',
    title: '资产管理',
    icon: Goods,
    roles: ['资产专员', '系统管理员'],
    items: [
      { label: '资产列表', path: '/asset', type: 'primary' },
      { label: '新增资产', path: '/asset/create', type: 'success' },
      { label: '折旧规则管理', path: '/depreciation', type: 'info', plain: true },
      { label: '待审核资产', path: '/asset', query: { status: '待审核' }, type: 'warning', plain: true }
    ]
  },
  {
    key: 'auction',
    title: '拍卖管理',
    icon: Histogram,
    items: [
      { label: '拍卖活动列表', path: '/auction', type: 'primary' },
      { label: '创建拍卖活动', path: '/auction/create', type: 'success', roles: ['资产专员', '系统管理员'] },
      { label: '我的竞拍', path: '/auction/my', type: 'success', roles: ['普通员工'] },
      { label: '进行中拍卖', path: '/auction', query: { status: 'in_progress' }, type: 'warning', plain: true }
    ]
  },
  {
    key: 'disposal',
    title: '资产处置',
    icon: Box,
    roles: ['资产专员', '系统管理员'],
    items: [
      { label: '待处置', path: '/disposal', query: { tab: 'pending' }, type: 'primary' },
      { label: '已处置', path: '/disposal', query: { tab: 'completed' }, type: 'success', plain: true }
    ]
  },
  {
    key: 'statistics',
    title: '统计查询',
    icon: DataAnalysis,
    roles: ['资产专员', '财务专员', '系统管理员', '普通员工'],
    items: [
      { label: '统计总览', path: '/statistics', type: 'primary', roles: ['资产专员', '财务专员', '系统管理员'] },
      { label: '资产处置统计', path: '/statistics/asset', type: 'success', plain: true, roles: ['资产专员', '财务专员', '系统管理员'] },
      { label: '资产处置档案查询', path: '/statistics/archive', type: 'info', plain: true, roles: ['资产专员', '财务专员', '系统管理员', '普通员工'] },
      { label: '员工竞拍统计', path: '/statistics/employee', type: 'warning', plain: true, roles: ['资产专员', '财务专员', '系统管理员'] }
    ]
  },
  {
    key: 'finance',
    title: '财务审核',
    icon: Wallet,
    roles: ['财务专员', '系统管理员'],
    tag: '仅财务',
    items: [
      { label: '待审核', path: '/finance', query: { tab: 'pending' }, type: 'primary' },
      { label: '付款待审核', path: '/finance', query: { tab: 'payment' }, type: 'warning' }
    ]
  },
  {
    key: 'user',
    title: '用户与权限',
    icon: User,
    roles: ['系统管理员'],
    tag: '管理员',
    items: [
      { label: '用户管理', path: '/user', type: 'primary' },
      { label: '部门管理', path: '/department', type: 'success', plain: true }
    ]
  }
])

const visibleEntrySections = computed(() => {
  return entrySections.value
    .filter((section) => !section.roles || userStore.hasAnyRole(...section.roles))
    .map((section) => {
      const items = section.items.filter((item) => !item.roles || userStore.hasAnyRole(...item.roles))
      return { ...section, items }
    })
    .filter((section) => section.items.length > 0)
})

const isSingleEntry = computed(() => visibleEntrySections.value.length === 1)

const todoList = computed(() => {
  const list = []
  if (userStore.hasAnyRole('资产专员', '系统管理员')) {
    list.push({
      label: '待审核资产',
      count: todoStats.pendingAssets,
      path: '/asset',
      query: { status: '待审核' },
      tagType: 'warning'
    })
  }
  if (userStore.hasAnyRole('财务专员', '系统管理员')) {
    list.push({
      label: '待审核',
      count: todoStats.pendingFinanceApprovals,
      path: '/finance',
      query: { tab: 'pending' },
      tagType: 'warning'
    })
  }
  if (userStore.hasAnyRole('财务专员', '系统管理员')) {
    list.push({
      label: '付款待审核',
      count: todoStats.pendingPayments,
      path: '/finance',
      query: { tab: 'payment' },
      tagType: 'danger'
    })
  }
  if (userStore.hasAnyRole('资产专员', '系统管理员')) {
    list.push({
      label: '待处置资产',
      count: todoStats.pendingDisposals,
      path: '/disposal',
      query: { tab: 'pending' },
      tagType: 'warning'
    })
  }
  return list
})

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const formatAmount = (value) => {
  const number = Number(value)
  if (Number.isNaN(number)) return '-'
  return number.toLocaleString()
}

const formatNumber = (value) => {
  const number = Number(value)
  if (Number.isNaN(number)) return '-'
  return number.toLocaleString()
}

const formatRate = (value) => {
  const number = Number(value)
  if (Number.isNaN(number)) return '-'
  const percent = number > 1 ? number : number * 100
  return `${percent.toFixed(2)}%`
}

const formatTodoCount = (value) => {
  const number = Number(value)
  if (Number.isNaN(number)) return '-'
  return `${number} 项`
}

const getStatusType = (status) => {
  const statusMap = {
    '待审核': 'warning',
    '待拍卖': 'info',
    '拍卖中': 'success',
    '已成交': 'success',
    '已处置': 'info',
    '流拍': 'danger',
    'in_progress': 'success',
    'ended': 'info',
    'not_started': 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'not_started': '未开始',
    'PENDING': '未开始',
    'in_progress': '进行中',
    'ended': '已结束',
    '待审核': '待审核',
    '待拍卖': '待拍卖',
    '拍卖中': '拍卖中',
    '已成交': '已成交',
    '已处置': '已处置',
    '流拍': '流拍'
  }
  return statusMap[status] || status
}

const getAuctionStartTime = (row) => row.startTime || row.start_at || row.startDate || row.start || row.createdAt || ''

const sortAuctionsByStartTimeDesc = (list) => {
  return list
    .slice()
    .sort((a, b) => new Date(getAuctionStartTime(b)).getTime() - new Date(getAuctionStartTime(a)).getTime())
}

const pickLatestFiveAuctions = (list) => {
  if (!Array.isArray(list)) return []
  return sortAuctionsByStartTimeDesc(list).slice(0, 5)
}

const fetchLatestFiveAuctions = async () => {
  const pageSize = 50
  const res = await getAuctions({ page: 1, pageSize })
  const list = res.data?.list || []
  if (list.length >= 5) {
    return pickLatestFiveAuctions(list)
  }
  return pickLatestFiveAuctions(list)
}

const loadMyBidSummary = async () => {
  if (!isEmployee.value) return
  bidStats.pendingConfirm = 0
  bidStats.pendingPayment = 0
  bidStats.ended = 0
  try {
    const res = await getAuctions({
      myAuctions: true,
      status: 'in_progress',
      page: 1,
      pageSize: 5
    })
    myBidList.value = res.data?.list || []
    bidStats.inProgress = res.data?.total ?? myBidList.value.length
  } catch (error) {
    console.error('加载我的竞拍概览失败:', error)
  }

  try {
    const res = await getAuctions({
      myAuctions: true,
      status: 'ended',
      page: 1,
      pageSize: 200
    })
    const endedList = res.data?.list || []
    bidStats.ended = res.data?.total ?? endedList.length
  } catch (error) {
    console.error('Failed to load ended auctions count:', error)
  }

  try {
    const res = await getAuctions({
      myAuctions: true,
      status: 'won',
      page: 1,
      pageSize: 200
    })
    const wonList = res.data?.list || []
    bidStats.wins = res.data?.total ?? wonList.length
    bidStats.pendingConfirm = wonList.filter(item => String(item.confirmStatus || '').trim().toLowerCase() === 'pending').length
  } catch (error) {
    console.error('加载待确认成交失败:', error)
  }

  try {
    const res = await getMyPendingPaymentCount()
    bidStats.pendingPayment = Number(res.data) || 0
  } catch (error) {
    console.error('加载待付款数量失败:', error)
  }
}

const loadFinancePendingPaymentTodo = async () => {
  if (!userStore.hasAnyRole('财务专员', '系统管理员')) return
  try {
    const res = await getFinanceTransactions({
      page: 1,
      size: 1,
      confirmStatus: 'confirmed',
      paymentStatus: 'pending'
    })
    todoStats.pendingPayments = Number(res.data?.total) || 0
  } catch (error) {
    console.error('加载付款待审核数量失败:', error)
  }
}

const loadFinancePendingApprovalTodo = async () => {
  if (!userStore.hasAnyRole('财务专员', '系统管理员')) return
  try {
    const res = await getPendingApprovals({
      page: 1,
      size: 1
    })
    todoStats.pendingFinanceApprovals = Number(res.data?.total) || 0
  } catch (error) {
    console.error('加载财务待审核数量失败:', error)
  }
}

const loadPendingAssetTodo = async () => {
  if (!userStore.hasAnyRole('资产专员', '系统管理员')) return
  try {
    const res = await getAssets({
      page: 1,
      pageSize: 1,
      status: '待审核'
    })
    todoStats.pendingAssets = Number(res.data?.total) || 0
  } catch (error) {
    console.error('加载待审核资产数量失败:', error)
  }
}

const loadPendingDisposalTodo = async () => {
  if (!userStore.hasAnyRole('ASSET_SPECIALIST', 'asset_specialist', 'ADMIN', 'admin', 'SYSTEM_ADMIN', 'system_admin')) return
  try {
    const res = await getPendingDisposalTransactions({
      page: 1,
      size: 1
    })
    todoStats.pendingDisposals = Number(res.data?.total) || 0
  } catch (error) {
    console.error('Failed to load pending disposal count:', error)
  }
}

const goTo = (path, query = {}) => {
  router.push({ path, query })
}

onMounted(async () => {
  try {
    const statsData = await getDashboardStats()
    const data = statsData?.data || {}
    stats.monthDisposals = data.monthDisposals ?? data.disposedAssets ?? 0
    stats.totalAmount = data.totalAmount ?? 0
    stats.failRate = data.failRate ?? 0

    todoStats.pendingAssets = data.pendingAssets ?? 0
    todoStats.pendingPayments = data.pendingPayments ?? 0
    todoStats.pendingDisposals = data.pendingDisposals ?? 0

    bidStats.ended = data.myBidsEnded ?? 0
    bidStats.wins = data.myBidsWins ?? 0

    await loadMyBidSummary()
    await loadPendingAssetTodo()
    await loadFinancePendingApprovalTodo()
    await loadFinancePendingPaymentTodo()
    await loadPendingDisposalTodo()

    try {
      recentAuctions.value = await fetchLatestFiveAuctions()
    } catch (error) {
      const recentAuctionList = Array.isArray(data.recentAuctions) ? data.recentAuctions : []
      recentAuctions.value = pickLatestFiveAuctions(recentAuctionList)
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.welcome-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.4;
}

.welcome-desc {
  color: #909399;
  font-size: 14px;
}

.entry-row {
  margin-bottom: 20px;
}

.entry-row.single .entry-card {
  width: 100%;
}

.entry-card {
  height: 100%;
}

.entry-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.entry-icon {
  color: #409eff;
}

.entry-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  box-shadow: inset 0 0 0 1px rgba(64, 158, 255, 0.08);
}

.todo-main {
  display: flex;
  align-items: center;
  gap: 10px;
}

.todo-title {
  font-size: 14px;
  color: #303133;
}

.bid-summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bid-hint {
  color: #909399;
  font-size: 13px;
}

.bid-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.bid-stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.bid-item {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 12px;
}

.bid-item.clickable {
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.bid-item.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(64, 158, 255, 0.12);
}

.bid-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 6px;
}

.bid-value {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.bid-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bid-list-title {
  font-size: 13px;
  color: #909399;
}

.bid-row {
  display: grid;
  grid-template-columns: 1fr auto auto;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f9fafc;
}

.bid-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bid-price {
  font-size: 13px;
  color: #606266;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-item {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 12px;
}

.stat-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 6px;
}

.stat-value {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

@media (max-width: 992px) {
  .stat-grid,
  .bid-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-grid,
  .bid-stats {
    grid-template-columns: 1fr;
  }
}
</style>
