<template>
  <div class="employee-statistics">
    <el-card>
      <template #header>
        <span>员工竞拍记录</span>
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
        <el-form-item label="员工姓名">
          <el-input
            v-model="queryForm.name"
            placeholder="请输入员工姓名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <!-- 统计汇总 -->
      <el-row :gutter="20" class="summary-row">
        <el-col :xs="12" :md="6">
          <el-statistic title="参与人数" :value="summary.totalParticipants" />
        </el-col>
        <el-col :xs="12" :md="6">
          <el-statistic title="总竞拍场次" :value="summary.totalAuctions" />
        </el-col>
        <el-col :xs="12" :md="6">
          <el-statistic title="中标次数" :value="summary.totalWins" />
        </el-col>
        <el-col :xs="12" :md="6">
          <el-statistic title="违约次数" :value="summary.totalBreaches" />
        </el-col>
      </el-row>

      <!-- 详细数据 -->
      <el-table
        v-loading="loading"
        :data="employeeList"
        stripe
        border
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="phone" label="联系方式" width="120" />
        <el-table-column prop="auctionCount" label="竞拍场次" width="100" align="right" />
        <el-table-column prop="winCount" label="中标次数" width="100" align="right" />
        <el-table-column prop="breachCount" label="违约次数" width="100" align="right">
          <template #default="{ row }">
            <el-tag v-if="row.breachCount > 0" type="danger">{{ row.breachCount }}</el-tag>
            <span v-else>{{ row.breachCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="成交总额(元)" width="150" align="right">
          <template #default="{ row }">¥{{ row.totalAmount?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="winRate" label="中标率" width="100" align="right">
          <template #default="{ row }">{{ (row.winRate * 100).toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column label="资产明细" width="100" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleViewDetail(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <!-- 资产明细对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="成交资产明细"
      width="800px"
    >
      <el-table :data="currentEmployeeAssets" stripe border>
        <el-table-column prop="assetCode" label="资产编号" width="150" />
        <el-table-column prop="assetName" label="资产名称" min-width="200" />
        <el-table-column prop="finalPrice" label="成交价(元)" width="120" align="right">
          <template #default="{ row }">¥{{ row.finalPrice?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="auctionTime" label="成交时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'success' : 'danger'">
              {{ row.status === 'completed' ? '已完成' : '已违约' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDepartments, getEmployeeStatistics, getBidsSummary } from '@/api'

const loading = ref(false)
const timeRange = ref('month')
const departments = ref([])
const employeeList = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentEmployeeAssets = ref([])

const queryForm = reactive({
  timeRange: 'month',
  departmentId: '',
  name: '',
  page: 1,
  pageSize: 10
})

const summary = reactive({
  totalParticipants: 0,
  totalAuctions: 0,
  totalWins: 0,
  totalBreaches: 0
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
  queryForm.page = 1
  handleQuery()
}

const buildEmployeeList = (list) => list.map(item => ({
  userId: item.userId,
  name: item.name || item.username || '',
  department: item.department || '',
  phone: item.phone || '-',
  auctionCount: item.auctionCount || 0,
  winCount: item.winCount || 0,
  breachCount: item.defaultCount || 0,
  totalAmount: item.totalAmount || 0,
  winRate: item.auctionCount ? (item.winCount || 0) / item.auctionCount : 0,
  assets: item.assets || []
}))

const computeSummary = (list) => ({
  totalParticipants: list.length,
  totalAuctions: list.reduce((sum, item) => sum + (item.auctionCount || 0), 0),
  totalWins: list.reduce((sum, item) => sum + (item.winCount || 0), 0),
  totalBreaches: list.reduce((sum, item) => sum + (item.breachCount || 0), 0)
})

const handleQuery = async () => {
  loading.value = true
  try {
    // 使用正确的参数结构，后端接口需要 period 参数而不是 timeRange
    const params = {
      period: queryForm.timeRange,
      departmentId: queryForm.departmentId,
      name: queryForm.name,
      page: queryForm.page,
      pageSize: queryForm.pageSize
    }
    const res = await getBidsSummary(params)
    const rawList = Array.isArray(res.data) ? res.data : (res.data?.list || [])
    const mappedList = buildEmployeeList(rawList)

    let filteredList = mappedList
    if (queryForm.name) {
      const keyword = queryForm.name.trim()
      filteredList = filteredList.filter(item => item.name?.includes(keyword))
    }
    if (queryForm.departmentId) {
      const deptName = departments.value.find(d => d.id === queryForm.departmentId)?.name
      if (deptName) {
        filteredList = filteredList.filter(item => item.department === deptName)
      }
    }

    total.value = filteredList.length
    const startIndex = (queryForm.page - 1) * queryForm.pageSize
    const endIndex = startIndex + queryForm.pageSize
    employeeList.value = filteredList.slice(startIndex, endIndex)

    const summaryData = computeSummary(filteredList)
    summary.totalParticipants = summaryData.totalParticipants
    summary.totalAuctions = summaryData.totalAuctions
    summary.totalWins = summaryData.totalWins
    summary.totalBreaches = summaryData.totalBreaches
  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  // TODO: 实现导出功能
  console.log('导出数据')
}

const handleViewDetail = async (row) => {
  try {
    const assets = Array.isArray(row.assets) ? row.assets : []
    currentEmployeeAssets.value = assets.map(item => ({
      assetCode: item.assetId ?? '-',
      assetName: item.assetName || '-',
      finalPrice: item.finalPrice || 0,
      auctionTime: item.transactionDate || '-',
      status: 'completed'
    }))
    detailDialogVisible.value = true
  } catch (error) {
    console.error('加载资产明细失败:', error)
  }
}

onMounted(() => {
  loadDepartments()
  handleQuery()
})
</script>

<style scoped>
.employee-statistics {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
