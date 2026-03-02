<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>企业废旧资产内部拍卖与处置平台</h1>
        <p>Internal Auction & Disposal Platform</p>
      </div>
      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入工号"
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
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
        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="login-form"
            @keyup.enter="handleRegister"
          >
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login as loginApi, register as registerApi, getDepartments } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const departments = ref([])

const loginFormRef = ref()
const loginForm = reactive({
  username: '',
  password: ''
})

const registerFormRef = ref()
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
  departmentId: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
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

const loadDepartments = async () => {
  try {
    const res = await getDepartments()
    departments.value = res.data
  } catch (error) {
    console.error('加载部门失败:', error)
    // 如果API调用失败，使用模拟数据
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

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    const res = await loginApi(loginForm)
    
    // 直接使用 res，因为拦截器已经处理了 Result 包装
    // res 包含的是后端 UserLoginResponse 的数据
    if (res && res.data && res.data.token) {
      if (userStore) {
        userStore.setToken(res.data.token)
        // 检查用户信息是否存在
        if (res.data.user) {
          console.log('设置用户信息:', res.data.user)
          userStore.setUser(res.data.user)
        } else {
          console.log('用户信息为空')
        }
      }
      ElMessage.success('登录成功')
      console.log('准备跳转到首页')
      console.log('当前token:', userStore.token)
      console.log('当前用户:', userStore.user)
      try {
        router.push('/')
        console.log('跳转成功')
      } catch (error) {
        console.error('跳转失败:', error)
      }
    } else {
      console.error('登录响应数据结构异常:', res)
      ElMessage.error('用户名或密码错误')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('用户名或密码错误')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  const valid = await registerFormRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    await registerApi({
      username: registerForm.username,
      password: registerForm.password,
      name: registerForm.name,
      phone: registerForm.phone,
      departmentId: registerForm.departmentId
    })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    registerFormRef.value?.resetFields()
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败')
    activeTab.value = 'login'
    registerFormRef.value?.resetFields()
  } finally {
    loading.value = false
  }
}

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
