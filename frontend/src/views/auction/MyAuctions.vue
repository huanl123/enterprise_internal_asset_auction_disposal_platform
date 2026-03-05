<template>
  <div class="my-auctions">
    <el-card>
      <template #header>
        <span>我的竞拍</span>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="进行中" name="in_progress">
          <el-table
            v-loading="loading"
            :data="auctionList"
            stripe
            border
          >
            <el-table-column prop="assetName" label="资产名称" width="200" />
            <el-table-column prop="assetCode" label="资产编号" width="150" />
            <el-table-column prop="currentPrice" label="当前价格(元)" width="130" align="right">
              <template #default="{ row }">¥{{ row.currentPrice?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="myBid" label="我的出价(元)" width="130" align="right">
              <template #default="{ row }">¥{{ row.myBid?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="isHighest" label="当前状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isHighest" type="success">领先</el-tag>
                <el-tag v-else type="info">落后</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="180" />
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="canContinueBid(row)"
                  text
                  type="primary"
                  size="small"
                  @click="handleBid(row)"
                >
                  继续出价
                </el-button>
                <el-tag v-else type="info" size="small">已结束</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已结束" name="ended">
          <el-table
            v-loading="loading"
            :data="auctionList"
            stripe
            border
          >
            <el-table-column prop="assetName" label="资产名称" width="200" />
            <el-table-column prop="assetCode" label="资产编号" width="150" />
            <el-table-column prop="finalPrice" label="最终成交价(元)" width="150" align="right">
              <template #default="{ row }">¥{{ row.finalPrice?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="myMaxBid" label="我的最高出价(元)" width="150" align="right">
              <template #default="{ row }">¥{{ row.myMaxBid?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="result" label="结果" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.result === 'won'" type="success">中标</el-tag>
                <el-tag v-else-if="row.result === 'lost'" type="info">未中标</el-tag>
                <el-tag v-else type="warning">已结束</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="180" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已中标" name="won">
          <el-table
            v-loading="loading"
            :data="auctionList"
            stripe
            border
          >
            <el-table-column prop="assetName" label="资产名称" width="200" />
            <el-table-column prop="assetCode" label="资产编号" width="150" />
            <el-table-column prop="finalPrice" label="成交价(元)" width="130" align="right">
              <template #default="{ row }">¥{{ row.finalPrice?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="winTime" label="中标时间" width="180" />
            <el-table-column prop="confirmStatus" label="确认状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.confirmStatus === 'pending'" type="warning">待确认</el-tag>
                <el-tag v-else-if="row.confirmStatus === 'confirmed'" type="success">已确认</el-tag>
                <el-tag v-else-if="row.confirmStatus === 'expired'" type="danger">已过期</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.confirmStatus === 'pending'"
                  text
                  type="success"
                  size="small"
                  @click="handleConfirm(row)"
                >
                  确认成交
                </el-button>
                <el-button
                  v-if="row.confirmStatus === 'confirmed'"
                  text
                  type="primary"
                  size="small"
                  @click="handleViewDetail(row)"
                >
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="未中标" name="lost">
          <el-table
            v-loading="loading"
            :data="auctionList"
            stripe
            border
          >
            <el-table-column prop="assetName" label="资产名称" width="200" />
            <el-table-column prop="assetCode" label="资产编号" width="150" />
            <el-table-column prop="finalPrice" label="最终成交价(元)" width="150" align="right">
              <template #default="{ row }">¥{{ row.finalPrice?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="myMaxBid" label="我的最高出价(元)" width="150" align="right">
              <template #default="{ row }">¥{{ row.myMaxBid?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="180" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="所有记录" name="all">
          <el-table
            v-loading="loading"
            :data="auctionList"
            stripe
            border
          >
            <el-table-column prop="assetName" label="资产名称" width="200" />
            <el-table-column prop="assetCode" label="资产编号" width="150" />
            <el-table-column prop="finalPrice" label="成交价(元)" width="120" align="right">
              <template #default="{ row }">
                {{ row.finalPrice ? `¥${row.finalPrice?.toLocaleString()}` : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="myMaxBid" label="我的出价(元)" width="120" align="right">
              <template #default="{ row }">¥{{ row.myMaxBid?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="result" label="结果" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.result === 'won'" type="success">中标</el-tag>
                <el-tag v-else-if="row.result === 'lost'" type="info">未中标</el-tag>
                <el-tag v-else type="warning">进行中</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="loadMyAuctions"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getAuctions, confirmTransaction } from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('in_progress')
const loading = ref(false)
const auctionList = ref([])
const total = ref(0)
const confirmStatusFilter = ref('')

const PAGE_SIZE_KEY = 'my_auction_page_size'
const getUserKey = () => userStore.user?.id || userStore.user?.username || userStore.user?.account || 'guest'
const getPageSizeKey = () => `${PAGE_SIZE_KEY}_${getUserKey()}`
const getSavedPageSize = () => Number(localStorage.getItem(getPageSizeKey())) || 10

const queryForm = reactive({
  status: 'in_progress',
  page: 1,
  pageSize: getSavedPageSize()
})

const applyClientFilters = (list) => {
  let result = Array.isArray(list) ? list.slice() : []
  if (confirmStatusFilter.value) {
    result = result.filter(item =>
      String(item.confirmStatus || '').trim().toLowerCase() === confirmStatusFilter.value
    )
  }
  return result
}

const loadMyAuctions = async () => {
  loading.value = true
  try {
    const res = await getAuctions({
      ...queryForm,
      myAuctions: true
    })
    const list = res.data.list || []
    const filtered = applyClientFilters(list)
    auctionList.value = filtered
    total.value = filtered.length
  } catch (error) {
    console.error('加载我的竞拍失败:', error)
  } finally {
    loading.value = false
  }
}

const isEndedByTime = (row) => {
  if (!row?.endTime) return false
  const endTime = new Date(row.endTime).getTime()
  return !Number.isNaN(endTime) && endTime <= Date.now()
}

const canContinueBid = (row) => {
  if (!row) return false
  if (row.status && row.status !== 'in_progress') return false
  return !isEndedByTime(row)
}

const handleTabChange = (tab) => {
  queryForm.status = tab
  queryForm.page = 1
  confirmStatusFilter.value = ''
  loadMyAuctions()
}

const handleSizeChange = (pageSize) => {
  queryForm.pageSize = pageSize
  queryForm.page = 1
  localStorage.setItem(getPageSizeKey(), String(pageSize))
  loadMyAuctions()
}

const handleBid = (row) => {
  router.push(`/auction/${row.id}`)
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认成交后，您需要在48小时内完成付款，逾期未付款将视为违约，将被扣除3个月竞拍资格。确定要确认成交吗？',
      '确认成交',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await confirmTransaction(row.id)
    ElMessage.success('确认成功')
    loadMyAuctions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认失败:', error)
    }
  }
}

const handleViewDetail = (row) => {
  router.push(`/auction/${row.id}`)
}

onMounted(() => {
  syncFromRoute()
  loadMyAuctions()
})

const syncFromRoute = () => {
  const status = String(route.query.status || '').trim().toLowerCase()
  const confirmStatus = String(route.query.confirmStatus || '').trim().toLowerCase()

  confirmStatusFilter.value = confirmStatus || ''

  if (['in_progress', 'ended', 'won', 'lost', 'all'].includes(status)) {
    activeTab.value = status
    queryForm.status = status
  }
}

watch(
  () => route.query,
  () => {
    syncFromRoute()
    loadMyAuctions()
  }
)
</script>

<style scoped>
.my-auctions {
  padding: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
