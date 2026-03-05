<template>
  <div class="archive-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-main">
            <div class="page-title">{{ archivePageTitle }}</div>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="资产编号">
          <el-input v-model="queryForm.code" placeholder="请输入资产编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="queryForm.name" placeholder="请输入资产名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="archiveList" stripe border>
        <el-table-column prop="code" label="资产编号" min-width="170" />
        <el-table-column prop="name" label="资产名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.status || '已处置' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归档时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openArchive(row)">查看档案</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          background
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          @current-change="loadArchiveList"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="资产处置档案详情" size="60%" destroy-on-close>
      <div v-loading="detailLoading" class="detail-wrap">
        <template v-if="archiveDetail.asset">
          <el-card shadow="never" class="detail-card">
            <template #header><span>资产基本信息</span></template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="资产编号">{{ archiveDetail.asset?.code || '-' }}</el-descriptions-item>
              <el-descriptions-item label="资产名称">{{ archiveDetail.asset?.name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="资产分类">{{ archiveDetail.asset?.category || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ archiveDetail.asset?.status || '-' }}</el-descriptions-item>
              <el-descriptions-item label="购置日期">{{ archiveDetail.asset?.purchaseDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="当前价值">¥{{ formatAmount(archiveDetail.asset?.currentValue) }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card shadow="never" class="detail-card">
            <template #header><span>交易单与处置信息</span></template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="交易单号">{{ archiveDetail.transaction?.code || '-' }}</el-descriptions-item>
              <el-descriptions-item label="成交金额">¥{{ formatAmount(archiveDetail.transaction?.finalPrice) }}</el-descriptions-item>
              <el-descriptions-item label="中标者">{{ archiveDetail.winnerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系方式">{{ archiveDetail.winnerPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属部门">{{ archiveDetail.winnerDepartment || '-' }}</el-descriptions-item>
              <el-descriptions-item label="中标时间">{{ formatDateTime(archiveDetail.winTime) }}</el-descriptions-item>
              <el-descriptions-item label="确认成交时间">{{ formatDateTime(archiveDetail.transaction?.confirmTime) }}</el-descriptions-item>
              <el-descriptions-item label="付款确认时间">{{ formatDateTime(archiveDetail.transaction?.paymentTime) }}</el-descriptions-item>
              <el-descriptions-item label="处置完成时间" :span="2">{{ formatDateTime(archiveDetail.transaction?.disposalTime) }}</el-descriptions-item>
              <el-descriptions-item label="付款凭证" :span="2">
                <div v-if="paymentVoucherUrls.length" class="voucher-grid">
                  <el-image
                    v-for="(url, index) in paymentVoucherUrls"
                    :key="`pay-${index}`"
                    :src="url"
                    fit="cover"
                    :preview-src-list="paymentVoucherUrls"
                    :initial-index="index"
                    class="voucher-image"
                  />
                </div>
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item label="处置凭证" :span="2">
                <div v-if="disposalVoucherUrls.length" class="voucher-grid">
                  <el-image
                    v-for="(url, index) in disposalVoucherUrls"
                    :key="`disposal-${index}`"
                    :src="url"
                    fit="cover"
                    :preview-src-list="disposalVoucherUrls"
                    :initial-index="index"
                    class="voucher-image"
                  />
                </div>
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card shadow="never" class="detail-card">
            <template #header><span>流程记录</span></template>
            <el-timeline>
              <el-timeline-item
                v-for="item in archiveHistory"
                :key="item.id"
                :timestamp="formatDateTime(item.createTime)"
                :type="getTimelineType(item.operation)"
              >
                <div class="history-item">
                  <div class="history-title">{{ item.operation || '-' }}</div>
                  <div class="history-meta">操作人：{{ item.operatorName || '-' }}</div>
                  <div class="history-content">{{ item.content || '-' }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </template>

        <el-empty v-else-if="!detailLoading" description="暂无档案详情" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { getDisposedArchiveAssets, getDisposedArchiveDetail } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isAdminView = computed(() => userStore.hasAnyRole('系统管理员'))
const isNormalUserView = computed(() => userStore.hasAnyRole('普通员工'))
const isScopedDepartmentView = computed(() => !isAdminView.value && !isNormalUserView.value)
const scopedDepartmentName = computed(() => {
  const directName = userStore.user?.departmentName
  if (directName && String(directName).trim()) {
    return String(directName).trim()
  }
  return '本部门'
})
const archivePageTitle = computed(() => (
  isNormalUserView.value
    ? '我的资产处置档案查询'
    : (isScopedDepartmentView.value ? `资产处置档案查询 · ${scopedDepartmentName.value}` : '资产处置档案查询')
))

const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)

const queryForm = reactive({
  code: '',
  name: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const archiveList = ref([])
const archiveDetail = reactive({
  asset: null,
  transaction: null,
  history: [],
  bids: [],
  winnerName: '',
  winnerPhone: '',
  winnerDepartment: '',
  winTime: ''
})

const paymentVoucherUrls = computed(() => splitVoucherUrls(archiveDetail.transaction?.paymentVoucher))
const disposalVoucherUrls = computed(() => splitVoucherUrls(archiveDetail.transaction?.disposalVoucher))
const archiveHistory = computed(() => Array.isArray(archiveDetail.history) ? archiveDetail.history : [])

const splitVoucherUrls = (value) => {
  if (!value) return []
  return String(value)
    .replace(/\r?\n/g, ',')
    .split(',')
    .map(item => item.trim())
    .filter(Boolean)
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const text = dayjs(value)
  return text.isValid() ? text.format('YYYY-MM-DD HH:mm:ss') : String(value)
}

const formatAmount = (value) => {
  const number = Number(value)
  if (Number.isNaN(number)) return '-'
  return number.toLocaleString()
}

const getTimelineType = (operation) => {
  const op = String(operation || '')
  if (op.includes('拒绝') || op.includes('违约')) return 'danger'
  if (op.includes('确认') || op.includes('审核通过')) return 'success'
  if (op.includes('创建')) return 'primary'
  return 'info'
}

const loadArchiveList = async () => {
  loading.value = true
  try {
    const res = await getDisposedArchiveAssets({
      code: queryForm.code || undefined,
      name: queryForm.name || undefined,
      page: pagination.page,
      size: pagination.size
    })
    archiveList.value = res.data?.list || []
    pagination.total = Number(res.data?.total) || 0
  } catch (error) {
    console.error('加载资产处置档案列表失败:', error)
    archiveList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const resetArchiveDetail = () => {
  archiveDetail.asset = null
  archiveDetail.transaction = null
  archiveDetail.history = []
  archiveDetail.bids = []
  archiveDetail.winnerName = ''
  archiveDetail.winnerPhone = ''
  archiveDetail.winnerDepartment = ''
  archiveDetail.winTime = ''
}

const openArchive = async (row) => {
  if (!row?.id) return
  detailVisible.value = true
  detailLoading.value = true
  resetArchiveDetail()
  try {
    const res = await getDisposedArchiveDetail(row.id)
    const detail = res.data || {}
    archiveDetail.asset = detail.asset || null
    archiveDetail.transaction = detail.transaction || null
    archiveDetail.history = detail.history || []
    archiveDetail.bids = detail.bids || []
    archiveDetail.winnerName = detail.winnerName || ''
    archiveDetail.winnerPhone = detail.winnerPhone || ''
    archiveDetail.winnerDepartment = detail.winnerDepartment || ''
    archiveDetail.winTime = detail.winTime || ''
  } catch (error) {
    console.error('加载资产处置档案详情失败:', error)
  } finally {
    detailLoading.value = false
  }
}

const handleQuery = () => {
  pagination.page = 1
  loadArchiveList()
}

const handleReset = () => {
  queryForm.code = ''
  queryForm.name = ''
  pagination.page = 1
  loadArchiveList()
}

const handleSizeChange = () => {
  pagination.page = 1
  loadArchiveList()
}

onMounted(() => {
  loadArchiveList()
})
</script>

<style scoped>
.archive-page {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
}

.header-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.query-form {
  margin-bottom: 16px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-card {
  border-radius: 10px;
}

.voucher-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
  gap: 8px;
  width: 100%;
}

.voucher-image {
  width: 100%;
  height: 96px;
  border-radius: 8px;
}

.history-item {
  background: #f7f9fc;
  border: 1px solid #edf1f7;
  border-radius: 8px;
  padding: 10px 12px;
}

.history-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.history-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.history-content {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

@media (max-width: 768px) {
  :deep(.el-drawer) {
    width: 100% !important;
  }

  .pagination-wrap {
    justify-content: center;
  }
}
</style>
