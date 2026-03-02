<template>
  <div class="finance-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>财务审核</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="待审核" name="pending">
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

          <el-table
            v-loading="loading"
            :data="pendingList"
            stripe
            border
          >
            <el-table-column prop="code" label="资产编号" width="150" />
            <el-table-column prop="name" label="资产名称" width="200" />
            <el-table-column prop="originalValue" label="原值(元)" width="120" align="right">
              <template #default="{ row }">¥{{ row.originalValue?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="currentValue" label="当前价值(元)" width="130" align="right">
              <template #default="{ row }">¥{{ row.currentValue?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="startPrice" label="建议起拍价(元)" width="140" align="right">
              <template #default="{ row }">¥{{ row.startPrice?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="180">
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleApprove(row)">
                  审核
                </el-button>
                <el-button text type="info" size="small" @click="handleView(row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="付款待审核" name="payment">
          <el-table
            v-loading="loading"
            :data="paymentList"
            stripe
            border
          >
            <el-table-column prop="transactionCode" label="交易单号" width="150" />
            <el-table-column prop="assetCode" label="资产编号" width="130" />
            <el-table-column prop="assetName" label="资产名称" width="180" />
            <el-table-column prop="winnerName" label="中标者" width="100" />
            <el-table-column prop="finalPrice" label="成交价(元)" width="120" align="right">
              <template #default="{ row }">¥{{ row.finalPrice?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column label="付款凭证" width="120" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.paymentVoucher"
                  text
                  type="primary"
                  size="small"
                  @click="previewVoucher(row.paymentVoucher)"
                >
                  查看
                </el-button>
                <el-tag v-else size="small" type="warning">未提交</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="paymentDeadline" label="付款截止时间" width="180" />
            <el-table-column prop="confirmTime" label="确认成交时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="handleConfirmPayment(row)">
                  确认收款
                </el-button>
                <el-button type="danger" size="small" @click="handleRejectPayment(row)">
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="审核历史" name="history">
          <el-table
            v-loading="loading"
            :data="historyList"
            stripe
            border
          >
            <el-table-column prop="assetCode" label="资产编号" width="150" />
            <el-table-column prop="assetName" label="资产名称" width="200" />
            <el-table-column prop="reviewer" label="审核人" width="100" />
            <el-table-column prop="result" label="审核结果" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.result === '通过' ? 'success' : 'danger'">
                  {{ row.result }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reviewTime" label="审核时间" width="180" />
            <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 资产审核对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      title="资产定价审核"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="资产编号">{{ currentAsset.code }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ currentAsset.name }}</el-descriptions-item>
        <el-descriptions-item label="原值">¥{{ currentAsset.originalValue?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="当前价值">¥{{ currentAsset.currentValue?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="建议起拍价">¥{{ currentAsset.startPrice?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="折旧规则">{{ currentAsset.depreciationRuleName }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <el-form :model="approveForm" label-width="120px">
        <el-form-item label="开启保留价">
          <el-switch v-model="approveForm.hasReservePrice" />
          <span style="margin-left: 10px; color: #999">
            {{ approveForm.hasReservePrice ? '已开启' : '未开启' }}
          </span>
        </el-form-item>
        <el-form-item label="调整起拍价">
          <el-input-number
            v-model="approveForm.startPrice"
            :min="0"
            :step="100"
            controls-position="right"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #999">元</span>
        </el-form-item>
        <el-form-item v-if="approveForm.hasReservePrice" label="保留价">
          <el-input-number
            v-model="approveForm.reservePrice"
            :min="0"
            :step="100"
            controls-position="right"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #999">元 (默认为当前价值)</span>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input
            v-model="approveForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleRejectApprove">拒绝</el-button>
        <el-button type="primary" @click="handleSubmitApprove">通过审核</el-button>
      </template>
    </el-dialog>

    <!-- 付款确认对话框 -->
    <el-dialog
      v-model="paymentDialogVisible"
      title="确认收款"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="交易单号">{{ currentTransaction.code }}</el-descriptions-item>
        <el-descriptions-item label="成交价">¥{{ currentTransaction.finalPrice?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="中标者">{{ currentTransaction.winnerName }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ currentTransaction.winnerPhone }}</el-descriptions-item>
        <el-descriptions-item label="付款截止时间" :span="2">
          {{ currentTransaction.paymentDeadline }}
        </el-descriptions-item>
        <el-descriptions-item label="付款凭证" :span="2">
          <el-link
            v-if="currentTransaction.paymentVoucher"
            type="primary"
            @click="previewVoucher(currentTransaction.paymentVoucher)"
          >
            查看中标者已提交的凭证
          </el-link>
          <span v-else>未提交</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-form :model="paymentForm" label-width="120px" style="margin-top: 20px">
        <el-form-item label="上传凭证">
          <el-upload
            v-model:file-list="paymentFileList"
            list-type="picture-card"
            :limit="1"
            :auto-upload="false"
            :before-upload="beforePaymentUpload"
            :on-change="handlePaymentChange"
            :on-preview="handlePreview"
            :on-remove="handlePaymentRemove"
          >
            <el-button type="primary">选择付款凭证</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input
            v-model="paymentForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="paymentDialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleSubmitPayment">确认收款</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog v-model="previewDialogVisible" title="预览" width="600px">
      <el-image :src="previewUrl" style="width: 100%" fit="contain" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'
import { 
  confirmPayment, 
  rejectPayment, 
  getPendingApprovals, 
  rejectAssetById,
  getFinanceTransactions
} from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('')
const loading = ref(false)
const VALID_TABS = ['pending', 'payment', 'history']

const resolveTab = (tab) => {
  return VALID_TABS.includes(tab) ? tab : 'pending'
}

watch(activeTab, (tab) => {
  if (tab === 'pending') {
    loadPendingList()
  } else if (tab === 'payment') {
    loadPaymentList()
  } else if (tab === 'history') {
    loadHistoryList()
  }
})

watch(
  () => route.query.tab,
  (tab) => {
    activeTab.value = resolveTab(tab)
  },
  { immediate: true }
)
const pendingList = ref([])
const paymentList = ref([])
const historyList = ref([])

const approveDialogVisible = ref(false)
const paymentDialogVisible = ref(false)
const currentAsset = reactive({})
const currentTransaction = reactive({})
const paymentFileList = ref([])
const paymentVoucherFile = ref(null)

const previewDialogVisible = ref(false)
const previewUrl = ref('')

const queryForm = reactive({
  assetCode: '',
  assetName: ''
})

const approveForm = reactive({
  hasReservePrice: false,
  startPrice: 0,
  reservePrice: 0,
  remark: ''
})

const paymentForm = reactive({
  remark: ''
})

const loadPendingList = async () => {
  loading.value = true
  try {
    const params = {
      page: 1,
      size: 100
    }
    
    // 使用API函数获取待审核资产列表
    const response = await getPendingApprovals(params)
    
    if (response && response.code === 200) {
      // 后端返回的是分页结果，需要提取data.list
      const pageResult = response.data
      pendingList.value = pageResult?.list || []
    } else {
      ElMessage.error(response?.message || '加载待审核列表失败')
      pendingList.value = []
    }
  } catch (error) {
    console.error('加载待审核列表失败:', error)
    ElMessage.error('加载待审核列表失败：' + (error.response?.data?.message || error.message))
    pendingList.value = []
  } finally {
    loading.value = false
  }
}

const loadPaymentList = async () => {
  loading.value = true
  try {
    const params = {
      page: 1,
      size: 100,
      confirmStatus: 'confirmed',
      paymentStatus: 'pending'
    }

    const response = await getFinanceTransactions(params)
    if (response && response.code === 200) {
      const pageResult = response.data
      const list = pageResult?.list || []
      paymentList.value = list.map(item => ({
        ...item,
        transactionCode: item.code
      }))
    } else {
      paymentList.value = []
      ElMessage.error(response?.message || '加载付款列表失败')
    }
  } catch (error) {
    console.error('加载付款列表失败:', error)
    ElMessage.error('加载付款列表失败：' + (error.response?.data?.message || error.message))
    paymentList.value = []
  } finally {
    loading.value = false
  }
}

const loadHistoryList = async () => {
  loading.value = true
  try {
    const response = await request.get('/finance/review-history')
    if (response && response.code === 200) {
      historyList.value = response.data || []
    } else {
      historyList.value = []
      ElMessage.error(response?.message || '加载审核历史失败')
    }
  } catch (error) {
    console.error('加载审核历史失败:', error)
    historyList.value = []
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  loadPendingList()
}

const handleReset = () => {
  queryForm.assetCode = ''
  queryForm.assetName = ''
  loadPendingList()
}

const handleView = (row) => {
  if (!row?.id) {
    ElMessage.warning('资产ID不存在，无法查看')
    return
  }
  router.push(`/asset/${row.id}`)
}

const handleApprove = (row) => {
  Object.assign(currentAsset, row)
  approveForm.startPrice = row.startPrice
  approveForm.reservePrice = row.currentValue
  approveDialogVisible.value = true
}

const handleSubmitApprove = async () => {
  try {
    // 验证数据
    if (!currentAsset.id) {
      ElMessage.error('资产ID不能为空')
      return
    }
    
    if (approveForm.startPrice <= 0) {
      ElMessage.error('起拍价必须大于0')
      return
    }
    
    if (approveForm.hasReservePrice && approveForm.reservePrice <= 0) {
      ElMessage.error('保留价必须大于0')
      return
    }
    
    // 调用API提交审核 - request已经配置了baseURL为'/api'
    const response = await request.post(
      `/finance/assets/${currentAsset.id}/approve`,
      {
        startPrice: approveForm.startPrice,
        hasReservePrice: approveForm.hasReservePrice,
        reservePrice: approveForm.hasReservePrice ? approveForm.reservePrice : null,
        remark: approveForm.remark
      }
    )
    
    if (response && response.code === 200) {
      ElMessage.success('审核通过')
      approveDialogVisible.value = false
      // 重置表单
      approveForm.hasReservePrice = false
      approveForm.startPrice = 0
      approveForm.reservePrice = 0
      approveForm.remark = ''
      // 刷新列表
      loadPendingList()
      // 刷新历史（不管当前tab，保证数据最新）
      loadHistoryList()
    } else {
      ElMessage.error(response?.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败：' + (error.response?.data?.message || error.message))
  }
}

const handleRejectApprove = async () => {
  try {
    if (!currentAsset.id) {
      ElMessage.error('资产ID不能为空')
      return
    }
    
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^(?!\s*$).+/,
      inputErrorMessage: '请输入拒绝原因',
      type: 'warning'
    })
    
    if (!reason || reason.trim() === '') {
      ElMessage.warning('拒绝原因不能为空')
      return
    }
    
    // 调用拒绝API
    const response = await rejectAssetById(
      currentAsset.id,
      reason,
      userStore.user.id
    )
    
    if (response && response.code === 200) {
      ElMessage.success('已拒绝')
      approveDialogVisible.value = false
      // 重置表单
      approveForm.hasReservePrice = false
      approveForm.startPrice = 0
      approveForm.reservePrice = 0
      approveForm.remark = ''
      // 刷新列表
      loadPendingList()
      // 刷新历史（不管当前tab，保证数据最新）
      loadHistoryList()
    } else {
      ElMessage.error(response?.message || '拒绝失败')
    }
  } catch (error) {
    if (error === 'cancel') {
      // 用户点击了取消
      return
    }
    console.error('拒绝失败:', error)
    ElMessage.error('拒绝失败：' + (error.response?.data?.message || error.message))
  }
}

const handleConfirmPayment = (row) => {
  Object.assign(currentTransaction, row)
  paymentForm.remark = ''
  paymentFileList.value = []
  paymentVoucherFile.value = null
  paymentDialogVisible.value = true
}

const handleRejectPayment = async (row) => {
  try {
    await ElMessageBox.prompt('请输入拒绝原因', '拒绝付款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入拒绝原因'
    })
    const reason = await ElMessageBox.prompt.result
    await rejectPayment({
      transactionId: row.id,
      reason
    })
    ElMessage.success('已拒绝')
    loadPaymentList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error)
    }
  }
}

const beforePaymentUpload = (file) => {
  // 只选择文件，不在这里直接上传；提交时走后端：POST /api/finance/transactions/{id}/payment
  paymentVoucherFile.value = file
  return false
}

const handlePaymentChange = (file, fileList) => {
  paymentFileList.value = fileList || []
  paymentVoucherFile.value = file?.raw || paymentFileList.value?.[0]?.raw || null
}

const handlePaymentRemove = (file, fileList) => {
  paymentFileList.value = fileList || []
  paymentVoucherFile.value = paymentFileList.value?.[0]?.raw || null
}

const handlePreview = (file) => {
  const url = file?.url || (file?.raw ? URL.createObjectURL(file.raw) : '')
  if (!url) return
  previewUrl.value = url
  previewDialogVisible.value = true
}

const previewVoucher = (url) => {
  if (!url) return
  previewUrl.value = url
  previewDialogVisible.value = true
}

const handleSubmitPayment = async () => {
  if (!paymentVoucherFile.value) {
    paymentVoucherFile.value = paymentFileList.value?.[0]?.raw || null
  }
  const existingVoucher = currentTransaction.paymentVoucher || ''
  if (!paymentVoucherFile.value && !existingVoucher) {
    ElMessage.warning('请上传付款凭证')
    return
  }

  try {
    await confirmPayment({
      transactionId: currentTransaction.id,
      voucher: paymentVoucherFile.value,
      voucherUrl: paymentVoucherFile.value ? '' : existingVoucher,
      remark: paymentForm.remark
    })
    ElMessage.success('确认收款成功')
    paymentDialogVisible.value = false
    loadPaymentList()
  } catch (error) {
    console.error('确认收款失败:', error)
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

</script>

<style scoped>
.finance-management {
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
