<template>
  <!-- 拍卖列表页面 -->
  <div class="auction-list">
    <el-card>
      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <!-- 查询条件：拍卖名称、状态 -->
        <el-form-item label="拍卖名称">
          <el-input v-model="queryForm.name" placeholder="请输入拍卖名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="未开始" value="not_started" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已结束" value="ended" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <!-- 操作按钮：查询、重置、创建 -->
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button
            v-if="userStore.hasRole('asset_specialist') || userStore.hasRole('admin')"
            type="success"
            @click="handleCreate"
          >
            创建拍卖
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 拍卖表格 -->
      <el-table
        v-loading="loading"
        :data="auctionList"
        stripe
        border
      >
        <!-- 表格列：基本信息、价格、时间、状态 -->
        <el-table-column prop="name" label="拍卖名称" width="200" />
        <el-table-column label="资产信息" min-width="250">
          <template #default="{ row }">
            <div class="asset-info">
              <div class="asset-name">{{ row.assetName }}</div>
              <div class="asset-spec">{{ row.assetSpecification }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="startPrice" label="起拍价(元)" width="120" align="right">
          <template #default="{ row }">¥{{ row.startPrice?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="currentPrice" label="当前价格(元)" width="120" align="right">
          <template #default="{ row }">¥{{ row.currentPrice?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="incrementAmount" label="加价幅度(元)" width="130" align="right" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="bidCount" label="竞价次数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 操作列：查看、竞拍（仅普通员工） -->
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === 'in_progress' && !userStore.hasRole('asset_specialist') && !userStore.isAdmin() && !userStore.isFinanceSpecialist()"
              text
              type="success"
              size="small"
              @click="handleBid(row)"
            >
              竞拍
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-model="queryForm.page"
        :page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
// 导入依赖
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAuctions } from '@/api'

// 初始化路由和状态管理
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const auctionList = ref([])
const total = ref(0)

// 分页大小持久化
const PAGE_SIZE_KEY = 'auction_page_size'
const getUserKey = () => userStore.user?.id || userStore.user?.username || userStore.user?.account || 'guest'
const getPageSizeKey = () => `${PAGE_SIZE_KEY}_${getUserKey()}`
const getSavedPageSize = () => Number(localStorage.getItem(getPageSizeKey())) || 10

// 查询表单数据
const queryForm = reactive({
  name: '',
  status: '',
  page: 1,
  pageSize: getSavedPageSize()
})

// 允许的状态列表
const allowedStatusList = ['not_started', 'in_progress', 'ended', 'PENDING']

// 应用路由参数中的状态筛选
const applyRouteStatus = () => {
  const status = route.query.status
  if (typeof status === 'string' && allowedStatusList.includes(status)) {
    queryForm.status = status
    queryForm.page = 1
  }
}

// 获取状态标签类型
const getStatusType = (status) => {
  const statusMap = {
    not_started: 'info',
    PENDING: 'info',
    in_progress: 'success',
    ended: 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    not_started: '未开始',
    PENDING: '未开始',
    in_progress: '进行中',
    ended: '已结束'
  }
  return statusMap[status] || status
}

// 加载拍卖列表
const loadAuctions = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      name: queryForm.name?.trim() || undefined,
      status: queryForm.status || undefined
    }

    const res = await getAuctions(params)
    auctionList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('加载拍卖列表失败:', error)
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.status,
  () => {
    applyRouteStatus()
    loadAuctions()
  }
)

const handleQuery = () => {
  queryForm.page = 1
  loadAuctions()
}

const handleReset = () => {
  Object.assign(queryForm, {
    name: '',
    status: '',
    page: 1,
    pageSize: getSavedPageSize()
  })
  loadAuctions()
}

const handleCreate = () => {
  router.push('/auction/create')
}

const handleView = (row) => {
  router.push(`/auction/${row.id}`)
}

const handleBid = (row) => {
  router.push(`/auction/${row.id}`)
}

const handleSizeChange = (pageSize) => {
  queryForm.pageSize = pageSize
  queryForm.page = 1
  localStorage.setItem(getPageSizeKey(), String(pageSize))
  loadAuctions()
}

const handleCurrentChange = (page) => {
  queryForm.page = page
  loadAuctions()
}

onMounted(() => {
  applyRouteStatus()
  loadAuctions()
})
</script>

<style scoped>
.auction-list {
  padding: 20px;
}

.query-form {
  margin-bottom: 20px;
}

.asset-info {
  line-height: 1.5;
}

.asset-name {
  font-weight: bold;
  color: #333;
}

.asset-spec {
  font-size: 12px;
  color: #999;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>