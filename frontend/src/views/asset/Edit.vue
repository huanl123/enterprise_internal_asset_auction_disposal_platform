<template>
  <div class="asset-edit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>编辑资产</span>
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

        <el-form-item label="资产编号">
          <el-input v-model="form.code" disabled />
        </el-form-item>

        <el-form-item label="资产名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入资产名称" />
        </el-form-item>

        <el-form-item label="规格描述" prop="specification">
          <el-input
            v-model="form.specification"
            type="textarea"
            :rows="3"
            placeholder="请输入资产规格描述"
          />
        </el-form-item>

        <el-form-item label="使用部门">
          <el-input :value="form.departmentName" disabled />
        </el-form-item>

        <el-form-item label="购置日期">
          <el-input :value="form.purchaseDate" disabled />
        </el-form-item>

        <el-form-item label="资产原值">
          <el-input :value="`¥${form.originalValue?.toLocaleString()}`" disabled />
        </el-form-item>

        <el-divider content-position="left">折旧信息</el-divider>

        <el-form-item label="折旧规则">
          <el-input :value="form.depreciationRuleName" disabled />
        </el-form-item>

        <el-form-item label="当前价值">
          <el-input :value="`¥${currentValue?.toLocaleString()}`" disabled />
        </el-form-item>

        <el-form-item label="建议起拍价">
          <el-input :value="`¥${startPrice?.toLocaleString()}`" disabled />
        </el-form-item>

        <el-divider content-position="left">资产图片</el-divider>

        <el-form-item label="资产图片">
          <AssetImageWall
            :items="imageWallItems"
            :max="5"
            :disabled="isUploadDisabled"
            :uploading="uploading"
            tip="最多上传5张图片，支持jpg、png格式。点击图片可预览。"
            @select-files="handleSelectFiles"
            @remove="handleWallRemove"
            @reorder="handleWallReorder"
          />
        </el-form-item>


        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            保存修改
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
import { getAssetDetail, updateAsset, uploadAssetImages } from '@/api'
import AssetImageWall from '@/components/asset/AssetImageWall.vue'

const router = useRouter()
const route = useRoute()

const formRef = ref()
const submitting = ref(false)
const uploading = ref(false)
const originalSnapshot = ref(null)

const form = reactive({
  id: '',
  code: '',
  name: '',
  specification: '',
  departmentName: '',
  departmentId: '',
  purchaseDate: '',
  originalValue: 0,
  currentValue: 0,
  startPrice: 0,
  depreciationRuleId: '',
  depreciationRuleName: '',
  images: []
})


const rules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  specification: [{ required: true, message: '请输入规格描述', trigger: 'blur' }]
}

const currentValue = computed(() => form.currentValue)
const startPrice = computed(() => form.startPrice)
const isUploadDisabled = computed(() => uploading.value || (form.images?.length || 0) >= 5)
const imageWallItems = computed(() =>
  (form.images || []).map((url, index) => ({
    uid: `asset-image-${index}-${url}`,
    url
  }))
)

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

const buildSnapshot = (data) => ({
  ...data,
  images: normalizeImages(data?.images)
})

const applySnapshot = (snapshot) => {
  if (!snapshot) return
  Object.assign(form, snapshot)
  form.images = Array.isArray(snapshot.images) ? [...snapshot.images] : []
  formRef.value?.clearValidate()
}

const loadAssetDetail = async () => {
  try {
    const res = await getAssetDetail(route.params.id)
    const snapshot = buildSnapshot(res.data)
    applySnapshot(snapshot)
    originalSnapshot.value = snapshot
  } catch (error) {
    console.error('加载资产详情失败:', error)
  }
}
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }

  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
  }

  return isImage && isLt5M
}

const extractUploadUrls = (response) => {
  return Array.isArray(response?.data)
    ? response.data
    : Array.isArray(response?.data?.urls)
      ? response.data.urls
      : []
}

const handleSelectFiles = async (files = []) => {
  if (!form.id) {
    const error = new Error('资产ID不存在，无法上传图片')
    ElMessage.error(error.message)
    return
  }

  if (uploading.value) return

  const remaining = Math.max(0, 5 - (form.images?.length || 0))
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

  uploading.value = true
  try {
    let uploadedCount = 0

    for (const file of validFiles) {
      const response = await uploadAssetImages(form.id, [file])
      if (response.code !== 200) {
        throw new Error(response.message || '上传失败')
      }

      const urls = extractUploadUrls(response)
      if (urls.length) {
        form.images = [...form.images, urls[0]]
        uploadedCount += 1
      }
    }

    if (uploadedCount > 0) {
      if (uploadedCount === 1) {
        ElMessage.success('上传成功')
      } else {
        ElMessage.success(`成功上传${uploadedCount}张图片`)
      }
    } else {
      ElMessage.warning('未获取到上传后的图片地址')
    }
  } catch (error) {
    console.error('上传图片失败:', error)
    ElMessage.error('上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

const removeImage = (index) => {
  if (index < 0 || index >= form.images.length) return
  form.images.splice(index, 1)
}

const handleWallRemove = (_item, index) => {
  removeImage(index)
}

const moveImage = (fromIndex, toIndex) => {
  if (
    fromIndex < 0 ||
    toIndex < 0 ||
    fromIndex >= form.images.length ||
    toIndex >= form.images.length ||
    fromIndex === toIndex
  ) {
    return
  }

  const next = [...form.images]
  const [moved] = next.splice(fromIndex, 1)
  next.splice(toIndex, 0, moved)
  form.images = next
}

const handleWallReorder = ({ fromIndex, toIndex }) => {
  moveImage(fromIndex, toIndex)
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  submitting.value = true
  try {
    await updateAsset({
      id: form.id,
      name: form.name,
      specification: form.specification,
      images: form.images.join(',')
    })
    ElMessage.success('保存成功')
    originalSnapshot.value = buildSnapshot({
      ...form,
      images: form.images
    })
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}


const handleReset = () => {
  applySnapshot(originalSnapshot.value)
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadAssetDetail()
})
</script>

<style scoped>
.asset-edit {
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
