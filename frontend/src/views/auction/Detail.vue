<template>
  <div class="auction-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>拍卖详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <!-- 拍卖状态标签 -->
      <el-alert
        v-if="auction.status === 'in_progress'"
        title="拍卖进行中"
        type="success"
        :closable="false"
        show-icon
      >
        <template #default>
          <el-statistic
            :value="remainingSeconds"
            format="DD天HH小时mm分ss秒"
            class="timer"
          >
            <template #title>
              <div style="font-size: 14px; color: #666">剩余时间</div>
            </template>
          </el-statistic>
        </template>
      </el-alert>

      <el-descriptions :column="2" border style="margin-top: 20px">
        <el-descriptions-item label="拍卖名称">{{ auction.name }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(auction.status)">
            {{ getStatusText(auction.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ auction.assetName }}</el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ auction.assetCode }}</el-descriptions-item>
        <el-descriptions-item label="起拍价">¥{{ auction.startPrice?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="当前最高价">
          <span style="font-size: 24px; color: #f56c6c; font-weight: bold">
            ¥{{ auction.currentPrice?.toLocaleString() }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="加价幅度">¥{{ auction.incrementAmount }}</el-descriptions-item>
        <el-descriptions-item v-if="canViewReserve" label="保留价">
          {{ auction.hasReservePrice ? '已设置' : '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ auction.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ auction.endTime }}</el-descriptions-item>
        <el-descriptions-item label="竞价次数" :span="2">
          {{ auction.bidCount }} 次
        </el-descriptions-item>
      </el-descriptions>

      <!-- 资产图片 -->
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

      <!-- 拍卖说明 -->
      <el-divider content-position="left">拍卖说明</el-divider>
      <div class="description">
        {{ auction.description }}
      </div>

      <!-- 竞价区域 -->
      <template v-if="auction.status === 'in_progress'">
        <el-divider content-position="left">参与竞价</el-divider>

        <el-card class="bid-card">
          <el-row :gutter="20" align="middle">
            <el-col :span="12">
              <div class="bid-info">
                <div class="bid-label">当前最高价</div>
                <div class="bid-price">¥{{ auction.currentPrice?.toLocaleString() }}</div>
              </div>
            </el-col>
            <el-col :span="12" v-if="canBid">
              <el-button
                type="primary"
                size="large"
                @click="handleQuickBid"
              >
                一键出价 (¥{{ formatPrice(quickBidPrice) }})
              </el-button>
              <el-button
                type="success"
                size="large"
                @click="dialogVisible = true"
              >
                自定义出价
              </el-button>
              <div class="bid-tip">
                <el-tag v-if="isLeading" type="success" size="small">已领先</el-tag>
                <span v-if="isLeading">您已领先，可继续出价以拉开差距</span>
                <span v-else>当前未领先，可继续出价提升排名</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </template>

      <!-- 竞价记录 -->
      <el-divider content-position="left">竞价记录</el-divider>
      <el-table :data="auction.bids" stripe>
        <el-table-column prop="bidderName" label="竞拍者" width="120" />
        <el-table-column prop="price" label="出价(元)" align="right">
          <template #default="{ row }">¥{{ row.price?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="bidTime" label="出价时间" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isHighest" type="success" size="small">当前最高</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 成交信息 -->
      <template v-if="auction.status === 'ended' && auction.winner">
        <el-divider content-position="left">成交信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="中标者">{{ auction.winner.name }}</el-descriptions-item>
          <el-descriptions-item label="成交价">¥{{ auction.finalPrice?.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ auction.winner.phone }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ auction.winner.department }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-card>

    <!-- 自定义出价对话框 -->
    <el-dialog v-model="dialogVisible" title="自定义出价" width="400px">
      <el-form :model="bidForm" label-width="100px">
        <el-form-item label="当前最高价">
          <el-input :value="`¥${auction.currentPrice?.toLocaleString()}`" disabled />
        </el-form-item>
        <el-form-item label="最低出价">
          <el-input :value="`¥${formatPrice(minBidPrice)}`" disabled />
        </el-form-item>
        <el-form-item label="出价金额">
          <el-input-number
            v-model="bidForm.price"
            :min="minBidPrice"
            :step="auction.incrementAmount"
            :precision="2"
            controls-position="right"
            style="width: 100%"
          />
          <span style="margin-left: 10px">元</span>
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
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getAuctionDetail, getAuctionBids, getAssetImages, bid, quickBid } from '@/api'
import { validatePositiveMoney, MONEY_VALIDATION_MESSAGE } from '@/utils/amountValidation'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const auction = reactive({
  name: '',
  status: '',
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
  description: '',
  winner: null,
  finalPrice: 0
})

const dialogVisible = ref(false)
const bidForm = reactive({
  price: null
})

const timer = ref(null)
const remainingSeconds = ref(0)

const canBid = computed(() =>
  auction.status === 'in_progress' && !userStore.hasAnyRole('资产专员', '财务专员', '系统管理员')
)

const isLeading = computed(() => {
  const userId = userStore.user?.id
  if (!userId || !Array.isArray(auction.bids)) return false
  return auction.bids.some(bid => bid?.isHighest && bid?.bidderId === userId)
})

const canViewReserve = computed(() =>
  userStore.hasAnyRole('资产专员', '财务专员', '系统管理员')
)

const formatPrice = (value) => {
  const num = Number(value)
  if (Number.isNaN(num)) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const quickBidPrice = computed(() => {
  const currentPrice = Number(auction.currentPrice) || 0;
  const incrementAmount = Number(auction.incrementAmount) || 0;

  // 一键出价按“当前价 + 加价幅度”计算，不展示保留价
  // 添加防御性检查，避免NaN和Infinity
  const baseBid = isNaN(currentPrice) || isNaN(incrementAmount) ? 0 : currentPrice + incrementAmount;

  // 确保结果是有效的数字
  const result = isNaN(baseBid) || !isFinite(baseBid) ? 0 : baseBid;
  return Math.round(result * 100) / 100;
})

const minBidPrice = computed(() => {
  const total = (Number(auction.currentPrice) || 0) + (Number(auction.incrementAmount) || 0)
  return Math.round(total * 100) / 100
})

const getStatusType = (status) => {
  const statusMap = {
    'not_started': 'info',
    'in_progress': 'success',
    'ended': 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'not_started': '未开始',
    'in_progress': '进行中',
    'ended': '已结束'
  }
  return statusMap[status] || status
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
  if (typeof images === 'string') {
    return images.split(',').map(i => i.trim()).filter(Boolean)
  }
  return []
}

const normalizeBids = (bids = []) => (bids || []).map(bid => ({
  ...bid,
  bidderName: bid.bidderName || bid.bidder?.name || bid.bidderId || '-'
}))

const loadAuctionDetail = async () => {
  loading.value = true
  try {
    // 验证ID参数是否为有效数字
    const auctionId = route.params.id
    if (!auctionId || isNaN(Number(auctionId))) {
      console.warn('无效的拍卖ID:', auctionId)
      stopTimer()
      // 只有当用户真的在“拍卖详情页”时才提示；避免切换其它功能时被定时器误触发
      if (route.name === 'AuctionDetail') {
        ElMessage.warning('无效的拍卖信息')
        router.replace({ name: 'AuctionManagement' })
      }
      return
    }

    const [detailRes, bidsRes] = await Promise.all([
      getAuctionDetail(auctionId),
      getAuctionBids(auctionId)
    ])

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

    // 如果拍卖正在进行，启动倒计时；否则停止定时器
    if (auction.status === 'in_progress') {
      startTimer()
    } else {
      stopTimer()
    }
  } catch (error) {
    console.error('加载拍卖详情失败:', error)
    ElMessage.error('加载拍卖详情失败')
  } finally {
    loading.value = false
  }
}

const startTimer = () => {
  // 防止重复启动导致多个 setInterval 叠加（会在切换页面后继续触发请求/弹窗）
  if (timer.value) return

  updateRemainingTime()
  let refreshTick = 0

  timer.value = setInterval(() => {
    updateRemainingTime()

    refreshTick += 1
    // 每 10 秒刷新一次拍卖信息（1 秒一跳更符合倒计时体验）
    if (refreshTick % 10 === 0 && route.name === 'AuctionDetail') {
      loadAuctionDetail()
    }
  }, 1000)
}

const updateRemainingTime = () => {
  if (!auction.endTime) {
    remainingSeconds.value = 0
    return
  }

  const now = new Date().getTime()
  const endTime = new Date(auction.endTime).getTime()
  if (Number.isNaN(endTime)) {
    remainingSeconds.value = 0
    return
  }

  remainingSeconds.value = Math.max(0, Math.floor((endTime - now) / 1000))

  // 倒计时结束
  if (remainingSeconds.value <= 0) {
    stopTimer()
    if (route.name === 'AuctionDetail') {
      loadAuctionDetail()
    }
  }
}

const handleQuickBid = async () => {
  if (!validatePositiveMoney(quickBidPrice.value)) {
    ElMessage.error(`出价金额${MONEY_VALIDATION_MESSAGE}`)
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要以 ¥${quickBidPrice.value} 的价格出价吗？`,
      '确认出价',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await quickBid(route.params.id)
    ElMessage.success('出价成功')
    loadAuctionDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('出价失败:', error)
    }
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
    await ElMessageBox.confirm(
      `确定要以 ¥${bidForm.price} 的价格出价吗？`,
      '确认出价',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await submitBid(bidForm.price)
    dialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('出价失败:', error)
    }
  }
}

const submitBid = async (price) => {
  try {
    if (!validatePositiveMoney(price)) {
      ElMessage.error(`出价金额${MONEY_VALIDATION_MESSAGE}`)
      return
    }
    await bid({
      auctionId: route.params.id,
      price
    })
    ElMessage.success('出价成功')
    loadAuctionDetail()
  } catch (error) {
    console.error('出价失败:', error)
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadAuctionDetail()
})

onUnmounted(() => {
  stopTimer()
})
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
