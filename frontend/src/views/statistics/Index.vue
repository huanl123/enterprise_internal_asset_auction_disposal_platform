<template>
  <div class="statistics-overview">
    <el-card class="page-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>统计总览</span>
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
        <el-col :xs="24" :lg="14">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>统计查询入口</span>
              </div>
            </template>
            <div class="quick-grid">
              <button class="quick-btn quick-btn-primary" @click="goTo('/statistics/archive')">
                <span class="quick-title">资产处置档案查询</span>
                <span class="quick-desc">按资产编号/名称查询完整处置档案、交易单、凭证与流程记录</span>
              </button>
              <button class="quick-btn quick-btn-green" @click="goTo('/statistics/asset')">
                <span class="quick-title">资产处置统计</span>
                <span class="quick-desc">按月/季/年查看各部门处置数量、成交金额、流拍率并导出</span>
              </button>
              <button class="quick-btn quick-btn-orange" @click="goTo('/statistics/employee')">
                <span class="quick-title">员工竞拍记录</span>
                <span class="quick-desc">查看员工参拍场次、中标次数、违约次数与成交明细</span>
              </button>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="10">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>口径说明</span>
                <el-icon><DataAnalysis /></el-icon>
              </div>
            </template>
            <div class="tips-list">
              <div class="tip-item">
                <div class="tip-title">统计总览</div>
                <div class="tip-desc">仅展示全局概览与模块入口，不重复展示处置统计图表。</div>
              </div>
              <div class="tip-item">
                <div class="tip-title">资产处置统计</div>
                <div class="tip-desc">用于正式统计分析与导出，覆盖部门对比、趋势与汇总数据。</div>
              </div>
              <div class="tip-item">
                <div class="tip-title">资产处置档案查询</div>
                <div class="tip-desc">用于查询单个资产的完整处置链路（交易单、审批、凭证、处置记录）。</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { DataAnalysis, Goods, CircleCheck, Wallet, TrendCharts } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api'

const router = useRouter()

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
}

.page-card {
  background: #fff;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 12px;
}

.section-row {
  margin-top: 8px;
}

.quick-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.quick-btn {
  width: 100%;
  text-align: left;
  border: 1px solid #e5eaf3;
  border-radius: 12px;
  background: #fff;
  padding: 14px 16px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.quick-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.06);
}

.quick-btn-primary:hover {
  border-color: #409eff;
}

.quick-btn-green:hover {
  border-color: #67c23a;
}

.quick-btn-orange:hover {
  border-color: #e6a23c;
}

.quick-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.quick-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tip-item {
  padding: 12px;
  border-radius: 10px;
  background: #f7f9fc;
  border: 1px solid #edf1f7;
}

.tip-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.tip-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

@media (max-width: 992px) {
  .section-row {
    margin-top: 0;
  }
}
</style>
