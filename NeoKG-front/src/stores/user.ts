import { ref, computed } from 'vue'

// 用户信息类型定义
export interface UserInfo {
  id: string
  username: string
  email: string
  avatar?: string
  nickname?: string
  phone?: string
  createdAt?: string
  lastLoginAt?: string
}

// 用户状态
export const isLoggedIn = ref(false)
export const userInfo = ref<UserInfo | null>(null)

// 计算属性
export const userDisplayName = computed(() => {
  if (!userInfo.value) return '未登录'
  return userInfo.value.nickname || userInfo.value.username || '用户'
})

export const userAvatar = computed(() => {
  return userInfo.value?.avatar || ''
})

// 登录
export const login = (user: UserInfo, token: string) => {
  try {
    console.log('更新用户状态:', user, token) // 调试用
    
    userInfo.value = user
    isLoggedIn.value = true
    
    localStorage.setItem('userToken', token)
    localStorage.setItem('userInfo', JSON.stringify(user))
    
    console.log('用户状态更新完成:', {
      isLoggedIn: isLoggedIn.value,
      userInfo: userInfo.value,
      token: localStorage.getItem('userToken')
    }) // 调试用
    
  } catch (error) {
    console.error('更新用户状态失败:', error)
    throw error
  }
}

// 登出
export const logout = () => {
  userInfo.value = null
  isLoggedIn.value = false
  localStorage.removeItem('userToken')
  localStorage.removeItem('userInfo')
}

// 更新用户信息
export const updateUserInfo = (updatedInfo: Partial<UserInfo>) => {
  if (userInfo.value) {
    userInfo.value = { ...userInfo.value, ...updatedInfo }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }
}

// 初始化用户状态
export const initUserState = () => {
  const token = localStorage.getItem('userToken')
  const savedUserInfo = localStorage.getItem('userInfo')
  
  if (token && savedUserInfo) {
    try {
      const user = JSON.parse(savedUserInfo)
      userInfo.value = user
      isLoggedIn.value = true
    } catch (error) {
      console.error('解析用户信息失败:', error)
      logout()
    }
  }
}

// 获取认证令牌
export const getAuthToken = () => {
  return localStorage.getItem('userToken')
}