<template>
  <!-- 登录页面容器 -->
  <div class="login-container">
    <!-- 登录框主体 -->
    <div class="login-box">
      <!-- 页面头部：显示平台名称 -->
      <div class="login-header">
        <h1>企业废旧资产内部拍卖与处置平台</h1>
        <p>Internal Auction & Disposal Platform</p>
      </div>
      <!-- 标签页切换：登录/注册 -->
      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 登录表单 -->
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <!-- 工号输入 -->
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入工号"
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
            <!-- 密码输入 -->
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <!-- 登录按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-button"
                :loading="loading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- 注册表单 -->
        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="login-form"
            @keyup.enter="handleRegister"
          >
            <!-- 注册信息：工号、密码、确认密码 -->
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入工号"
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                size="large"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <!-- 个人信息：姓名、电话 -->
            <el-form-item prop="name">
              <el-input
                v-model="registerForm.name"
                placeholder="请输入姓名"
                size="large"
                prefix-icon="UserFilled"
              />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入联系方式"
                size="large"
                prefix-icon="Phone"
              />
            </el-form-item>
            <!-- 部门选择 -->
            <el-form-item prop="departmentId">
              <el-select
                v-model="registerForm.departmentId"
                placeholder="请选择部门"
                size="large"
                style="width: 100%"
              >
                <el-option
                  v-for="dept in departments"
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-button"
                :loading="loading"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
// 导入依赖
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login as loginApi, register as registerApi, getDepartments } from '@/api'

// 初始化路由和状态管理
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const activeTab = ref('login') // 当前激活的标签页
const loading = ref(false) // 加载状态
const departments = ref([]) // 部门列表

// 表单引用和数据
const loginFormRef = ref() // 登录表单引用
const loginForm = reactive({ username: '', password: '' }) // 登录表单数据

const registerFormRef = ref() // 注册表单引用
const registerForm = reactive({ // 注册表单数据
  username: '', password: '', confirmPassword: '', name: '', phone: '', departmentId: ''
})

// 登录表单验证规则
const loginRules = {
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 注册表单验证规则（包含密码一致性校验、手机号格式校验）
const registerRules = {
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  // 自定义验证器：检查两次密码是否一致
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }]
}

// 加载部门列表（支持降级到模拟数据）
const loadDepartments = async () => {
  try {
    const res = await getDepartments()
    departments.value = res.data
  } catch (error) {
    console.error('加载部门失败:', error)
    // API 调用失败时使用模拟数据
    departments.value = [
      { id: 1, name: '技术部' },
      { id: 2, name: '财务部' },
      { id: 3, name: '行政部' },
      { id: 4, name: '市场部' },
      { id: 5, name: '人力资源部' },
      { id: 6, name: '资产管理部' },
      { id: 7, name: '总经办' }
    ]
  }
}

// 处理登录函数
const handleLogin = async () => {
  // 表单验证
  const valid = await loginFormRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    // 调用登录 API
    const res = await loginApi({
      username: String(loginForm.username || '').trim(),
      password: String(loginForm.password || '')
    })
    
    // 保存 token 和用户信息
    if (res && res.data && res.data.token) {
      if (userStore) {
        userStore.setToken(res.data.token)
        if (res.data.user) {
          userStore.setUser(res.data.user)
        }
      }
      ElMessage.success('登录成功')
      // 跳转到首页
      router.push('/')
    } else {
      ElMessage.error('用户名或密码错误')
    }
  } catch (error) {
    console.error('登录失败:', error)
    const message = String(error?.message || '')
    // 特殊处理账号被禁用的情况
    if (message.includes('禁用')) {
      ElMessage.error(message)
    } else {
      ElMessage.error('用户名或密码错误')
    }
  } finally {
    loading.value = false
  }
}

// 处理注册函数
const handleRegister = async () => {
  // 表单验证
  const valid = await registerFormRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    // 调用注册 API
    await registerApi({
      username: String(registerForm.username || '').trim(),
      password: String(registerForm.password || ''),
      name: String(registerForm.name || '').trim(),
      phone: String(registerForm.phone || '').trim(),
      departmentId: registerForm.departmentId
    })
    ElMessage.success('注册成功，请登录')
    // 切换到登录 Tab 并清空表单
    activeTab.value = 'login'
    registerFormRef.value?.resetFields()
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载部门列表
onMounted(() => {
  loadDepartments()
})
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 450px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.login-header p {
  font-size: 14px;
  color: #999;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  margin-top: 10px;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.el-tabs__item) {
  font-size: 16px;
}
</style>
