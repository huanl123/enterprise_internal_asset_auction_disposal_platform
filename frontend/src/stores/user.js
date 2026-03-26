import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const storage = window.sessionStorage
  const legacyStorage = window.localStorage

  const migrateLegacyAuthState = () => {
    const legacyToken = legacyStorage.getItem('token')
    const legacyUser = legacyStorage.getItem('user')

    if (!storage.getItem('token') && legacyToken) {
      storage.setItem('token', legacyToken)
    }
    if (!storage.getItem('user') && legacyUser) {
      storage.setItem('user', legacyUser)
    }

    legacyStorage.removeItem('token')
    legacyStorage.removeItem('user')
  }

  migrateLegacyAuthState()

  const token = ref(storage.getItem('token') || '')
  const user = ref(JSON.parse(storage.getItem('user') || 'null'))

  const setToken = (newToken) => {
    token.value = newToken
    storage.setItem('token', newToken)
  }

  const setUser = (userData) => {
    user.value = userData
    storage.setItem('user', JSON.stringify(userData))
  }

  const logout = () => {
    token.value = ''
    user.value = null
    storage.removeItem('token')
    storage.removeItem('user')
    legacyStorage.removeItem('token')
    legacyStorage.removeItem('user')
  }

const hasRole = (role) => {
  if (!user.value?.role) return false
  const userRole = user.value.role
  
  // 角色映射：将中文角色名映射到对应的英文角色名
  const chineseToEnglishMap = {
    '系统管理员': ['ADMIN', 'SYSTEM_ADMIN', 'admin'],
    '资产专员': ['ASSET_SPECIALIST', 'asset_specialist'],
    '财务专员': ['FINANCE_SPECIALIST', 'finance_specialist'],
    '普通员工': ['EMPLOYEE', 'NORMAL_USER', 'employee']
  }
  
  // 获取对应的英文角色列表
  const englishRoles = chineseToEnglishMap[userRole] || [userRole]
  
  // 检查是否匹配要求的角色
  const requiredRoles = chineseToEnglishMap[role] || [role]
  
  const result = englishRoles.some(userEngRole => 
    requiredRoles.includes(userEngRole) || userEngRole.toUpperCase() === role.toUpperCase()
  )
  
  console.log('权限检查 - 用户角色:', userRole, '要求角色:', role, '结果:', result)
  return result
}

const hasAnyRole = (...roles) => {
  if (!user.value?.role) return false
  const userRole = user.value.role
  
  // 角色映射：将中文角色名映射到对应的英文角色名
  const chineseToEnglishMap = {
    '系统管理员': ['ADMIN', 'SYSTEM_ADMIN', 'admin'],
    '资产专员': ['ASSET_SPECIALIST', 'asset_specialist'],
    '财务专员': ['FINANCE_SPECIALIST', 'finance_specialist'],
    '普通员工': ['EMPLOYEE', 'NORMAL_USER', 'employee']
  }
  
  // 获取用户的英文角色列表
  const englishUserRoles = chineseToEnglishMap[userRole] || [userRole]
  
  // 检查是否匹配任意一个要求的角色
  return roles.some(requiredRole => {
    // 处理中文角色名查找
    let requiredEnglishRoles = []
    if (chineseToEnglishMap[requiredRole]) {
      requiredEnglishRoles = chineseToEnglishMap[requiredRole]
    } else {
      // 如果是英文角色名，直接使用
      requiredEnglishRoles = [requiredRole]
    }
    
    return englishUserRoles.some(userEngRole => 
      requiredEnglishRoles.includes(userEngRole) || 
      userEngRole.toUpperCase() === requiredRole.toUpperCase() ||
      requiredRole.toUpperCase() === userEngRole.toUpperCase()
    )
  })
}

// 添加一个辅助函数，用于检查用户是否拥有任何管理员角色
const isAdmin = () => {
  return hasAnyRole('系统管理员')
}

// 添加一个辅助函数，用于检查用户是否拥有资产专员角色
const isAssetSpecialist = () => {
  return hasAnyRole('资产专员')
}

// 添加一个辅助函数，用于检查用户是否拥有财务专员角色
const isFinanceSpecialist = () => {
  return hasAnyRole('财务专员')
}

// 添加一个辅助函数，用于检查用户是否为普通员工
const isNormalUser = () => {
  return hasAnyRole('普通员工')
}

return {
    token,
    user,
    setToken,
    setUser,
    logout,
    hasRole,
    hasAnyRole,
    isAdmin,
    isAssetSpecialist,
    isFinanceSpecialist,
    isNormalUser
}
})
