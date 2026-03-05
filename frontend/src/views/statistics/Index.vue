<template>
  <div class="statistics-overview">
    <el-card class="page-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ overviewTitle }}</span>
        </div>
      </template>

      <el-row :gutter="16" class="stat-row">
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card" shadow="hover">
            <el-statistic title="总资产数" :value="stats.totalAssets">
              <template #suffix><el-icon><Goods /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card" shadow="hover">
            <el-statistic title="已处置资产" :value="stats.disposedAssets">
              <template #suffix><el-icon><CircleCheck /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card" shadow="hover">
            <el-statistic title="总成交金额(元)" :value="stats.totalAmount" :precision="2">
              <template #suffix><el-icon><Wallet /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card" shadow="hover">
            <el-statistic title="流拍率" :value="stats.failRate" suffix="%">
              <template #suffix><el-icon><TrendCharts /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="section-row">
        <el-col :span="24">
          <el-card class="entry-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>统计查询入口</span>
              </div>
            </template>
            <div class="entry-grid">
              <button class="entry-btn entry-btn-archive" @click="goTo('/statistics/archive')">
                <div class="entry-title-wrap">
                  <span class="entry-title">资产处置档案查询</span>
                  <span class="entry-pill">档案</span>
                </div>
                <span class="entry-desc">按资产编号或名称检索，查看交易单、凭证与流程记录。</span>
                <span class="entry-action">进入模块</span>
              </button>
              <button class="entry-btn entry-btn-stat" @click="goTo('/statistics/asset')">
                <div class="entry-title-wrap">
                  <span class="entry-title">资产处置统计</span>
                  <span class="entry-pill">统计</span>
                </div>
                <span class="entry-desc">查看趋势、部门对比与汇总数据，支持按周期筛选和导出。</span>
                <span class="entry-action">进入模块</span>
              </button>
              <button class="entry-btn entry-btn-employee" @click="goTo('/statistics/employee')">
                <div class="entry-title-wrap">
                  <span class="entry-title">员工竞拍记录</span>
                  <span class="entry-pill">人员</span>
                </div>
                <span class="entry-desc">查看员工参拍场次、中标次数、违约次数与成交明细。</span>
                <span class="entry-action">进入模块</span>
              </button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Goods, CircleCheck, Wallet, TrendCharts } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isScopedDepartmentView = computed(() => !userStore.hasAnyRole('系统管理员'))
const scopedDepartmentName = computed(() => {
  const directName = userStore.user?.departmentName
  if (directName && String(directName).trim()) {
    return String(directName).trim()
  }
  return '本部门'
})
const overviewTitle = computed(() => (
  isScopedDepartmentView.value ? `统计总览 · ${scopedDepartmentName.value}` : '统计总览'
))

const stats = reactive({
  totalAssets: 0,
  disposedAssets: 0,
  totalAmount: 0,
  failRate: 0
})

const goTo = (path) => {
  router.push(path)
}

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    const data = res.data || {}
    stats.totalAssets = Number(data.totalAssets) || 0
    stats.disposedAssets = Number(data.disposedAssets) || 0
    stats.totalAmount = Number(data.totalAmount) || 0
    stats.failRate = Number(data.failRate) || 0
  } catch (error) {
    console.error('加载统计总览失败:', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.statistics-overview {
  padding: 20px;
  background: linear-gradient(180deg, #f5f7fb 0%, #ffffff 100%);
}

.page-card {
  background: #fff;
  border: 1px solid #e8edf5;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stat-row {
  margin-bottom: 18px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #e6ebf2;
  background: linear-gradient(180deg, #ffffff 0%, #fafcff 100%);
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(32, 60, 100, 0.08);
}

.section-row {
  margin-top: 8px;
}

.entry-card {
  border: 1px solid #e8edf5;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.entry-btn {
  text-align: left;
  border: 1px solid #e4eaf5;
  border-radius: 12px;
  background: #fff;
  padding: 16px;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.entry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(32, 60, 100, 0.1);
}

.entry-btn-archive {
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  border-color: #cfe0ff;
}

.entry-btn-stat {
  background: linear-gradient(180deg, #f7fcf9 0%, #ffffff 100%);
  border-color: #cfe9d7;
}

.entry-btn-employee {
  background: linear-gradient(180deg, #fffaf4 0%, #ffffff 100%);
  border-color: #f4dfc2;
}

.entry-title-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.entry-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.entry-pill {
  font-size: 12px;
  line-height: 1;
  color: #5f6b7a;
  padding: 5px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid #dde4ee;
}

.entry-desc {
  font-size: 13px;
  color: #5b6472;
  line-height: 1.5;
}

.entry-action {
  margin-top: 2px;
  font-size: 13px;
  color: #2f5ea8;
  font-weight: 500;
}

@media (max-width: 992px) {
  .section-row {
    margin-top: 0;
  }

  .entry-grid {
    grid-template-columns: 1fr;
  }
}

@media (min-width: 993px) and (max-width: 1320px) {
  .entry-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
