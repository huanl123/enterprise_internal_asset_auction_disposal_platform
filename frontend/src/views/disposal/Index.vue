<template>
  <!-- 资产处置管理页面 -->
  <div class="disposal-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>资产处置</span>
        </div>
      </template>

      <!-- 标签页切换：待处置/已处置/处置记录 -->
      <el-tabs v-model="activeTab">
        <!-- 待处置资产列表 -->
        <el-tab-pane label="待处置" name="pending">
          <!-- 查询表单 -->
          <el-form :inline="true" :model="queryForm" class="query-form">
            <el-form-item label="资产编号">
              <el-input v-model="queryForm.assetCode" placeholder="请输入资产编号" clearable />
            </el-form-item>
            <el-form-item label="资产名称">
              <el-input v-model="queryForm.assetName" placeholder="请输入资产名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 待处置资产表格 -->
          <el-table
            v-loading="loading"
            :data="pendingList"
            stripe
            border
          >
            <el-table-column prop="assetCode" label="资产编号" width="150" />
            <el-table-column prop="assetName" label="资产名称" width="200" />
            <el-table-column prop="winnerName" label="中标者" width="100" />
            <el-table-column prop="finalPrice" label="成交价(元)" width="120" align="right">
              <template #default="{ row }">¥{{ row.finalPrice?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="confirmTime" label="成交确认时间" width="180" />
            <el-table-column prop="paymentTime" label="付款确认时间" width="180" />
            <!-- 操作列：确认处置、查看 -->
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleConfirm(row)">
                  确认处置
                </el-button>
                <el-button text type="info" size="small" @click="handleView(row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 已处置资产列表 -->
        <el-tab-pane label="已处置" name="completed">
          <!-- 已完成表格 -->
          <el-table
            v-loading="loading"
            :data="completedList"
            stripe
            border
          >
            <el-table-column prop="assetCode" label="资产编号" width="150" />
            <el-table-column prop="assetName" label="资产名称" width="200" />
            <el-table-column prop="winnerName" label="中标者" width="100" />
            <el-table-column prop="finalPrice" label="成交价(元)" width="120" align="right">
              <template #default="{ row }">¥{{ row.finalPrice?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="disposalTime" label="处置完成时间" width="180" />
            <el-table-column prop="handler" label="处置人" width="100" />
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="handleView(row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 处置历史记录 -->
        <el-tab-pane label="处置记录" name="history">
          <!-- 历史记录表格 -->
          <el-table
            v-loading="loading"
            :data="historyList"
            stripe
            border
          >
            <el-table-column prop="assetCode" label="资产编号" width="150" />
            <el-table-column prop="assetName" label="资产名称" width="200" />
            <el-table-column prop="action" label="操作类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getActionType(row.action)">
                  {{ row.action }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="操作人" width="100" />
            <el-table-column prop="operationTime" label="操作时间" width="180" />
            <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 确认处置对话框 -->
    <el-dialog
      v-model="confirmDialogVisible"
      title="确认资产处置"
      width="700px"
    >
      <!-- 资产信息展示 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="资产编号">{{ currentDisposal.assetCode }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ currentDisposal.assetName }}</el-descriptions-item>
        <el-descriptions-item label="中标者">{{ currentDisposal.winnerName }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ currentDisposal.winnerPhone }}</el-descriptions-item>
        <el-descriptions-item label="成交价" :span="2">
          ¥{{ currentDisposal.finalPrice?.toLocaleString() }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 处置表单：备注、凭证上传 -->
      <el-divider />

      <el-form :model="confirmForm" label-width="120px">
        <el-form-item label="处置备注" required>
          <el-input
            v-model="confirmForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入处置备注，如交接方式、交接时间等"
          />
        </el-form-item>
        <el-form-item label="上传凭证" required>
          <el-upload
            v-model:file-list="confirmFileList"
            list-type="picture-card"
            :limit="1"
            :auto-upload="false"
            :before-upload="beforeConfirmUpload"
            :on-change="handleConfirmChange"
            :on-preview="handlePreview"
            :on-remove="handleConfirmRemove"
          >
            <el-button type="primary">选择处置凭证</el-button>
          </el-upload>
          <div style="color: #999; font-size: 12px; margin-top: 5px">
            请上传交接单签字扫描件或其他处置凭证
          </div>
        </el-form-item>
      </el-form>

      <!-- 对话框按钮：取消、确认处置 -->
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleSubmitConfirm">确认处置</el-button>
      </template>
    </el-dialog>
  
  <!-- 图片预览对话框 -->
  <el-dialog v-model="previewDialogVisible" title="预览" width="600px">
    <el-image :src="previewUrl" style="width: 100%" fit="contain" />
  </el-dialog>
</div>
</template>

<script setup>
// 导入依赖
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { 
  getPendingDisposalTransactions,
  getCompletedDisposalTransactions,
  getDisposalHistory,
  confirmDisposal
} from '@/api'

// 初始化路由和状态管理
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

// 响应式数据
const activeTab = ref('pending') // 当前激活的标签页
const loading = ref(false)
const pendingList = ref([]) // 待处置列表
const completedList = ref([]) // 已完成列表
const historyList = ref([]) // 历史记录列表

// 允许的标签页
const allowedTabs = ['pending', 'completed', 'history']

// 应用路由参数中的 tab
const applyRouteTab = () => {
  const tab = route.query.tab
  if (typeof tab === 'string' && allowedTabs.includes(tab)) {
    activeTab.value = tab
  }
}

// 监听 tab 切换，加载对应数据
watch(activeTab, (tab) => {
  if (tab === 'pending') {
    loadPendingList()
  } else if (tab === 'completed') {
    loadCompletedList()
  } else if (tab === 'history') {
    loadHistoryList()
  }
})

watch(
  () => route.query.tab,
  () => {
    applyRouteTab()
  }
)

const confirmDialogVisible = ref(false)
const currentDisposal = reactive({})
const confirmFileList = ref([])
const confirmVoucherFile = ref(null)

const previewDialogVisible = ref(false)
const previewUrl = ref('')

const queryForm = reactive({
  assetCode: '',
  assetName: ''
})

const confirmForm = reactive({
  remark: ''
})

const getActionType = (action) => {
  const typeMap = {
    '确认成交': 'success',
    '确认收款': 'success',
    '确认处置': 'success',
    '拒绝付款': 'danger',
    '取消交易': 'warning'
  }
  return typeMap[action] || 'info'
}

const loadPendingList = async () => {
  loading.value = true
  try {
    const response = await getPendingDisposalTransactions({ page: 1, size: 100 })
    if (response && response.code === 200) {
      const list = response.data?.list || []
      const filtered = list.filter(item => {
        const codeOk = !queryForm.assetCode || (item.assetCode || '').includes(queryForm.assetCode)
        const nameOk = !queryForm.assetName || (item.assetName || '').includes(queryForm.assetName)
        return codeOk && nameOk
      })
      pendingList.value = filtered
    } else {
      pendingList.value = []
      ElMessage.error(response?.message || '加载待处置列表失败')
    }
  } catch (error) {
    console.error('加载待处置列表失败:', error)
    ElMessage.error('加载待处置列表失败：' + (error.response?.data?.message || error.message))
    pendingList.value = []
  } finally {
    loading.value = false
  }
}

const loadCompletedList = async () => {
  loading.value = true
  try {
    const [txResponse, historyResponse] = await Promise.all([
      getCompletedDisposalTransactions({ page: 1, size: 100 }),
      getDisposalHistory()
    ])

    let handlerMap = {}
    if (historyResponse && historyResponse.code === 200 && Array.isArray(historyResponse.data)) {
      handlerMap = historyResponse.data.reduce((map, item) => {
        const assetId = item?.assetId
        const action = item?.action
        if (assetId != null && action === '确认处置') {
          if (!map[assetId]) {
            map[assetId] = item?.operator || '-'
          }
        }
        return map
      }, {})
    }

    if (txResponse && txResponse.code === 200) {
      const list = txResponse.data?.list || []
      const filtered = list.filter(item => {
        const codeOk = !queryForm.assetCode || (item.assetCode || '').includes(queryForm.assetCode)
        const nameOk = !queryForm.assetName || (item.assetName || '').includes(queryForm.assetName)
        return codeOk && nameOk
      })
      completedList.value = filtered.map(item => ({
        ...item,
        handler: item.handler || handlerMap[item.assetId] || '-'
      }))
    } else {
      completedList.value = []
      ElMessage.error(txResponse?.message || '加载已处置列表失败')
    }
  } catch (error) {
    console.error('加载已处置列表失败:', error)
    ElMessage.error('加载已处置列表失败：' + (error.response?.data?.message || error.message))
    completedList.value = []
  } finally {
    loading.value = false
  }
}

const loadHistoryList = async () => {
  loading.value = true
  try {
    const response = await getDisposalHistory()
    if (response && response.code === 200) {
      historyList.value = response.data || []
    } else {
      historyList.value = []
      ElMessage.error(response?.message || '加载处置记录失败')
    }
  } catch (error) {
    console.error('加载处置记录失败:', error)
    ElMessage.error('加载处置记录失败：' + (error.response?.data?.message || error.message))
    historyList.value = []
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  if (activeTab.value === 'pending') {
    loadPendingList()
  } else if (activeTab.value === 'completed') {
    loadCompletedList()
  } else if (activeTab.value === 'history') {
    loadHistoryList()
  }
}

const handleReset = () => {
  queryForm.assetCode = ''
  queryForm.assetName = ''
  handleQuery()
}

const handleView = (row) => {
  const assetId = row?.assetId || row?.id
  if (!assetId) {
    ElMessage.warning('缺少资产ID，无法查看详情')
    return
  }
  router.push(`/disposal/${assetId}`)
}

const handleConfirm = (row) => {
  Object.assign(currentDisposal, row)
  confirmForm.remark = ''
  confirmFileList.value = []
  confirmVoucherFile.value = null
  confirmDialogVisible.value = true
}

const beforeConfirmUpload = (file) => {
  confirmVoucherFile.value = file
  return false
}

const handleConfirmChange = (file, fileList) => {
  confirmFileList.value = fileList || []
  confirmVoucherFile.value = file?.raw || confirmFileList.value?.[0]?.raw || file || null
}

const handleConfirmRemove = () => {
  confirmVoucherFile.value = null
  confirmFileList.value = []
}

const handlePreview = (file) => {
  const url = file?.url || (file?.raw ? URL.createObjectURL(file.raw) : '')
  if (!url) return
  previewUrl.value = url
  previewDialogVisible.value = true
}

const handleSubmitConfirm = async () => {
  if (!confirmForm.remark) {
    ElMessage.warning('请输入处置备注')
    return
  }

  const voucherFile = confirmVoucherFile.value || confirmFileList.value?.[0]?.raw || null
  if (!voucherFile) {
    ElMessage.warning('请选择处置凭证')
    return
  }

  try {
    // 后端接口：POST /api/disposal/assets/{id}/confirm （id 为资产ID）
    const assetId = currentDisposal.assetId || currentDisposal.id
    await confirmDisposal({
      assetId,
      remark: confirmForm.remark,
      voucher: voucherFile
    })
    ElMessage.success('处置确认成功')
    confirmDialogVisible.value = false
    loadPendingList()
  } catch (error) {
    console.error('确认处置失败:', error)
  }
}

onMounted(() => {
  applyRouteTab()
  handleQuery()
})
</script>

<style scoped>
.disposal-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}
</style>
