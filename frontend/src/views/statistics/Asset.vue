<template>
  <div class="asset-statistics">
    <el-card>
      <template #header>
        <span>{{ pageTitle }}</span>
      </template>

      <el-form :inline="true" class="query-form">
        <el-form-item label="统计周期">
          <el-radio-group v-model="period" @change="handlePeriodChange">
            <el-radio-button value="month">按月</el-radio-button>
            <el-radio-button value="quarter">按季</el-radio-button>
            <el-radio-button value="year">按年</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="时间点">
          <el-select
            v-model="selectedPoint"
            placeholder="请选择时间点"
            style="width: 220px"
            :disabled="trendList.length === 0"
            @change="handlePointChange"
          >
            <el-option
              v-for="item in trendList"
              :key="item.periodKey"
              :label="item.periodLabel"
              :value="item.periodKey"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="refreshAll">刷新</el-button>
          <el-button @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="20" class="summary-row">
        <el-col :xs="12" :md="8">
          <el-statistic title="拍卖结束数" :value="summary.totalCount" />
        </el-col>
        <el-col :xs="12" :md="8">
          <el-statistic title="成交金额" :value="summary.totalAmount" :precision="2" prefix="¥" />
        </el-col>
        <el-col :xs="12" :md="8">
          <el-statistic title="流拍率" :value="summary.failureRate" suffix="%" :precision="2" />
        </el-col>
      </el-row>

      <el-card class="trend-card">
        <template #header>
          <div class="card-header">
            <span>近 12 个周期处置趋势（{{ periodText }}）</span>
          </div>
        </template>
        <div ref="trendChartRef" class="chart-container"></div>
      </el-card>

      <el-divider />
      <h3>部门处置汇总（{{ currentPointLabel }}）</h3>
      <el-table v-loading="loading" :data="departmentRows" stripe border>
        <el-table-column prop="departmentName" label="部门" min-width="180" />
        <el-table-column prop="disposalCount" label="处置数量" width="160" align="right" />
        <el-table-column prop="totalAmount" label="成交金额(元)" min-width="180" align="right">
          <template #default="{ row }">¥{{ Number(row.totalAmount || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="failureRate" label="流拍率" width="140" align="right">
          <template #default="{ row }">{{ Number(row.failureRate || 0).toFixed(2) }}%</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { getDisposalTrendStatistics, getDepartmentDisposalComparison } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const period = ref('month')
const selectedPoint = ref('')
const trendList = ref([])
const departmentRows = ref([])

const trendChartRef = ref(null)
let trendChart

const summary = reactive({
  totalCount: 0,
  totalAmount: 0,
  failureRate: 0
})

const isScopedDepartmentView = computed(() => !userStore.hasAnyRole('系统管理员'))
const scopedDepartmentName = computed(() => {
  const directName = userStore.user?.departmentName
  if (directName && String(directName).trim()) {
    return String(directName).trim()
  }
  return '本部门'
})
const pageTitle = computed(() => (
  isScopedDepartmentView.value ? `资产处置统计 · ${scopedDepartmentName.value}` : '资产处置统计'
))

const periodText = computed(() => {
  if (period.value === 'quarter') return '按季'
  if (period.value === 'year') return '按年'
  return '按月'
})

const currentPointLabel = computed(() => {
  if (!selectedPoint.value) return '当前'
  const item = trendList.value.find(x => x.periodKey === selectedPoint.value)
  return item?.periodLabel || selectedPoint.value
})

const renderTrendChart = () => {
  if (!trendChart) return

  const labels = trendList.value.map(item => item.periodLabel)
  const countValues = trendList.value.map(item => Number(item.disposalCount || 0))
  const amountValues = trendList.value.map(item => Number(item.totalAmount || 0))
  const failureRateValues = trendList.value.map(item => Number(item.failureRate || 0))

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const lines = [params?.[0]?.axisValueLabel || '']
        params.forEach(item => {
          if (item.seriesName === '成交金额') {
            lines.push(`${item.marker}${item.seriesName}: ¥${Number(item.value || 0).toLocaleString()}`)
          } else if (item.seriesName === '流拍率') {
            lines.push(`${item.marker}${item.seriesName}: ${Number(item.value || 0).toFixed(2)}%`)
          } else {
            lines.push(`${item.marker}${item.seriesName}: ${item.value || 0}`)
          }
        })
        return lines.join('<br/>')
      }
    },
    legend: {
      data: ['处置数量', '成交金额', '流拍率'],
      top: 6,
      left: 'center'
    },
    grid: { left: 40, right: 130, top: 64, bottom: 60 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: {
        interval: labels.length > 8 ? 1 : 0,
        margin: 14
      }
    },

    yAxis: [
      {
        type: 'value',
        name: '处置数量',
        position: 'left'
      },
      {
        type: 'value',
        name: '成交金额',
        position: 'right',
        axisLabel: {
          formatter: (value) => `¥${Number(value).toLocaleString()}`
        }
      },
      {
        type: 'value',
        name: '流拍率(%)',
        position: 'right',
        offset: 70,
        axisLabel: {
          formatter: (value) => `${value}%`
        }
      }
    ],
    series: [
      {
        name: '处置数量',
        type: 'line',
        smooth: true,
        yAxisIndex: 0,
        data: countValues
      },
      {
        name: '成交金额',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: amountValues
      },
      {
        name: '流拍率',
        type: 'line',
        smooth: true,
        yAxisIndex: 2,
        data: failureRateValues
      }
    ]
  })
}

const initChart = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    renderTrendChart()
  }
}

const resizeChart = () => {
  trendChart?.resize()
}

const loadTrend = async () => {
  const res = await getDisposalTrendStatistics({ period: period.value })
  trendList.value = Array.isArray(res.data) ? res.data : []

  if (!trendList.value.length) {
    selectedPoint.value = ''
    renderTrendChart()
    return
  }

  const hasCurrent = trendList.value.some(item => item.periodKey === selectedPoint.value)
  if (!hasCurrent) {
    selectedPoint.value = trendList.value[trendList.value.length - 1].periodKey
  }

  renderTrendChart()
}

const calcSummary = () => {
  const totalCount = departmentRows.value.reduce((sum, row) => sum + Number(row.disposalCount || 0), 0)
  const totalAmount = departmentRows.value.reduce((sum, row) => sum + Number(row.totalAmount || 0), 0)
  const failedCount = departmentRows.value.reduce((sum, row) => sum + Number(row.failedCount || 0), 0)

  summary.totalCount = totalCount
  summary.totalAmount = totalAmount
  summary.failureRate = totalCount > 0 ? Number(((failedCount / totalCount) * 100).toFixed(2)) : 0
}

const loadDepartmentComparison = async () => {
  if (!selectedPoint.value) {
    departmentRows.value = []
    calcSummary()
    return
  }

  const res = await getDepartmentDisposalComparison({
    period: period.value,
    point: selectedPoint.value
  })
  departmentRows.value = Array.isArray(res.data) ? res.data : []
  calcSummary()
}

const refreshAll = async () => {
  loading.value = true
  try {
    await loadTrend()
    await loadDepartmentComparison()
  } catch (error) {
    console.error('加载资产处置统计失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePeriodChange = async () => {
  await refreshAll()
}

const handlePointChange = async () => {
  loading.value = true
  try {
    await loadDepartmentComparison()
  } catch (error) {
    console.error('加载部门对比失败:', error)
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  const headers = ['部门', '处置数量', '成交金额(元)', '流拍率']
  const rows = departmentRows.value.map(row => [
    row.departmentName || '',
    row.disposalCount || 0,
    row.totalAmount || 0,
    `${Number(row.failureRate || 0).toFixed(2)}%`
  ])

  const csvContent = [headers, ...rows]
    .map(row => row.map(value => `"${String(value ?? '')}"`).join(','))
    .join('\n')

  const blob = new Blob([`\ufeff${csvContent}`], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `资产处置统计_${period.value}_${selectedPoint.value || '当前'}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
}

onMounted(async () => {
  await refreshAll()
  await nextTick()
  initChart()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  trendChart?.dispose()
})
</script>

<style scoped>
.asset-statistics {
  padding: 20px;
}

.query-form {
  margin-bottom: 20px;
}

.summary-row {
  margin-bottom: 20px;
}

.trend-card {
  margin-top: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sub-text {
  color: #909399;
  font-size: 12px;
}

.chart-container {
  width: 100%;
  height: 360px;
}
</style>
