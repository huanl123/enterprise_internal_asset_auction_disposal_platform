<template>
  <!-- 个人中心页面 -->
  <div class="profile">
    <el-card>
      <template #header>
        <span>个人中心</span>
      </template>

      <el-row :gutter="20">
        <!-- 左侧：头像和基本信息 -->
        <el-col :xs="24" :md="8">
          <div class="avatar-section">
            <!-- 头像显示 -->
            <el-avatar :size="150" :src="avatarPreview">
              {{ user.name?.charAt(0) }}
            </el-avatar>
            <!-- 头像上传功能 -->
            <div class="avatar-actions">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button size="small">上传头像</el-button>
              </el-upload>
            </div>
            <!-- 用户名和角色标签 -->
            <h3 class="username">{{ user.name }}</h3>
            <el-tag :type="getRoleType(user.role)" size="large">
              {{ getRoleText(user.role) }}
            </el-tag>
          </div>
        </el-col>
        <!-- 右侧：个人信息编辑表单 -->
        <el-col :xs="24" :md="16">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            class="profile-form"
          >
            <!-- 不可修改字段：工号、部门、注册时间 -->
            <el-form-item label="工号">
              <el-input v-model="user.username" disabled />
            </el-form-item>
            <!-- 可修改字段：姓名、联系方式 -->
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item label="联系方式" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item label="所属部门">
              <el-input :model-value="user.departmentName || user.department?.name || user.department || ''" disabled />
            </el-form-item>
            <el-form-item label="注册时间">
              <el-input :model-value="user.createTime || user.registerTime || ''" disabled />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">
                保存修改
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

      <!-- 个人统计信息 -->
      <el-divider />
      <h3>我的统计</h3>
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :sm="6">
          <el-card class="stat-card">
            <el-statistic title="参与竞拍" :value="stats.auctionCount" />
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card class="stat-card">
            <el-statistic title="中标次数" :value="stats.winCount" />
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card class="stat-card">
            <el-statistic title="违约次数" :value="stats.breachCount" />
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card class="stat-card">
            <el-statistic title="成交总额(元)" :value="stats.totalAmount" :precision="2" prefix="¥" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 最近活动时间线 -->
      <el-divider />
      <h3>中标并成交资产明细</h3>
      <el-timeline>
        <el-timeline-item
          v-for="activity in recentActivities"
          :key="activity.id"
          :timestamp="activity.time"
          :type="activity.type"
        >
          {{ activity.content }}
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
// 导入依赖
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateProfile, getBiddingStatistics, uploadAvatar } from '@/api'

// 初始化用户状态管理
const userStore = useUserStore()
const user = reactive(userStore.user || {})

// 表单引用和提交状态
const formRef = ref()
const submitting = ref(false)
const uploading = ref(false)

// 头像预览
const avatarPreview = ref(user.avatar || '')
const avatarObjectUrl = ref('')

// 表单数据
const form = reactive({
  name: user.name || '',
  phone: user.phone || ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 10, message: '姓名长度不能超过10位', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 统计数据
const stats = reactive({
  auctionCount: 0,
  winCount: 0,
  breachCount: 0,
  totalAmount: 0
})

// 最近活动列表
const recentActivities = ref([])

// 获取角色标签类型
const getRoleType = (role) => {
  const roleMap = {
    'admin': 'danger',
    'ADMIN': 'danger',
    'SYSTEM_ADMIN': 'danger',
    'asset_specialist': 'warning',
    'ASSET_SPECIALIST': 'warning',
    'finance_specialist': 'success',
    'FINANCE_SPECIALIST': 'success',
    'employee': 'info',
    'EMPLOYEE': 'info',
    'NORMAL_USER': 'info'
  }
  return roleMap[role] || 'info'
}

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    'admin': '系统管理员',
    'ADMIN': '系统管理员',
    'SYSTEM_ADMIN': '系统管理员',
    'asset_specialist': '资产专员',
    'ASSET_SPECIALIST': '资产专员',
    'finance_specialist': '财务专员',
    'FINANCE_SPECIALIST': '财务专员',
    'employee': '普通员工',
    'EMPLOYEE': '普通员工',
    'NORMAL_USER': '普通员工'
  }
  return roleMap[role] || '未知'
}

// 提交表单修改
const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  submitting.value = true
  try {
    await updateProfile({
      name: form.name,
      phone: form.phone,
      avatar: avatarPreview.value || undefined
    })
    ElMessage.success('保存成功')
    // 更新 store 中的用户信息
    userStore.setUser({
      ...userStore.user,
      name: form.name,
      phone: form.phone,
      avatar: avatarPreview.value || userStore.user?.avatar
    })
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    submitting.value = false
  }
}



const handleReset = () => {
  form.name = user.name
  form.phone = user.phone
  if (avatarObjectUrl.value) {
    URL.revokeObjectURL(avatarObjectUrl.value)
    avatarObjectUrl.value = ''
  }
  avatarPreview.value = user.avatar || ''
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片格式')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

const handleAvatarUpload = async (options) => {
  if (uploading.value) return
  uploading.value = true
  try {
    if (avatarObjectUrl.value) {
      URL.revokeObjectURL(avatarObjectUrl.value)
    }
    avatarObjectUrl.value = URL.createObjectURL(options.file)
    avatarPreview.value = avatarObjectUrl.value

    const res = await uploadAvatar(options.file)
    const url = res.data?.url
    if (url) {
      avatarPreview.value = url
      user.avatar = url
      await updateProfile({
        name: form.name,
        phone: form.phone,
        avatar: url
      })
      userStore.setUser({
        ...userStore.user,
        avatar: url
      })
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
  } finally {
    uploading.value = false
  }
}

const loadMyStatistics = async () => {
  try {
    const userId = userStore.user?.id
    if (!userId) return

    const res = await getBiddingStatistics({ userId })
    const statsData = Array.isArray(res.data) ? res.data[0] : null
    if (!statsData) {
      stats.auctionCount = 0
      stats.winCount = 0
      stats.breachCount = 0
      stats.totalAmount = 0
      recentActivities.value = []
      return
    }

    stats.auctionCount = Number(statsData.auctionCount || 0)
    stats.winCount = Number(statsData.winCount || 0)
    stats.breachCount = Number(statsData.defaultCount || 0)
    stats.totalAmount = Number(statsData.totalAmount || 0)

    const assets = Array.isArray(statsData.assets) ? statsData.assets : []
    recentActivities.value = assets.map((item, index) => ({
      id: `${item.assetId}-${index}`,
      type: 'success',
      time: item.transactionDate || '',
      content: `成交“${item.assetName}”，成交价 ¥${Number(item.finalPrice || 0).toLocaleString()}`
    }))
  } catch (error) {
    console.error('加载个人统计失败:', error)
    stats.auctionCount = 0
    stats.winCount = 0
    stats.breachCount = 0
    stats.totalAmount = 0
    recentActivities.value = []
  }
}

onMounted(() => {
  loadMyStatistics()
})
</script>

<style scoped>
.profile {
  padding: 20px;
}

.avatar-section {
  text-align: center;
  padding: 20px;
}

.avatar-actions {
  margin-top: 12px;
}

.username {
  margin: 20px 0 10px;
  font-size: 24px;
  color: #333;
}

.profile-form {
  padding: 20px;
}

.stats-row {
  margin-top: 20px;
}

.stat-card {
  text-align: center;
  margin-bottom: 20px;
}
</style>
