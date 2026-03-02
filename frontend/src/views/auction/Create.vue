<template>
  <div class="auction-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建拍卖活动</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="auction-form"
      >
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="拍卖名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入拍卖活动名称" />
        </el-form-item>

        <el-form-item label="选择资产" prop="assetId">
          <el-select
            v-model="form.assetId"
            placeholder="请选择待拍卖资产"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="asset in availableAssets"
              :key="asset.id"
              :label="`${asset.code} - ${asset.name} (起拍价: ¥${asset.startPrice})`"
              :value="asset.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledEndDate"
          />
        </el-form-item>

        <el-form-item label="加价幅度" prop="incrementAmount">
          <el-input-number
            v-model="form.incrementAmount"
            :min="1"
            :step="10"
            controls-position="right"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #999">元</span>
        </el-form-item>

        <el-form-item label="保留价">
          <el-switch v-model="form.hasReservePrice" />
          <span style="margin-left: 10px; color: #999">
            {{ form.hasReservePrice ? '已开启' : '未开启' }}
          </span>
        </el-form-item>

        <el-form-item v-if="form.hasReservePrice && form.assetId" label="保留价金额">
          <el-input
            :value="`¥${reservePrice?.toLocaleString()}`"
            placeholder="默认为当前价值"
            disabled
          />
        </el-form-item>

        <el-divider content-position="left">参与部门</el-divider>

        <el-form-item label="参与部门" prop="departmentIds">
          <el-checkbox-group v-model="form.departmentIds">
            <el-checkbox
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.id"
              :value="dept.id"
            >
              {{ dept.name }}
            </el-checkbox>
            <el-checkbox label="all" value="all">全公司</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-divider content-position="left">拍卖须知</el-divider>

        <el-form-item label="拍卖说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请输入拍卖须知和说明"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            发布拍卖
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAssets, getDepartments, createAuction, getAssetDetail, getAuctions } from '@/api'

const router = useRouter()
const route = useRoute()

const formRef = ref()
const submitting = ref(false)
const availableAssets = ref([])
const departments = ref([])
const selectedAsset = ref(null)

const form = reactive({
  name: '',
  assetId: route.query.assetId || '',
  startTime: '',
  endTime: '',
  incrementAmount: 100,
  hasReservePrice: false,
  reservePrice: null,
  departmentIds: [],
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入拍卖名称', trigger: 'blur' }],
  assetId: [{ required: true, message: '请选择资产', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  incrementAmount: [
    { required: true, message: '请输入加价幅度', trigger: 'blur' }
  ],
  departmentIds: [
    { type: 'array', required: true, message: '请选择参与部门', trigger: 'change' }
  ],
  description: [{ required: true, message: '请输入拍卖说明', trigger: 'blur' }]
}

const reservePrice = computed(() => {
  if (!selectedAsset.value) return null
  return selectedAsset.value.currentValue
})

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const disabledEndDate = (time) => {
  if (!form.startTime) return false
  return time.getTime() <= new Date(form.startTime).getTime()
}

const loadAvailableAssets = async () => {
  try {
    const [assetsRes, auctionsRes] = await Promise.all([
      getAssets({ status: '待拍卖', pageSize: 1000 }),
      getAuctions({ pageSize: 1000 })
    ])

    const auctionedAssetIds = new Set(
      (auctionsRes.data?.list || [])
        .filter(auction => ['not_started', 'in_progress', 'PENDING'].includes(auction.status))
        .map(auction => auction.assetId)
    )

    const filteredAssets = (assetsRes.data?.list || []).filter(
      asset => !auctionedAssetIds.has(asset.id)
    )

    availableAssets.value = filteredAssets

    if (form.assetId && !filteredAssets.some(asset => asset.id === form.assetId)) {
      form.assetId = ''
      selectedAsset.value = null
    }
  } catch (error) {
    console.error('加载可用资产失败:', error)
  }
}

const loadDepartments = async () => {
  try {
    const res = await getDepartments()
    departments.value = res.data
  } catch (error) {
    console.error('加载部门失败:', error)
  }
}

const loadAssetInfo = async (assetId) => {
  if (!assetId) return
  try {
    const res = await getAssetDetail(assetId)
    selectedAsset.value = res.data
  } catch (error) {
    console.error('加载资产信息失败:', error)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  submitting.value = true
  try {
    // 处理部门ID数组，转换为字符串格式
    let departmentIds = ''
    if (form.departmentIds.includes('all')) {
      departmentIds = 'all'
    } else {
      departmentIds = form.departmentIds.join(',')
    }
    
    await createAuction({
      name: form.name,
      assetId: form.assetId,
      startTime: form.startTime,
      endTime: form.endTime,
      incrementAmount: form.incrementAmount,
      hasReservePrice: form.hasReservePrice,
      reservePrice: form.hasReservePrice ? reservePrice.value : null,
      departmentIds: departmentIds,
      description: form.description
    })
    ElMessage.success('拍卖活动创建成功')
    goBack()
  } catch (error) {
    console.error('创建拍卖失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  formRef.value.resetFields()
  selectedAsset.value = null
}

const goBack = () => {
  router.back()
}

// 监听资产选择变化
import { watch } from 'vue'

// 监听资产选择变化
watch(() => form.assetId, (newVal) => {
  if (newVal) {
    loadAssetInfo(newVal)
  }
})

onMounted(() => {
  loadAvailableAssets()
  loadDepartments()
  if (form.assetId) {
    loadAssetInfo(form.assetId)
  }
})
</script>

<style scoped>
.auction-create {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.auction-form {
  max-width: 800px;
}
</style>
