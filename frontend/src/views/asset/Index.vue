<template>
  <!-- 资产列表页面 -->
  <div class="asset-list">
    <el-card>
      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <!-- 查询条件：资产编号、名称、状态、部门 -->
        <el-form-item label="资产编号">
          <el-input v-model="queryForm.code" placeholder="请输入资产编号" clearable />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="queryForm.name" placeholder="请输入资产名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审核" value="待审核" />
            <el-option label="待拍卖" value="待拍卖" />
            <el-option label="拍卖中" value="拍卖中" />
            <el-option label="已成交" value="已成交" />
            <el-option label="已处置" value="已处置" />
            <el-option label="流拍" value="流拍" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用部门">
          <el-select v-model="queryForm.departmentId" placeholder="请选择部门" clearable>
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <!-- 操作按钮：查询、重置、新增 -->
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button
            v-if="userStore.hasRole('asset_specialist') || userStore.hasRole('admin')"
            type="success"
            @click="handleCreate"
          >
            新增资产
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 资产表格 -->
      <el-table
        v-loading="loading"
        :data="assetList"
        stripe
        border
      >
        <!-- 表格列：基本信息、价值、部门、状态 -->
        <el-table-column prop="code" label="资产编号" width="150" />
        <el-table-column prop="name" label="资产名称" width="200" />
        <el-table-column prop="specification" label="规格描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="originalValue" label="原值 (元)" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.originalValue?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="currentValue" label="当前价值(元)" width="130" align="right">
          <template #default="{ row }">
            ¥{{ row.currentValue?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="startPrice" label="起拍价(元)" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.startPrice?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="使用部门" width="120" />
        <el-table-column prop="purchaseDate" label="购置日期" width="110" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 操作列：查看、编辑、重新定价、审核 -->
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="canEdit(row)"
              text
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canRecalculate(row)"
              text
              type="warning"
              size="small"
              @click="handleRecalculate(row)"
            >
              重新定价
            </el-button>
            <el-button
              v-if="canApprove(row)"
              text
              type="success"
              size="small"
              @click="handleApprove(row)"
            >
              审核通过
            </el-button>
            <el-button
              v-if="canApprove(row)"
              text
              type="danger"
              size="small"
              @click="handleReject(row)"
            >
              审核拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getAssets, getDepartments, recalculateAssetValue, approveAssetById, rejectAssetById } from '@/api'

// 初始化路由和状态管理
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const assetList = ref([])
const total = ref(0)
const departments = ref([])

// 分页大小持久化（基于用户 ID）
const PAGE_SIZE_KEY = 'asset_page_size'
const getUserKey = () => userStore.user?.id || userStore.user?.username || userStore.user?.account || 'guest'
const getPageSizeKey = () => `${PAGE_SIZE_KEY}_${getUserKey()}`
const getSavedPageSize = () => Number(localStorage.getItem(getPageSizeKey())) || 10

// 查询表单数据
const queryForm = reactive({
  code: '',
  name: '',
  status: null,
  departmentId: null,
  page: 1,
  pageSize: getSavedPageSize()
})

// 允许的状态列表
const allowedStatusList = ['待审核', '待拍卖', '拍卖中', '已成交', '已处置', '流拍']

// 应用路由参数中的状态筛选
const applyRouteStatus = () => {
  const status = route.query.status
  if (typeof status === 'string' && allowedStatusList.includes(status)) {
    queryForm.status = status
    queryForm.page = 1
  }
}

// 构建查询参数
const buildQueryParams = () => {
  const params = {
    page: queryForm.page,
    pageSize: queryForm.pageSize
  }

  if (queryForm.code?.trim()) {
    params.code = queryForm.code.trim()
  }

  if (queryForm.name?.trim()) {
    params.name = queryForm.name.trim()
  }

  if (queryForm.status) {
    params.status = queryForm.status
  }

  if (queryForm.departmentId) {
    params.departmentId = queryForm.departmentId
  }

  return params
}

const canEdit = (row) => {
  return (userStore.hasRole('asset_specialist') || userStore.hasRole('admin')) &&
         ['待审核', '待拍卖', '流拍'].includes(row.status)
}

const canRecalculate = (row) => {
  return (userStore.hasRole('asset_specialist') || userStore.hasRole('admin')) &&
         ['待审核', '待拍卖', '流拍'].includes(row.status)
}

const canApprove = (row) => {
  return (userStore.hasRole('asset_specialist') || userStore.hasRole('finance_specialist') || userStore.hasRole('admin')) &&
         row.status === '待审核'
}

const getStatusType = (status) => {
  const statusMap = {
    '待审核': 'warning',
    '待拍卖': 'info',
    '拍卖中': 'success',
    '已成交': 'success',
    '已处置': 'info',
    '流拍': 'danger'
  }
  return statusMap[status] || 'info'
}

const loadDepartments = async () => {
  try {
    const res = await getDepartments()
    departments.value = res.data
  } catch (error) {
    console.error('加载部门失败:', error)
  }
}

const loadAssets = async () => {
  loading.value = true
  try {
    const res = await getAssets(buildQueryParams())
    assetList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('加载资产列表失败:', error)
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.status,
  () => {
    applyRouteStatus()
    loadAssets()
  }
)

const handleQuery = () => {
  queryForm.page = 1
  loadAssets()
}

const handleReset = () => {
  Object.assign(queryForm, {
    code: '',
    name: '',
    status: null,
    departmentId: null,
    page: 1,
    pageSize: getSavedPageSize()
  })
  loadAssets()
}

const handleSizeChange = (pageSize) => {
  queryForm.pageSize = pageSize
  queryForm.page = 1
  localStorage.setItem(getPageSizeKey(), String(pageSize))
  loadAssets()
}

const handleCurrentChange = (page) => {
  queryForm.page = page
  loadAssets()
}

const handleCreate = () => {
  router.push('/asset/create')
}

const handleView = (row) => {
  router.push(`/asset/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/asset/${row.id}/edit`)
}

const handleRecalculate = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要重新计算该资产的定价吗？计算后将进入待审核状态。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await recalculateAssetValue(row.id)
    ElMessage.success('重新定价成功')
    loadAssets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新定价失败:', error)
    }
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要审核通过该资产吗？审核通过后将进入待拍卖状态。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    await approveAssetById(row.id, userStore.user?.id)
    ElMessage.success('审核通过成功')
    loadAssets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
    }
  }
}

const handleReject = async (row) => {
  try {
    const reason = prompt('请输入拒绝原因：')
    if (!reason || reason.trim() === '') {
      return
    }
    await rejectAssetById(row.id, reason, userStore.user?.id)
    ElMessage.success('审核拒绝成功')
    loadAssets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核拒绝失败:', error)
    }
  }
}

onMounted(() => {
  applyRouteStatus()
  loadDepartments()
  loadAssets()
})
</script>

<style scoped>
.asset-list {
  padding: 20px;
}

.query-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
