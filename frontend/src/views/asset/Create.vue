<template>
  <div class="asset-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>新增资产</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="asset-form"
      >
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="资产名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入资产名称" />
        </el-form-item>

        <el-form-item label="资产编号" prop="code">
          <el-input 
            v-model="form.code" 
            placeholder="系统自动生成或手动输入" 
            :disabled="!!generatedCode"
          />
          <el-button 
            type="primary" 
            plain 
            @click="generateCode"
            :disabled="!!generatedCode"
            style="margin-left: 10px;"
          >
            {{ generatedCode ? '已生成' : '自动生成' }}
          </el-button>
          <el-button 
            v-if="generatedCode"
            type="warning" 
            plain 
            @click="clearGeneratedCode"
            style="margin-left: 10px;"
          >
            清除
          </el-button>
        </el-form-item>

        <el-form-item label="规格描述" prop="specification">
          <el-input
            v-model="form.specification"
            type="textarea"
            :rows="3"
            placeholder="请输入资产规格描述"
          />
        </el-form-item>

        <el-form-item label="使用部门" prop="departmentId">
          <el-select v-model="form.departmentId" placeholder="请选择使用部门" style="width: 100%">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="购置日期" prop="purchaseDate">
          <el-date-picker
            v-model="form.purchaseDate"
            type="date"
            placeholder="请选择购置日期"
            style="width: 100%"
            :disabled-date="disableFutureDate"
          />
        </el-form-item>

        <el-form-item label="资产原值" prop="originalValue">
          <el-input-number
            v-model="form.originalValue"
            :min="0.01"
            :precision="2"
            :step="0.01"
            controls-position="right"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999">元</span>
        </el-form-item>

        <el-divider content-position="left">折旧信息</el-divider>

        <el-form-item label="折旧规则" prop="depreciationRuleId">
          <el-select v-model="form.depreciationRuleId" placeholder="请选择折旧规则" style="width: 100%">
            <el-option
              v-for="rule in depreciationRules"
              :key="rule.id"
              :label="`${rule.name} (年限: ${rule.usefulLife}年, 残值率: ${rule.salvageRate}%)`"
              :value="rule.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="currentValue" label="当前价值">
          <el-input :value="`¥${currentValue.toLocaleString()}`" disabled>
            <template #append>
              <el-button @click="calculateValue">重新计算</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item v-if="startPrice" label="建议起拍价">
          <el-input :value="`¥${startPrice.toLocaleString()}`" disabled />
        </el-form-item>

        <el-divider content-position="left">资产图片</el-divider>

        <el-form-item label="资产图片" prop="images">
          <AssetImageWall
            :items="imageWallItems"
            :max="5"
            :disabled="isUploadDisabled"
            :uploading="submitting"
            tip="最多上传5张图片，支持jpg、png格式。点击图片可预览。"
            @select-files="handleSelectFiles"
            @remove="handleWallRemove"
            @reorder="handleWallReorder"
          />
        </el-form-item>


        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交审核
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AssetImageWall from '@/components/asset/AssetImageWall.vue'
import { validatePositiveMoney, MONEY_VALIDATION_MESSAGE } from '@/utils/amountValidation'
import {
  getDepartments,
  getActiveDepreciationRules,
  createAsset,
  deleteAsset,
  uploadAssetImages
} from '@/api'

const router = useRouter()

const formRef = ref()
const submitting = ref(false)
const departments = ref([])
const depreciationRules = ref([])
const wallImages = ref([])
const generatedCode = ref(null) // 存储自动生成的编号
const form = reactive({
  name: '',
  code: '',
  specification: '',
  departmentId: '',
  purchaseDate: '',
  originalValue: null,
  depreciationRuleId: '',
  images: []
})

const rules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入资产编号', trigger: 'blur' }],
  specification: [{ required: true, message: '请输入规格描述', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择使用部门', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择购置日期', trigger: 'change' }],
  originalValue: [
    { required: true, message: '请输入资产原值', trigger: 'blur' }
  ],
  depreciationRuleId: [
    { required: true, message: '请选择折旧规则', trigger: 'change' }
  ]
}

const isUploadDisabled = computed(() => submitting.value || wallImages.value.length >= 5)
const imageWallItems = computed(() => wallImages.value)
const disableFutureDate = (date) => date.getTime() > Date.now()

const currentValue = computed(() => {
  if (!form.originalValue || !form.purchaseDate || !form.depreciationRuleId) {
    return form.originalValue || 0
  }

  const rule = depreciationRules.value.find(r => r.id === form.depreciationRuleId)
  if (!rule) return form.originalValue || 0

  const purchaseDate = new Date(form.purchaseDate)
  const now = new Date()
  const actualYears = (now - purchaseDate) / (1000 * 60 * 60 * 24 * 365)

  const salvageRate = (rule.salvageRate || 0) / 100
  const safeSalvageRate = Math.min(Math.max(salvageRate, 0), 1)
  const salvageValue = form.originalValue * safeSalvageRate
  let currentVal

  if (actualYears <= rule.usefulLife) {
    currentVal = form.originalValue * (1 - (1 - safeSalvageRate) * (actualYears / rule.usefulLife))
  } else {
    currentVal = salvageValue
  }

  currentVal = Math.min(currentVal, form.originalValue)
  return Math.max(0, Math.round(currentVal * 100) / 100)
})

const startPrice = computed(() => {
  if (!currentValue.value) return null
  return Math.max(0, Math.round(currentValue.value * 0.8 * 100) / 100)
})

const loadDepartments = async () => {
  try {
    const res = await getDepartments()
    departments.value = res.data
  } catch (error) {
    console.error('加载部门失败:', error)
  }
}

const loadDepreciationRules = async () => {
  try {
    const res = await getActiveDepreciationRules()
    depreciationRules.value = res.data || []
  } catch (error) {
    console.error('加载折旧规则失败:', error)
  }
}

const calculateValue = () => {
  currentValue.value
}

const generateCode = async () => {
  try {
    const today = new Date()
    const year = today.getFullYear()
    const month = String(today.getMonth() + 1).padStart(2, '0')
    const day = String(today.getDate()).padStart(2, '0')
    const randomNum = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
    
    const tempCode = `ASSET-${year}${month}${day}-${randomNum}`
    generatedCode.value = tempCode
    form.code = tempCode
    
    ElMessage.success('已生成资产编号')
  } catch (error) {
    console.error('生成资产编号失败:', error)
    ElMessage.error('生成资产编号失败')
  }
}

const clearGeneratedCode = () => {
  generatedCode.value = null
  form.code = ''
}


const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const maxSize = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  if (!maxSize) {
    ElMessage.error('图片大小不能超过5MB!')
  }
  
  return isImage && maxSize
}

const revokeLocalUrl = (item) => {
  if (item?.url && typeof item.url === 'string' && item.url.startsWith('blob:')) {
    URL.revokeObjectURL(item.url)
  }
}

const handleSelectFiles = (files = []) => {
  const remaining = Math.max(0, 5 - wallImages.value.length)
  if (remaining <= 0) {
    ElMessage.warning('最多上传5张图片')
    return
  }

  const selected = files.slice(0, remaining)
  if (files.length > remaining) {
    ElMessage.warning('最多上传5张图片，超出的图片已忽略')
  }

  const validFiles = selected.filter(beforeUpload)
  if (!validFiles.length) return

  const items = validFiles.map((file) => ({
    uid: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    file,
    url: URL.createObjectURL(file),
    status: 'done'
  }))

  wallImages.value = [...wallImages.value, ...items]
}

const handleWallRemove = (item, index) => {
  if (index < 0 || index >= wallImages.value.length) return
  const [removed] = wallImages.value.splice(index, 1)
  revokeLocalUrl(removed || item)
}

const moveWallImage = (fromIndex, toIndex) => {
  if (
    fromIndex < 0 ||
    toIndex < 0 ||
    fromIndex >= wallImages.value.length ||
    toIndex >= wallImages.value.length ||
    fromIndex === toIndex
  ) {
    return
  }
  const next = [...wallImages.value]
  const [moved] = next.splice(fromIndex, 1)
  next.splice(toIndex, 0, moved)
  wallImages.value = next
}

const handleWallReorder = ({ fromIndex, toIndex }) => {
  moveWallImage(fromIndex, toIndex)
}

const clearWallImages = () => {
  wallImages.value.forEach(revokeLocalUrl)
  wallImages.value = []
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  if (!validatePositiveMoney(form.originalValue)) {
    ElMessage.error(`资产原值${MONEY_VALIDATION_MESSAGE}`)
    return
  }
  if (!validatePositiveMoney(currentValue.value)) {
    ElMessage.error(`当前价值${MONEY_VALIDATION_MESSAGE}`)
    return
  }
  if (!validatePositiveMoney(startPrice.value)) {
    ElMessage.error(`起拍价${MONEY_VALIDATION_MESSAGE}`)
    return
  }

  if (wallImages.value.length === 0) {
    ElMessage.warning('请至少上传一张资产图片')
    return
  }

  submitting.value = true
  let assetId = null

  try {
    // 先创建资产并获取资产ID
    const assetData = {
      ...form,
      images: '', purchaseDate: form.purchaseDate.toISOString().split('T')[0]
    }

    // 在提交前添加计算出的currentValue和startPrice
    assetData.currentValue = currentValue.value
    assetData.startPrice = startPrice.value

    const createResult = await createAsset(assetData)
    assetId = createResult.data.id

    // 上传图片到对应的资产ID
    const filesToUpload = wallImages.value
      .map(item => item.file)
      .filter(file => file instanceof File)

    const response = await uploadAssetImages(assetId, filesToUpload)
    if (response.code !== 200) {
      throw new Error(response.message || '图片上传失败')
    }

    ElMessage.success('资产创建成功，已提交审核')
    handleReset()
  } catch (error) {
    if (assetId) {
      try {
        await deleteAsset(assetId)
      } catch (e) {
        // ignore
      }
    }

    console.error('创建资产失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  formRef.value.resetFields()
  clearWallImages()
  generatedCode.value = null
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadDepartments()
  loadDepreciationRules()
})

onBeforeUnmount(() => {
  clearWallImages()
})
</script>

<style scoped>
.asset-create {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.asset-form {
  max-width: 800px;
}

</style>
