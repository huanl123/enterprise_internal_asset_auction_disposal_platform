import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000  // 增加超时时间到60秒
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    const requestUrl = response.config?.url || ''
    const isAuthRequest = requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register')
    if (res.code !== 200) {
      if (!isAuthRequest) {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    const requestUrl = error.config?.url || ''
    const isAuthRequest = requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register')

    if (!isAuthRequest) {
      if (error.response) {
        if (error.response.status === 401) {
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
        } else if (error.response.status === 403) {
          ElMessage.error('没有权限访问')
        } else {
          ElMessage.error(error.response.data?.message || '请求失败')
        }
      } else {
        ElMessage.error('网络错误，请检查网络连接')
      }
    }

    return Promise.reject(error)
  }
)

export default request