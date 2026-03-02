<template>
  <div class="asset-statistics">
    <el-card>
      <template #header>
        <span>资产处置统计</span>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="时间范围">
          <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
            <el-radio-button value="month">按月</el-radio-button>
            <el-radio-button value="quarter">按季</el-radio-button>
            <el-radio-button value="year">按年</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="queryForm.departmentId" placeholder="全部部门" clearable style="width: 150px">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <!-- 统计汇总 -->
      <el-row :gutter="20" class="summary-row">
        <el-col :xs="12" :md="6">
          <el-statistic title="处置总数" :value="summary.totalCount" />
        </el-col>
        <el-col :xs="12" :md="6">
          <el-statistic title="总成交金额" :value="summary.totalAmount" :precision="2" prefix="¥" />
        </el-col>
        <el-col :xs="12" :md="6">
          <el-statistic title="流拍数" :value="summary.failCount" />
        </el-col>
        <el-col :xs="12" :md="6">
          <el-statistic title="流拍率" :value="summary.failRate" suffix="%" />
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :xs="24" :md="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>{{ trendTitle }}</span>
                <div class="trend-actions">
                  <el-radio-group v-model="trendPeriod" size="small" @change="handleTrendPeriodChange">
                    <el-radio-button label="month">按月</el-radio-button>
                    <el-radio-button label="day">按天</el-radio-button>
                  </el-radio-group>
                  <el-date-picker
                    v-if="trendPeriod === 'day'"
                    v-model="selectedMonth"
                    type="month"
                    value-format="YYYY-MM"
                    format="YYYY-MM"
                    size="small"
                    clearable
                    placeholder="选择月份"
                    :disabled-date="disableFutureMonth"
                    @change="handleMonthChange"
                  />
                  <el-button v-if="trendPeriod === 'day' && selectedMonth" text size="small" @click="clearSelectedMonth">
                    返回月份
                  </el-button>
                  <span v-else-if="trendPeriod === 'day'" class="trend-hint">请选择月份查看按天明细</span>
                </div>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-card>
            <template #header>部门对比</template>
            <div ref="deptChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细数据 -->
      <el-divider />
      <h3>详细数据</h3>
      <el-table
        v-loading="loading"
        :data="statisticsList"
        stripe
        border
        :summary-method="getSummaries"
        show-summary
      >
        <el-table-column prop="period" label="时间周期" width="120" />
        <el-table-column prop="department" label="部门" width="150" />
        <el-table-column prop="count" label="处置数量" width="100" align="right" />
        <el-table-column prop="amount" label="成交金额(元)" width="150" align="right">
          <template #default="{ row }">¥{{ row.amount?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="failCount" label="流拍数量" width="100" align="right" />
        <el-table-column prop="avgPrice" label="平均成交价(元)" width="150" align="right">
          <template #default="{ row }">¥{{ row.avgPrice?.toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { getDepartments, getAssetDisposalStatistics, getDepartmentDisposalStatistics, getDealTrend } from '@/api'

const loading = ref(false)
const timeRange = ref('month')
const departments = ref([])
const statisticsList = ref([])
const trendData = ref([])
const trendPeriod = ref('month')
const selectedMonth = ref('')

const trendChartRef = ref(null)
const deptChartRef = ref(null)
let trendChart
let deptChart

const queryForm = reactive({
  timeRange: 'month',
  departmentId: ''
})

const summary = reactive({
  totalCount: 0,
  totalAmount: 0,
  failCount: 0,
  failRate: 0
})

const trendTitle = computed(() => {
  if (trendPeriod.value === 'day') {
    return selectedMonth.value ? `成交趋势（${selectedMonth.value}·按天）` : '成交趋势（点击月份查看按天）'
  }
  return '成交趋势（近6个月·按月）'
})

const loadDepartments = async () => {
  try {
    const res = await getDepartments()
    departments.value = res.data
  } catch (error) {
    console.error('加载部门失败:', error)
  }
}

const handleTimeRangeChange = () => {
  queryForm.timeRange = timeRange.value
  handleQuery()
}

const getPeriodLabel = (period) => {
  switch (period) {
    case 'month':
      return '本月'
    case 'quarter':
      return '本季度'
    case 'year':
      return '本年'
    default:
      return ''
  }
}

const renderTrendChart = () => {
  if (!trendChart) return
  const labels = trendData.value.map(item => item.label)
  const amounts = trendData.value.map(item => Number(item.amount || 0))
  const hasMany = labels.length > 12
  const showMonthOverview = trendPeriod.value === 'day' && !selectedMonth.value

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: {
        interval: showMonthOverview ? 0 : (hasMany ? 2 : 0),
        rotate: showMonthOverview ? 0 : (hasMany ? 30 : 0),
        formatter: (value) => {
          if (trendPeriod.value === 'day') {
            if (selectedMonth.value) {
              return typeof value === 'string' && value.length >= 10 ? value.slice(5) : value
            }
            return typeof value === 'string' ? value.slice(0, 7) : value
          }
          return value
        }
      }
    },
    yAxis: { type: 'value' },
    dataZoom: [],
    series: [{
      name: '成交金额',
      type: 'line',
      smooth: true,
      data: amounts,
      symbol: 'circle',
      symbolSize: 6
    }]
  })
}

const renderDeptChart = () => {
  if (!deptChart) return
  const names = statisticsList.value.map(item => item.department || '')
  const failed = statisticsList.value.map(item => item.failCount || 0)
  const success = statisticsList.value.map(item => (item.count || 0) - (item.failCount || 0))
  const hasMany = names.length > 6
  deptChart.setOption({
    color: ['#4C8DFF', '#FF8C69'],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const title = params?.[0]?.axisValueLabel || ''
        const lines = params.map(p => `${p.marker}${p.seriesName}：${p.data || 0}`)
        return [title, ...lines].join('<br/>')
      }
    },
    legend: { data: ['成交', '流拍'], top: 4, right: 10 },
    grid: { left: 50, right: 20, top: 40, bottom: hasMany ? 60 : 40 },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        interval: 0,
        rotate: hasMany ? 30 : 0,
        formatter: (value) => (value && value.length > 6 ? `${value.slice(0, 6)}…` : value)
      }
    },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F0F0F0' } } },
    dataZoom: hasMany ? [{ type: 'inside' }, { type: 'slider', height: 12, bottom: 8 }] : [],
    series: [
      { name: '成交', type: 'bar', barWidth: 18, barGap: '30%', itemStyle: { borderRadius: [6, 6, 0, 0] }, data: success },
      { name: '流拍', type: 'bar', barWidth: 18, barGap: '30%', itemStyle: { borderRadius: [6, 6, 0, 0] }, data: failed }
    ]
  })
}

const handleTrendClick = (params) => {
  if (trendPeriod.value !== 'day' || selectedMonth.value) return

  if (params?.componentType === 'xAxis') {
    const value = String(params?.value || params?.name || '')
    if (/^\d{4}-\d{2}$/.test(value)) {
      selectedMonth.value = value
      loadTrendData()
      return
    }
  }

  if (params?.componentType === 'series' && params?.name) {
    const value = String(params.name)
    if (/^\d{4}-\d{2}$/.test(value)) {
      selectedMonth.value = value
      loadTrendData()
      return
    }
  }
}

const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    trendChart.on('click', handleTrendClick)
  }
  if (deptChartRef.value) {
    deptChart = echarts.init(deptChartRef.value)
  }
  renderTrendChart()
  renderDeptChart()
}

const resizeCharts = () => {
  trendChart?.resize()
  deptChart?.resize()
}

const loadTrendData = async () => {
  try {
    if (trendPeriod.value !== 'day') {
      selectedMonth.value = ''
    }
    const params = trendPeriod.value === 'day'
      ? (selectedMonth.value ? { period: 'day', month: selectedMonth.value } : { period: 'month' })
      : { period: 'month' }
    const res = await getDealTrend(params)
    trendData.value = res.data || []
    renderTrendChart()
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

const handleTrendPeriodChange = () => {
  if (trendPeriod.value !== 'day') {
    selectedMonth.value = ''
  }
  loadTrendData()
}

const disableFutureMonth = (date) => date.getTime() > Date.now()

const handleMonthChange = (value) => {
  selectedMonth.value = value || ''
  loadTrendData()
}

const clearSelectedMonth = () => {
  selectedMonth.value = ''
  loadTrendData()
}

const handleQuery = async () => {
  loading.value = true
  try {
    const [summaryRes, listRes] = await Promise.all([
      getAssetDisposalStatistics({ period: queryForm.timeRange }),
      getDepartmentDisposalStatistics({ period: queryForm.timeRange })
    ])

    const summaryData = summaryRes.data || {}
    summary.totalCount = summaryData.totalAssets || 0
    summary.totalAmount = summaryData.totalAmount || 0
    summary.failCount = summaryData.failedCount || 0
    summary.failRate = summaryData.failureRate != null ? Number(summaryData.failureRate).toFixed(2) : 0

    const rawList = Array.isArray(listRes.data) ? listRes.data : (listRes.data?.list || [])
    const periodLabel = getPeriodLabel(queryForm.timeRange)
    let mappedList = rawList.map(item => {
      const totalCount = item.disposalCount || 0
      const failedCount = item.failedCount || 0
      const successCount = totalCount - failedCount
      return {
        period: periodLabel,
        department: item.departmentName || '未知部门',
        departmentId: item.departmentId,
        count: totalCount,
        amount: item.totalAmount || 0,
        failCount: failedCount,
        avgPrice: successCount > 0 ? (Number(item.totalAmount || 0) / successCount) : 0
      }
    })
    
    // 过滤掉未分配部门的数据
    mappedList = mappedList.filter(item => item.departmentId != null && item.departmentId !== 0 && item.department !== '未分配')

    if (queryForm.departmentId) {
      mappedList = mappedList.filter(item => item.departmentId === queryForm.departmentId)
    }

    statisticsList.value = mappedList
    renderDeptChart()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  const headers = ['时间周期', '部门', '处置数量', '成交金额(元)', '流拍数量', '平均成交价(元)']
  const rows = statisticsList.value.map(row => [
    row.period,
    row.department,
    row.count,
    row.amount,
    row.failCount,
    row.avgPrice ? row.avgPrice.toFixed(2) : '0.00'
  ])

  const csvContent = [headers, ...rows]
    .map(row => row.map(value => `"${String(value ?? '')}"`).join(','))
    .join('\n')

  const blob = new Blob([`\ufeff${csvContent}`], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const filename = `资产处置统计_${getPeriodLabel(queryForm.timeRange) || queryForm.timeRange}.csv`
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}

const getSummaries = (param) => {
  const { columns, data } = param
  const sums = []

  // 使用summary中的总体数据，确保与顶部汇总一致
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '总计'
      return
    }
    if (index === 1) {
      sums[index] = ''
      return
    }
    if (column.property === 'count') {
      sums[index] = summary.totalCount
    } else if (column.property === 'amount') {
      sums[index] = summary.totalAmount
    } else if (column.property === 'failCount') {
      sums[index] = summary.failCount
    } else if (column.property === 'avgPrice') {
      const total = summary.totalAmount
      const successCount = summary.totalCount - summary.failCount
      sums[index] = successCount > 0 ? `¥${(total / successCount).toFixed(2)}` : '¥0.00'
    } else {
      sums[index] = ''
    }
  })

  return sums
}

onMounted(async () => {
  await loadDepartments()
  await handleQuery()
  await loadTrendData()
  await nextTick()
  initCharts()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.off('click', handleTrendClick)
  trendChart?.dispose()
  deptChart?.dispose()
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
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trend-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.trend-hint {
  color: #909399;
  font-size: 12px;
}

.chart-container {
  height: 300px;
  width: 100%;
}
</style>
