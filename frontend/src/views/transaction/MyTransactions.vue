<template>
  <div class="my-transactions">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的交易单</span>
          <el-tag v-if="filterLabel" size="small" effect="plain">{{ filterLabel }}</el-tag>
        </div>
      </template>

      <el-table v-loading="loading" :data="transactionList" stripe border>
        <el-table-column prop="code" label="交易单号" width="160" />
        <el-table-column prop="assetName" label="资产名称" min-width="180" />
        <el-table-column prop="finalPrice" label="成交价(元)" width="120" align="right">
          <template #default="{ row }">¥{{ formatAmount(row.finalPrice) }}</template>
        </el-table-column>
        <el-table-column prop="confirmStatus" label="确认状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getConfirmTag(row.confirmStatus)">{{ getConfirmText(row.confirmStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="付款状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getPaymentTag(row)">{{ getPaymentText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentDeadline" label="付款截止时间" width="180">
          <template #default="{ row }">{{ formatTime(row.paymentDeadline) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="canSubmitPayment(row)"
              type="primary"
              size="small"
              @click="openPayDialog(row)"
            >
              提交付款
            </el-button>
            <el-tag v-else-if="hasPaymentVoucher(row)" size="small" type="info">已提交</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && transactionList.length === 0" description="暂无交易单" />
    </el-card>

    <el-dialog v-model="payDialogVisible" title="提交付款凭证" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="交易单号">{{ currentTransaction.code }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ currentTransaction.assetName }}</el-descriptions-item>
        <el-descriptions-item label="成交价">¥{{ formatAmount(currentTransaction.finalPrice) }}</el-descriptions-item>
        <el-descriptions-item label="付款截止时间">{{ formatTime(currentTransaction.paymentDeadline) }}</el-descriptions-item>
      </el-descriptions>

      <el-form :model="payForm" label-width="120px" style="margin-top: 20px">
        <el-form-item label="付款凭证" required>
          <el-upload
            v-model:file-list="payFileList"
            list-type="picture-card"
            :limit="1"
            :auto-upload="false"
            :before-upload="beforePayUpload"
            :on-change="handlePayChange"
            :on-preview="handlePreview"
            :on-remove="handlePayRemove"
          >
            <el-button type="primary">选择付款凭证</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="payForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可填写付款方式或交易说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleSubmitPay">提交付款</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialogVisible" title="预览" width="600px">
      <el-image :src="previewUrl" style="width: 100%" fit="contain" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getMyTransactions, submitMyPaymentVoucher } from '@/api'

const route = useRoute()
const loading = ref(false)
const transactionList = ref([])
const payDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const previewUrl = ref('')
const currentTransaction = ref({})
const payFileList = ref([])
const payVoucherFile = ref(null)
const payForm = ref({
  remark: ''
})

const queryForm = ref({
  confirmStatus: '',
  paymentStatus: ''
})

const filterLabel = computed(() => {
  const parts = []
  if (queryForm.value.confirmStatus) {
    parts.push(`确认状态：${getConfirmText(queryForm.value.confirmStatus)}`)
  }
  if (queryForm.value.paymentStatus) {
    parts.push(`付款状态：${getPaymentTextByStatus(queryForm.value.paymentStatus)}`)
  }
  return parts.join('，')
})

const syncQueryFromRoute = () => {
  queryForm.value.confirmStatus = String(route.query.confirmStatus || '').trim().toLowerCase()
  queryForm.value.paymentStatus = String(route.query.paymentStatus || '').trim().toLowerCase()
}

const loadTransactions = async () => {
  loading.value = true
  try {
    const params = {}
    if (queryForm.value.confirmStatus) params.confirmStatus = queryForm.value.confirmStatus
    if (queryForm.value.paymentStatus) params.paymentStatus = queryForm.value.paymentStatus
    const res = await getMyTransactions(params)
    transactionList.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('加载我的交易单失败:', error)
    transactionList.value = []
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const formatAmount = (value) => {
  const number = Number(value)
  if (Number.isNaN(number)) return '-'
  return number.toLocaleString()
}

const getConfirmText = (status) => {
  const value = String(status || '').trim().toLowerCase()
  if (value === 'pending') return '待确认'
  if (value === 'confirmed') return '已确认'
  if (value === 'expired') return '已过期'
  return status || '-'
}

const getConfirmTag = (status) => {
  const value = String(status || '').trim().toLowerCase()
  if (value === 'pending') return 'warning'
  if (value === 'confirmed') return 'success'
  if (value === 'expired') return 'danger'
  return 'info'
}

const hasPaymentVoucher = (row) => Boolean(row?.paymentVoucher)

const getPaymentTextByStatus = (status) => {
  const value = String(status || '').trim().toLowerCase()
  if (value === 'pending') return '待付款'
  if (value === 'approved') return '已收款'
  if (value === 'rejected') return '未通过'
  return status || '-'
}

const getPaymentText = (row) => {
  const value = String(row?.paymentStatus || '').trim().toLowerCase()
  if (value === 'pending') {
    return hasPaymentVoucher(row) ? '已付款待确认' : '待付款'
  }
  if (value === 'approved') return '已收款'
  if (value === 'rejected') return '未通过'
  return row?.paymentStatus || '-'
}

const getPaymentTag = (row) => {
  const value = String(row?.paymentStatus || '').trim().toLowerCase()
  if (value === 'pending') return hasPaymentVoucher(row) ? 'info' : 'warning'
  if (value === 'approved') return 'success'
  if (value === 'rejected') return 'danger'
  return 'info'
}

const canSubmitPayment = (row) => {
  const confirm = String(row?.confirmStatus || '').trim().toLowerCase()
  const payment = String(row?.paymentStatus || '').trim().toLowerCase()
  return confirm === 'confirmed' && payment === 'pending' && !hasPaymentVoucher(row)
}

const openPayDialog = (row) => {
  currentTransaction.value = row || {}
  payForm.value.remark = ''
  payFileList.value = []
  payVoucherFile.value = null
  payDialogVisible.value = true
}

const beforePayUpload = (file) => {
  payVoucherFile.value = file
  return false
}

const handlePayChange = (file, fileList) => {
  payFileList.value = fileList || []
  payVoucherFile.value = file?.raw || payFileList.value?.[0]?.raw || file || null
}

const handlePayRemove = () => {
  payVoucherFile.value = null
  payFileList.value = []
}

const handlePreview = (file) => {
  previewUrl.value = file?.url || file?.response?.data?.url || ''
  previewDialogVisible.value = true
}

const handleSubmitPay = async () => {
  const voucher = payVoucherFile.value || payFileList.value?.[0]?.raw || null
  if (!voucher) {
    ElMessage.warning('请先选择付款凭证')
    return
  }
  try {
    await submitMyPaymentVoucher(currentTransaction.value.id, {
      voucher,
      remark: payForm.value.remark
    })
    ElMessage.success('付款凭证提交成功')
    payDialogVisible.value = false
    await loadTransactions()
  } catch (error) {
    console.error('提交付款失败:', error)
  }
}

watch(
  () => route.query,
  () => {
    syncQueryFromRoute()
    loadTransactions()
  },
  { immediate: true }
)
</script>

<style scoped>
.my-transactions {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
