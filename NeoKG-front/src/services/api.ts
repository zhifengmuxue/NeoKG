import axios from 'axios'
import type { ApiGraphData } from '../types/graph'

// 创建axios实例
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    console.log('API请求:', config.method?.toUpperCase(), config.url)
    return config
  },
  (error) => {
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    console.log('API响应:', response.status, response.config.url)
    return response
  },
  (error) => {
    console.error('API响应错误:', error.response?.status, error.config?.url, error.message)
    return Promise.reject(error)
  }
)

/**
 * 获取文档-关键词图数据
 */
export const getDocKeywordGraph = async (): Promise<ApiGraphData> => {
  try {
    const response = await apiClient.get<ApiGraphData>('/graph/doc-keyword')
    return response.data
  } catch (error) {
    console.error('获取文档-关键词图数据失败:', error)
    throw error
  }
}

/**
 * 获取所有可用的图类型
 */
export const getGraphTypes = async (): Promise<string[]> => {
  try {
    const response = await apiClient.get<string[]>('/graph/types')
    return response.data
  } catch (error) {
    console.error('获取图类型失败:', error)
    throw error
  }
}

// 删除关键词
export const deleteKeyword = async (id: string): Promise<void> => {
  try {
    const response = await fetch(`/api/keywords/${id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
  } catch (error) {
    console.error('删除关键词失败:', error)
    throw error
  }
}

// 删除文档
export const deleteDocument = async (id: string): Promise<void> => {
  try {
    const response = await fetch(`/api/documents/${id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
  } catch (error) {
    console.error('删除文档失败:', error)
    throw error
  }
}

export default apiClient
