<template>
  <div class="auction-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>拍卖详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-alert
        v-if="auction.status === 'in_progress'"
        title="拍卖进行中"
        type="success"
        :closable="false"
        show-icon
      >
        <template #default>
          <el-statistic :value="remainingSeconds" :formatter="formatRemainingSeconds" class="timer">
            <template #title>
              <div style="font-size: 14px; color: #666">剩余时间</div>
            </template>
          </el-statistic>
        </template>
      </el-alert>

      <el-descriptions :column="2" border style="margin-top: 20px">
        <el-descriptions-item label="拍卖名称">{{ auction.name }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(auction.status)">{{ getStatusText(auction.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ auction.assetName }}</el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ auction.assetCode }}</el-descriptions-item>
        <el-descriptions-item label="起拍价">¥{{ auction.startPrice?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="当前最高价">
          <span class="current-price">¥{{ auction.currentPrice?.toLocaleString() }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="加价幅度">¥{{ auction.incrementAmount }}</el-descriptions-item>
        <el-descriptions-item v-if="canViewReserve" label="保留价">
          {{ auction.hasReservePrice ? '已设置' : '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ auction.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ auction.endTime }}</el-descriptions-item>
        <el-descriptions-item label="出价次数" :span="2">{{ auction.bidCount }} 次</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">资产图片</el-divider>
      <div class="image-list">
        <el-image
          v-for="(image, index) in auction.assetImages"
          :key="index"
          :src="image"
          :preview-src-list="auction.assetImages"
          :initial-index="index"
          fit="cover"
          class="asset-image"
        />
      </div>

      <el-divider content-position="left">拍卖说明</el-divider>
      <div class="description">{{ auction.description }}</div>

      <template v-if="auction.status === 'in_progress'">
        <el-divider content-position="left">参与竞拍</el-divider>

        <el-card class="bid-card">
          <el-row :gutter="20" align="middle">
            <el-col :span="12">
              <div class="bid-info">
                <div class="bid-label">当前最高价</div>
                <div class="bid-price">¥{{ auction.currentPrice?.toLocaleString() }}</div>
              </div>
            </el-col>
            <el-col :span="12" v-if="canBid">
              <el-button type="primary" size="large" @click="handleQuickBid">
                一键出价(¥{{ formatPrice(quickBidPrice) }})
              </el-button>
              <el-button type="success" size="large" @click="dialogVisible = true">自定义出价</el-button>
              <el-button v-if="canWithdraw" type="danger" size="large" @click="handleWithdrawBid">撤价</el-button>

              <div class="bid-tip">
                <el-tag v-if="isLeading" type="success" size="small">已领先</el-tag>
                <span v-if="isLeading">您已领先，可继续出价以拉开差距</span>
                <span v-else>当前未领先，可继续出价提升排名</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </template>

      <el-divider content-position="left">出价记录</el-divider>
      <el-table :data="auction.bids" stripe>
        <el-table-column prop="bidderName" label="竞拍者" width="140" />
        <el-table-column prop="price" label="出价(元)" align="right">
          <template #default="{ row }">¥{{ row.price?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="bidTime" label="出价时间" width="200" />
        <el-table-column prop="isHighest" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isHighest" type="success" size="small">当前最高</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="自定义出价" width="420px">
      <el-form label-width="100px">
        <el-form-item label="最低出价">
          <el-input :value="`¥${formatPrice(minBidPrice)}`" disabled />
        </el-form-item>
        <el-form-item label="出价金额">
          <el-input-number
            v-model="bidForm.price"
            :min="minBidPrice"
            :step="0.01"
            :precision="2"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitBid">确认出价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { bid, getAssetImages, getAuctionBids, getAuctionDetail, quickBid, withdrawBid } from '@/api'
import { validatePositiveMoney, MONEY_VALIDATION_MESSAGE } from '@/utils/amountValidation'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const auction = reactive({
  id: null,
  name: '',
  status: '',
  assetId: null,
  assetName: '',
  assetCode: '',
  startPrice: 0,
  currentPrice: 0,
  incrementAmount: 0,
  hasReservePrice: false,
  startTime: '',
  endTime: '',
  bidCount: 0,
  bids: [],
  assetImages: [],
  description: ''
})

const dialogVisible = ref(false)
const bidForm = reactive({ price: null })

const timer = ref(null)
const remainingSeconds = ref(0)

const isInIrrevocableWindow = computed(() => remainingSeconds.value <= 12 * 60 * 60)

const canBid = computed(() => {
  return (
    auction.status === 'in_progress' &&
    !userStore.hasAnyRole('资产专员', '财务专员', '系统管理员')
  )
})

const isLeading = computed(() => {
  const userId = userStore.user?.id
  if (!userId || !Array.isArray(auction.bids)) return false
  return auction.bids.some((b) => b?.isHighest && b?.bidderId === userId)
})

const canWithdraw = computed(() => canBid.value && isLeading.value && remainingSeconds.value > 12 * 60 * 60)

const canViewReserve = computed(() => userStore.hasAnyRole('资产专员', '财务专员', '系统管理员'))

const formatPrice = (value) => {
  const num = Number(value)
  if (Number.isNaN(num)) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatRemainingSeconds = (value) => {
  const total = Math.max(0, Number(value) || 0)
  const hours = Math.floor(total / 3600)
  const minutes = Math.floor((total % 3600) / 60)
  const seconds = Math.floor(total % 60)
  const hh = String(hours).padStart(2, '0')
  const mm = String(minutes).padStart(2, '0')
  const ss = String(seconds).padStart(2, '0')
  return `${hh}:${mm}:${ss}`
}

const quickBidPrice = computed(() => {
  const current = Number(auction.currentPrice) || 0
  const inc = Number(auction.incrementAmount) || 0
  const result = current + inc
  return Math.round(result * 100) / 100
})

const minBidPrice = computed(() => {
  const total = (Number(auction.currentPrice) || 0) + (Number(auction.incrementAmount) || 0)
  return Math.round(total * 100) / 100
})

const buildBidConfirmText = (price) => {
  const base = `确定要以 ¥${formatPrice(price)} 的价格出价吗？`
  return isInIrrevocableWindow.value ? `${base}\n\n一旦出价不可撤销。` : base
}

const getStatusType = (status) => {
  const map = { not_started: 'info', in_progress: 'success', ended: 'warning' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { not_started: '未开始', in_progress: '进行中', ended: '已结束' }
  return map[status] || status
}

const stopTimer = () => {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
}

const normalizeImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images.filter(Boolean)
  if (typeof images === 'string') return images.split(',').map((i) => i.trim()).filter(Boolean)
  return []
}

const normalizeBids = (bids = []) =>
  (bids || []).map((b) => ({ ...b, bidderName: b.bidderName || b.bidder?.name || b.bidderId || '-' }))

const updateRemainingTime = () => {
  if (!auction.endTime) {
    remainingSeconds.value = 0
    return
  }
  const now = Date.now()
  const end = new Date(auction.endTime).getTime()
  if (Number.isNaN(end)) {
    remainingSeconds.value = 0
    return
  }
  remainingSeconds.value = Math.max(0, Math.floor((end - now) / 1000))
  if (remainingSeconds.value <= 0) {
    stopTimer()
    loadAuctionDetail()
  }
}

const startTimer = () => {
  if (timer.value) return
  updateRemainingTime()
  let refreshTick = 0
  timer.value = setInterval(() => {
    updateRemainingTime()
    refreshTick += 1
    if (refreshTick % 10 === 0) loadAuctionDetail()
  }, 1000)
}

const loadAuctionDetail = async () => {
  loading.value = true
  try {
    const auctionId = route.params.id
    const [detailRes, bidsRes] = await Promise.all([getAuctionDetail(auctionId), getAuctionBids(auctionId)])

    Object.assign(auction, detailRes.data)
    auction.bids = normalizeBids(bidsRes?.data || [])

    const imagesFromDetail = normalizeImages(auction.assetImages)
    if (imagesFromDetail.length > 0) {
      auction.assetImages = imagesFromDetail
    } else if (auction.assetId) {
      const imagesRes = await getAssetImages(auction.assetId)
      auction.assetImages = normalizeImages(imagesRes?.data)
    } else {
      auction.assetImages = []
    }

    if (auction.status === 'in_progress') startTimer()
    else stopTimer()
  } catch (e) {
    console.error('加载拍卖详情失败:', e)
    ElMessage.error('加载拍卖详情失败')
  } finally {
    loading.value = false
  }
}

const handleWithdrawBid = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要撤销当前最高出价 ¥${formatPrice(auction.currentPrice)} 吗？撤销后将由次高出价者顺位领先。`,
      '确认撤价',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await withdrawBid(route.params.id)
    ElMessage.success('撤价成功')
    loadAuctionDetail()
  } catch (e) {
    if (e !== 'cancel') console.error('撤价失败:', e)
  }
}

const handleQuickBid = async () => {
  if (!validatePositiveMoney(quickBidPrice.value)) {
    ElMessage.error(`出价金额${MONEY_VALIDATION_MESSAGE}`)
    return
  }
  try {
    await ElMessageBox.confirm(buildBidConfirmText(quickBidPrice.value), '确认出价', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await quickBid(route.params.id)
    ElMessage.success('出价成功')
    loadAuctionDetail()
  } catch (e) {
    if (e !== 'cancel') console.error('出价失败:', e)
  }
}

const handleSubmitBid = async () => {
  if (!validatePositiveMoney(bidForm.price)) {
    ElMessage.error(`出价金额${MONEY_VALIDATION_MESSAGE}`)
    return
  }
  if (Number(bidForm.price) < Number(minBidPrice.value)) {
    ElMessage.error(`出价金额不能低于 ¥${formatPrice(minBidPrice.value)}`)
    return
  }
  try {
    await ElMessageBox.confirm(buildBidConfirmText(bidForm.price), '确认出价', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await bid({ auctionId: route.params.id, price: bidForm.price })
    ElMessage.success('出价成功')
    dialogVisible.value = false
    loadAuctionDetail()
  } catch (e) {
    if (e !== 'cancel') console.error('出价失败:', e)
  }
}

const goBack = () => router.back()

onMounted(loadAuctionDetail)
onUnmounted(stopTimer)
</script>

<style scoped>
.auction-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timer {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.current-price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.asset-image {
  width: 100%;
  height: 150px;
  border-radius: 4px;
  cursor: pointer;
}

.description {
  line-height: 1.8;
  color: #666;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.bid-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.bid-info {
  text-align: center;
}

.bid-tip {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 13px;
}

.bid-label {
  color: #fff;
  font-size: 14px;
  margin-bottom: 10px;
}

.bid-price {
  color: #fff;
  font-size: 36px;
  font-weight: bold;
}
</style>

