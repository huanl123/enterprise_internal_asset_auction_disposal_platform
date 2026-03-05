<template>
  <div class="asset-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>资产详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="资产编号">{{ asset.code }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ asset.name }}</el-descriptions-item>
        <el-descriptions-item label="规格描述" :span="2">{{ asset.specification }}</el-descriptions-item>
        <el-descriptions-item label="使用部门">{{ asset.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="购置日期">{{ asset.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="资产原值">¥{{ asset.originalValue?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="当前价值">¥{{ asset.currentValue?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="起拍价">¥{{ asset.startPrice?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="保留价">
          {{ asset.hasReservePrice ? `¥${ asset.reservePrice?.toLocaleString() }` : '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="折旧规则">{{ asset.depreciationRuleName }}</el-descriptions-item>
        <el-descriptions-item label="资产状态">
          <el-tag :type="getStatusType(asset.status)">{{ asset.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ asset.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">资产图片</el-divider>
      <div class="image-list" v-if="asset.images && asset.images.length > 0">
        <el-image
          v-for="(image, index) in asset.images"
          :key="index"
          :src="image"
          :preview-src-list="asset.images"
          :initial-index="index"
          fit="cover"
          class="asset-image"
        />
      </div>
      <div v-else class="no-images">
        暂无图片
      </div>

      <el-divider content-position="left" v-if="asset.status === '拍卖中'">竞价记录</el-divider>
      <div v-if="asset.status === '拍卖中' && auctionInfo">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-statistic title="当前最高价" :value="auctionInfo.currentPrice" prefix="¥" />
          </el-col>
          <el-col :span="12">
            <el-statistic title="剩余时间" :value="remainingTime">
              <template #suffix>秒</template>
            </el-statistic>
          </el-col>
        </el-row>
        <el-divider />
        <el-table :data="auctionInfo.bids" stripe>
          <el-table-column prop="bidderName" label="竞拍者" />
          <el-table-column prop="price" label="出价(元)" align="right">
            <template #default="{ row }">¥{{ row.price?.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column prop="bidTime" label="出价时间" />
        </el-table>
      </div>

      <el-divider content-position="left" v-if="asset.status === '待拍卖' && userStore.hasRole('资产专员')">操作</el-divider>
      <div v-if="asset.status === '待拍卖' && userStore.hasRole('资产专员')">
        <el-button type="primary" @click="handleCreateAuction">创建拍卖活动</el-button>
      </div>

      <el-divider content-position="left">流转记录</el-divider>
      <el-timeline>
        <el-timeline-item
          v-for="record in asset.history"
          :key="record.id"
          :timestamp="record.createTime"
          :type="getRecordType(record.type)"
        >
          <div class="record-item">
            <div class="record-title">{{ record.title }}</div>
            <div class="record-content">{{ record.content }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAssetDetail, getAuctionDetail } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const asset = reactive({
  code: '',
  name: '',
  specification: '',
  departmentName: '',
  purchaseDate: '',
  originalValue: 0,
  currentValue: 0,
  startPrice: 0,
  reservePrice: 0,
  hasReservePrice: false,
  depreciationRuleName: '',
  status: '',
  createTime: '',
  images: [],
  history: []
})

const auctionInfo = ref(null)
const remainingTime = ref(0)

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

const getRecordType = (type) => {
  const typeMap = {
    '创建': 'primary',
    '审核': 'success',
    '拍卖': 'warning',
    '成交': 'success',
    '处置': 'info'
  }
  return typeMap[type] || 'primary'
}

const normalizeImages = (images) => {
  if (Array.isArray(images)) return images.filter(Boolean)
  if (typeof images === 'string') {
    return images
      .split(',')
      .map(item => item.trim())
      .filter(Boolean)
  }
  return []
}

const loadAssetDetail = async () => {
  loading.value = true
  try {
    const res = await getAssetDetail(route.params.id)
    Object.assign(asset, res.data)
    asset.images = normalizeImages(res.data?.images)

    // 如果资产正在拍卖中，加载拍卖信息
    if (asset.status === '拍卖中' && asset.auctionId) {
      loadAuctionDetail(asset.auctionId)
    }
  } catch (error) {
    console.error('加载资产详情失败:', error)
  } finally {
    loading.value = false
  }
}

const loadAuctionDetail = async (auctionId) => {
  try {
    const res = await getAuctionDetail(auctionId)
    auctionInfo.value = res.data
  } catch (error) {
    console.error('加载拍卖信息失败:', error)
  }
}

const handleCreateAuction = () => {
  router.push({
    path: '/auction/create',
    query: { assetId: route.params.id }
  })
}

const goBack = () => {
  router.back()
}

// 新增：判断是否可以审核
const canApprove = computed(() => {
  return (userStore.hasRole('asset_specialist') || 
          userStore.hasRole('finance_specialist') || 
          userStore.hasRole('admin')) &&
         asset.status === '待审核';
});

onMounted(() => {
  loadAssetDetail()
})
</script>

<style scoped>
.asset-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.record-item {
  padding: 5px 0;
}

.record-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.record-content {
  font-size: 13px;
  color: #666;
}
</style>
