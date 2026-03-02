<template>
  <div class="disposal-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>处置详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="交易单号">{{ disposal.transactionCode }}</el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ disposal.assetCode }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ disposal.assetName }}</el-descriptions-item>
        <el-descriptions-item label="中标者">{{ disposal.winnerName }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ disposal.winnerPhone }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ disposal.winnerDepartment }}</el-descriptions-item>
        <el-descriptions-item label="成交价">¥{{ disposal.finalPrice?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="中标时间">{{ disposal.winTime }}</el-descriptions-item>
        <el-descriptions-item label="确认成交时间">{{ disposal.confirmTime }}</el-descriptions-item>
        <el-descriptions-item label="付款确认时间">{{ disposal.paymentTime }}</el-descriptions-item>
        <el-descriptions-item label="处置完成时间">{{ disposal.disposalTime }}</el-descriptions-item>
        <el-descriptions-item label="处置状态">
          <el-tag :type="getStatusType(disposal.status)">
            {{ disposal.status }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">处置记录</el-divider>
      <el-timeline>
        <el-timeline-item
          v-for="record in disposal.records"
          :key="record.id"
          :timestamp="record.createTime"
          :type="getRecordType(record.action)"
        >
          <div class="record-item">
            <div class="record-title">{{ record.action }}</div>
            <div class="record-operator">操作人：{{ record.operator }}</div>
            <div class="record-remark">{{ record.remark }}</div>
            <div v-if="record.voucher" class="record-voucher">
              <el-image
                v-for="(img, index) in record.voucherImages"
                :key="index"
                :src="img"
                fit="cover"
                :preview-src-list="record.voucherImages"
                :initial-index="index"
                class="voucher-image"
              />
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getDisposals } from '@/api'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const disposal = reactive({
  transactionCode: '',
  assetCode: '',
  assetName: '',
  winnerName: '',
  winnerPhone: '',
  winnerDepartment: '',
  finalPrice: 0,
  winTime: '',
  confirmTime: '',
  paymentTime: '',
  disposalTime: '',
  status: '',
  records: []
})

const getStatusType = (status) => {
  const statusMap = {
    '待处置': 'warning',
    '处置中': 'primary',
    '已处置': 'success'
  }
  return statusMap[status] || 'info'
}

const getRecordType = (action) => {
  const typeMap = {
    '确认成交': 'success',
    '确认收款': 'success',
    '确认处置': 'success',
    '拒绝付款': 'danger',
    '取消交易': 'warning'
  }
  return typeMap[action] || 'primary'
}

const loadDisposalDetail = async () => {
  loading.value = true
  try {
    const res = await getDisposals({ id: route.params.id })
    const detail = res?.data || {}
    const asset = detail.asset || {}
    const transaction = detail.transaction || {}
    const history = Array.isArray(detail.history) ? detail.history : []

    disposal.transactionCode = transaction.code || ''
    disposal.assetCode = asset.code || ''
    disposal.assetName = asset.name || ''
    disposal.winnerName = detail.winnerName || transaction.winnerName || ''
    disposal.winnerPhone = detail.winnerPhone || transaction.winnerPhone || ''
    disposal.winnerDepartment = detail.winnerDepartment || asset.departmentName || ''
    const winRecord = history.find(item => item?.operation === '拍卖结果' && (item?.content || '').includes('成交'))

    disposal.finalPrice = transaction.finalPrice || 0
    disposal.winTime = detail.winTime || transaction.winTime || winRecord?.createTime || ''
    disposal.confirmTime = transaction.confirmTime || ''
    disposal.paymentTime = transaction.paymentTime || ''
    disposal.disposalTime = transaction.disposalTime || ''
    disposal.status = asset.status || ''

    const voucherImages = (transaction.disposalVoucher || '')
      .split(',')
      .map(item => item.trim())
      .filter(Boolean)

    disposal.records = history.map(item => {
      const record = {
        id: item.id,
        action: item.operation,
        createTime: item.createTime,
        operator: item.operatorName || item.operator || (item.operatorId != null ? String(item.operatorId) : '-'),
        remark: item.content || '',
        voucher: null,
        voucherImages: []
      }
      if (item.operation === '确认处置' && voucherImages.length) {
        record.voucher = transaction.disposalVoucher
        record.voucherImages = voucherImages
      }
      return record
    })
  } catch (error) {
    console.error('加载处置详情失败:', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadDisposalDetail()
})
</script>

<style scoped>
.disposal-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-item {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.record-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.record-operator {
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
}

.record-remark {
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
}

.record-voucher {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
  margin-top: 10px;
}

.voucher-image {
  width: 100%;
  height: 100px;
  border-radius: 4px;
  cursor: pointer;
}
</style>
