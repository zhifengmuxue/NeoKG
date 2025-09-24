import { ref, computed } from 'vue'

// 用户信息接口
export interface UserInfo {
  id?: string | number
  username?: string
  email?: string
  nickname?: string
  avatar?: string
  avatarSeed?: string // 新增：Dicebear 头像种子
  roles?: string[]
  permissions?: string[]
  [key: string]: any
}

// 全局用户状态
export const isLoggedIn = ref<boolean>(false)
export const userInfo = ref<UserInfo>({})

// 生成用户头像种子
const generateAvatarSeed = (userInfo: UserInfo): string => {
  // 优先使用用户ID，然后是用户名，最后是随机生成
  if (userInfo.id) {
    return `user_${userInfo.id}`
  } else if (userInfo.username) {
    return `username_${userInfo.username}`
  } else if (userInfo.email) {
    return `email_${userInfo.email.split('@')[0]}`
  } else {
    return `random_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }
}

// 计算用户显示名称
export const userDisplayName = computed(() => {
  if (!userInfo.value) return 'Guest'
  return userInfo.value.nickname || userInfo.value.username || userInfo.value.email || 'User'
})

// 计算用户头像 - 支持传统头像和 Dicebear
export const userAvatar = computed(() => {
  if (!userInfo.value) return null
  
  // 如果用户已有自定义头像，优先使用
  if (userInfo.value.avatar && userInfo.value.avatar.startsWith('http')) {
    return userInfo.value.avatar
  }
  
  // 否则返回 null，让组件使用 Dicebear
  return null
})

// 获取用户头像种子
export const userAvatarSeed = computed(() => {
  if (!userInfo.value) return 'guest'
  
  // 如果已有保存的种子，使用它
  if (userInfo.value.avatarSeed) {
    return userInfo.value.avatarSeed
  }
  
  // 否则生成新的种子
  return generateAvatarSeed(userInfo.value)
})

// 登录函数 - 这是 Login.vue 需要的
export const login = (user: UserInfo, token?: string): void => {
  console.log('执行登录操作，用户信息:', user, '令牌:', token)
  
  // 如果没有头像种子，生成一个
  if (!user.avatarSeed) {
    user.avatarSeed = generateAvatarSeed(user)
  }
  
  userInfo.value = user
  isLoggedIn.value = true
  
  // 保存到localStorage
  localStorage.setItem('userInfo', JSON.stringify(user))
  localStorage.setItem('isLoggedIn', 'true')
  
  // 如果有token，也保存
  if (token) {
    localStorage.setItem('token', token)
  }
  
  console.log('登录状态已更新，当前用户:', userInfo.value)
}

// 设置用户信息
export const setUserInfo = (info: UserInfo): void => {
  // 如果没有头像种子，生成一个
  if (!info.avatarSeed) {
    info.avatarSeed = generateAvatarSeed(info)
  }
  
  userInfo.value = info
  isLoggedIn.value = true
  
  // 保存到localStorage
  localStorage.setItem('userInfo', JSON.stringify(info))
  localStorage.setItem('isLoggedIn', 'true')
}

// 更新用户头像种子
export const updateAvatarSeed = (newSeed: string): void => {
  if (userInfo.value) {
    userInfo.value.avatarSeed = newSeed
    // 清除传统头像
    if (userInfo.value.avatar && !userInfo.value.avatar.startsWith('http')) {
      delete userInfo.value.avatar
    }
    
    // 保存到localStorage
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }
}

// 登出
export const logout = (): void => {
  userInfo.value = {}
  isLoggedIn.value = false
  localStorage.removeItem('userInfo')
  localStorage.removeItem('isLoggedIn')
  localStorage.removeItem('token')
}

// 初始化用户状态
export const initUserState = (): void => {
  const savedUserInfo = localStorage.getItem('userInfo')
  const savedLoginState = localStorage.getItem('isLoggedIn')
  
  if (savedUserInfo && savedLoginState === 'true') {
    try {
      const info = JSON.parse(savedUserInfo)
      // 如果旧数据没有头像种子，生成一个
      if (!info.avatarSeed) {
        info.avatarSeed = generateAvatarSeed(info)
        localStorage.setItem('userInfo', JSON.stringify(info))
      }
      userInfo.value = info
      isLoggedIn.value = true
    } catch (error) {
      console.error('Failed to parse user info:', error)
      logout()
    }
  }
}