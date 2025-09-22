import axios from 'axios'

// 基础配置
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 创建axios实例
const authApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
authApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('userToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
authApi.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // 清除token并跳转到登录页
      localStorage.removeItem('userToken')
      localStorage.removeItem('userInfo')
      window.location.href = '/auth/login'
    }
    return Promise.reject(error.response?.data || error.message)
  }
)

// 类型定义
export interface LoginRequest {
  username: string
  password: string
  rememberMe: boolean
}

export interface RegisterRequest {
  username: string
  email?: string  // 改为可选
  nickname?: string
  password: string
  confirmPassword: string
  rememberMe: boolean
}

export interface AuthResponse {
  code: string
  message: string | null
  data: {
    user: {
      id: string
      username: string
      email: string
      nickname?: string
      avatar?: string
      phone?: string
      createdAt?: string
      lastLoginAt?: string
    }
    token: string
  }
  timestamp: number
}

export interface ApiResult<T = any> {
  code: string
  message: string | null
  data: T
  timestamp: number
}

// 模拟登录函数（降级处理）
const mockLogin = async (username: string, password: string): Promise<AuthResponse> => {
  await new Promise(resolve => setTimeout(resolve, 1000))
  
  if (username === 'admin' && password === '123456') {
    return {
      code: 'SUCCESS',
      message: null,
      data: {
        user: {
          id: '1',
          username: 'admin',
          email: 'admin@neokg.com',
          nickname: '管理员',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin'
        },
        token: 'mock-jwt-token-' + Date.now()
      },
      timestamp: Date.now()
    }
  } else {
    throw { message: '用户名或密码错误' }
  }
}

// 模拟注册函数（降级处理）
const mockRegister = async (data: RegisterRequest): Promise<AuthResponse> => {
  await new Promise(resolve => setTimeout(resolve, 1500))
  
  if (data.username === 'admin') {
    throw { message: '用户名已存在' }
  }
  
  return {
    code: 'SUCCESS',
    message: null,
    data: {
      user: {
        id: '2',
        username: data.username,
        email: data.email,
        nickname: data.nickname || data.username,
        avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${data.username}`
      },
      token: 'mock-jwt-token-' + Date.now()
    },
    timestamp: Date.now()
  }
}

// API方法
export const authService = {
  // 登录
  async login(data: LoginRequest): Promise<AuthResponse> {
    try {
      console.log('调用登录API:', data) // 调试用
      
      const result = await authApi.post('/auth/login', {
        username: data.username,
        password: data.password,
        rememberMe: data.rememberMe
      })
      
      console.log('API返回结果:', result) // 调试用
      
      // 如果后端只返回token，转换为前端期望的格式
      if (result.code === 'SUCCESS' && typeof result.data === 'string') {
        console.log('转换token格式:', result.data) // 调试用
        return {
          code: result.code,
          message: result.message,
          data: {
            user: {
              id: '',
              username: data.username,
              email: '',
              nickname: data.username,
              avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${data.username}`
            },
            token: result.data
          },
          timestamp: result.timestamp
        }
      }
      
      // 如果是标准格式，直接返回
      console.log('返回标准格式结果') // 调试用
      return result
      
    } catch (error: any) {
      console.error('API调用错误:', error) // 调试用
      
      // 检查是否是axios响应错误
      if (error.response) {
        console.error('HTTP错误响应:', error.response.status, error.response.data)
        // 重新抛出错误，但保持原始格式
        throw error
      }
      
      // 如果是网络错误，使用模拟数据
      if (error.code === 'ERR_NETWORK' || error.message?.includes('Network Error')) {
        console.warn('后端接口未连接，使用模拟登录')
        return mockLogin(data.username, data.password)
      }
      
      // 重新抛出错误，让上层处理
      throw error
    }
  },

  // 注册 - 适配后端返回格式
  async register(data: RegisterRequest): Promise<AuthResponse> {
    try {
      console.log('调用注册API:', data) // 调试用
      
      const result = await authApi.post('/auth/register', {
        username: data.username,
        email: data.email,
        nickname: data.nickname,
        password: data.password,
        confirmPassword: data.confirmPassword,
        rememberMe: data.rememberMe
      })
      
      console.log('注册API返回结果:', result) // 调试用
      
      // 如果后端只返回token，转换为前端期望的格式
      if (result.code === 'SUCCESS' && typeof result.data === 'string') {
        console.log('转换注册token格式:', result.data) // 调试用
        return {
          code: result.code,
          message: result.message,
          data: {
            user: {
              id: '',
              username: data.username,
              email: data.email,
              nickname: data.nickname || data.username,
              avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${data.username}`
            },
            token: result.data
          },
          timestamp: result.timestamp
        }
      }
      
      // 如果是标准格式，直接返回
      console.log('返回标准格式注册结果') // 调试用
      return result
      
    } catch (error: any) {
      console.error('注册API调用错误:', error) // 调试用
      
      // 检查是否是axios响应错误
      if (error.response) {
        console.error('注册HTTP错误响应:', error.response.status, error.response.data)
        // 重新抛出错误，但保持原始格式
        throw error
      }
      
      // 如果是网络错误，使用模拟数据
      if (error.code === 'ERR_NETWORK' || error.message?.includes('Network Error')) {
        console.warn('后端接口未连接，使用模拟注册')
        return mockRegister(data)
      }
      
      // 重新抛出错误，让上层处理
      throw error
    }
  },

  // 登出
  async logout(): Promise<ApiResult<string>> {
    return authApi.post('/auth/logout')
  },

  // 获取用户信息
  async getUserInfo(): Promise<ApiResult<any>> {
    return authApi.get('/auth/userinfo')
  },

  // 刷新token
  async refreshToken(): Promise<ApiResult<string>> {
    return authApi.post('/auth/refresh')
  }
}