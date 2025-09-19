<template>
  <div class="upload-page" :style="{ backgroundColor: isDark ? '#1f1f1f' : '#ffffff' }">
    <!-- 页面标题 -->
    <div class="page-header" :style="getHeaderStyle()">
      <h2 :style="{ color: isDark ? '#ffffff' : '#333', margin: 0 }">文件上传</h2>
      <p :style="{ color: isDark ? '#b3b3b3' : '#666', margin: '8px 0 0 0' }">
        支持上传多种格式的文件到知识图谱系统
      </p>
    </div>

    <!-- 上传区域 -->
    <div class="upload-section" :style="getCardStyle()">
      <a-upload-dragger
        v-model:fileList="fileList"
        name="file"
        :multiple="true"
        :action="uploadUrl"
        :beforeUpload="beforeUpload"
        :onChange="handleChange"
        :onRemove="handleRemove"
        :showUploadList="false"
        :style="getDraggerStyle()"
      >
        <div class="upload-content">
          <p class="ant-upload-drag-icon" :style="{ color: isDark ? '#666' : '#999' }">
            <cloud-upload-outlined style="font-size: 48px;" />
          </p>
          <p class="ant-upload-text" :style="{ color: isDark ? '#ffffff' : '#333' }">
            点击或拖拽文件到此区域上传
          </p>
          <p class="ant-upload-hint" :style="{ color: isDark ? '#b3b3b3' : '#666' }">
            支持单个或批量上传。支持 .txt, .md, .csv, .json, .xml, .pdf, .docx 等格式
          </p>
        </div>
      </a-upload-dragger>
    </div>

    <!-- 文件列表 (移到前面) -->
    <div class="file-list-section" :style="getCardStyle()">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3 :style="{ color: isDark ? '#ffffff' : '#333', margin: 0 }">文件列表</h3>
        <a-space>
          <a-button 
            @click="clearAll" 
            :disabled="fileList.length === 0"
            :style="getButtonStyle()"
          >
            清空列表
          </a-button>
          <a-button 
            type="primary" 
            @click="startUpload" 
            :loading="uploading"
            :disabled="fileList.length === 0"
          >
            开始上传
          </a-button>
        </a-space>
      </div>
      
      <a-table 
        :dataSource="fileList" 
        :columns="columns" 
        :pagination="false"
        :scroll="{ y: 400 }"
        :style="getTableStyle()"
      >
        <template #headerCell="{ column }">
          <span :style="{ color: isDark ? '#ffffff' : '#333' }">{{ column.title }}</span>
        </template>
        
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'name'">
            <div style="display: flex; align-items: center; gap: 8px;">
              <component :is="getFileIcon(record.name)" :style="{ fontSize: '16px', color: getFileIconColor(record.name) }" />
              <span :style="{ color: isDark ? '#ffffff' : '#333' }">{{ record.name }}</span>
            </div>
          </template>
          
          <template v-else-if="column.key === 'size'">
            <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">
              {{ formatFileSize(record.size) }}
            </span>
          </template>
          
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          
          <template v-else-if="column.key === 'progress'">
            <a-progress 
              :percent="record.progress || 0" 
              :size="'small'"
              :stroke-color="isDark ? '#1890ff' : '#1890ff'"
              :trail-color="isDark ? '#434343' : '#f5f5f5'"
            />
          </template>
          
          <template v-else-if="column.key === 'entities'">
            <span :style="{ color: isDark ? '#1890ff' : '#1890ff', fontWeight: 'bold' }">
              {{ record.entitiesCount || 0 }}
            </span>
          </template>
          
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button 
                size="small" 
                @click="retryUpload(record, index)"
                v-if="record.status === 'error'"
                :style="getSmallButtonStyle()"
              >
                重试
              </a-button>
              <a-button 
                size="small" 
                @click="viewDetails(record)"
                v-if="record.status === 'done' && record.documents"
                :style="getSmallButtonStyle()"
              >
                查看详情
              </a-button>
              <a-button 
                size="small" 
                danger 
                @click="removeFile(index)"
                :style="getSmallButtonStyle()"
              >
                移除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 上传配置和上传统计 (移到后面) -->
    <div class="config-section">
      <a-row :gutter="24">
        <a-col :span="12">
          <a-card title="上传配置" :style="getCardStyle()">
            <template #title>
              <span :style="{ color: isDark ? '#ffffff' : '#333' }">上传配置</span>
            </template>
            <a-form layout="vertical">
              <a-form-item label="目标图谱">
                <template #label>
                  <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">目标图谱</span>
                </template>
                <a-select v-model:value="uploadConfig.targetGraph" :style="getSelectStyle()">
                  <a-select-option value="main">主图谱</a-select-option>
                  <a-select-option value="test">测试图谱</a-select-option>
                  <a-select-option value="backup">备份图谱</a-select-option>
                </a-select>
              </a-form-item>
              
              <a-form-item label="相似度阈值">
                <template #label>
                  <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">相似度阈值</span>
                </template>
                <a-slider 
                  v-model:value="uploadConfig.threshold" 
                  :min="0.1" 
                  :max="1.0" 
                  :step="0.05"
                  :marks="{ 0.1: '0.1', 0.5: '0.5', 0.95: '0.95', 1.0: '1.0' }"
                />
                <div :style="{ color: isDark ? '#b3b3b3' : '#666', fontSize: '12px', marginTop: '4px' }">
                  当前值: {{ uploadConfig.threshold }}
                </div>
              </a-form-item>
              
              <a-form-item label="解析模式">
                <template #label>
                  <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">解析模式</span>
                </template>
                <a-radio-group v-model:value="uploadConfig.parseMode" :style="getRadioGroupStyle()">
                  <a-radio value="auto" :style="{ color: isDark ? '#ffffff' : '#333' }">自动解析</a-radio>
                  <a-radio value="manual" :style="{ color: isDark ? '#ffffff' : '#333' }">手动配置</a-radio>
                </a-radio-group>
              </a-form-item>
              
              <a-form-item label="数据处理">
                <template #label>
                  <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">数据处理</span>
                </template>
                <a-checkbox-group v-model:value="uploadConfig.dataProcessing">
                  <div style="display: flex; flex-direction: column; gap: 8px;">
                    <a-checkbox value="deduplicate" :style="{ color: isDark ? '#ffffff' : '#333' }">
                      去重处理
                    </a-checkbox>
                    <a-checkbox value="normalize" :style="{ color: isDark ? '#ffffff' : '#333' }">
                      数据标准化
                    </a-checkbox>
                    <a-checkbox value="validate" :style="{ color: isDark ? '#ffffff' : '#333' }">
                      数据校验
                    </a-checkbox>
                  </div>
                </a-checkbox-group>
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
        
        <a-col :span="12">
          <a-card title="上传统计" :style="getCardStyle()">
            <template #title>
              <span :style="{ color: isDark ? '#ffffff' : '#333' }">上传统计</span>
            </template>
            <div class="upload-stats">
              <div class="stat-item" :style="getStatItemStyle()">
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">待上传文件</span>
                <span :style="{ color: isDark ? '#ffffff' : '#333', fontWeight: 'bold' }">
                  {{ fileList.length }}
                </span>
              </div>
              <div class="stat-item" :style="getStatItemStyle()">
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">总文件大小</span>
                <span :style="{ color: isDark ? '#ffffff' : '#333', fontWeight: 'bold' }">
                  {{ formatFileSize(totalSize) }}
                </span>
              </div>
              <div class="stat-item" :style="getStatItemStyle()">
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">上传成功</span>
                <span :style="{ color: '#52c41a', fontWeight: 'bold' }">
                  {{ uploadStats.success }}
                </span>
              </div>
              <div class="stat-item" :style="getStatItemStyle()">
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">上传失败</span>
                <span :style="{ color: '#ff4d4f', fontWeight: 'bold' }">
                  {{ uploadStats.failed }}
                </span>
              </div>
              <div class="stat-item" :style="getStatItemStyle()">
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">解析实体数</span>
                <span :style="{ color: '#1890ff', fontWeight: 'bold' }">
                  {{ uploadStats.totalEntities }}
                </span>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 上传历史 -->
    <div class="history-section" :style="getCardStyle()">
      <h3 :style="{ color: isDark ? '#ffffff' : '#333', marginBottom: '16px' }">上传历史</h3>
      <a-list
        :dataSource="uploadHistory"
        :style="getListStyle()"
      >
        <template #renderItem="{ item }">
          <a-list-item :style="getListItemStyle()">
            <a-list-item-meta>
              <template #title>
                <span :style="{ color: isDark ? '#ffffff' : '#333' }">{{ item.fileName }}</span>
              </template>
              <template #description>
                <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">
                  上传时间: {{ item.uploadTime }} | 
                  状态: {{ item.status }} | 
                  大小: {{ formatFileSize(item.size) }} |
                  实体数: {{ item.entitiesCount || 0 }}
                </span>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a :style="{ color: isDark ? '#1890ff' : '#1890ff' }" @click="viewHistoryDetails(item)">查看详情</a>
              <a :style="{ color: isDark ? '#ff4d4f' : '#ff4d4f' }" @click="deleteHistory(item)">删除</a>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </div>

    <!-- 文档详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="文档解析详情"
      :width="800"
      :footer="null"
    >
      <div v-if="selectedDocuments && selectedDocuments.length > 0">
        <h4>解析结果 (共 {{ selectedDocuments.length }} 个文档片段)</h4>
        <a-list
          :dataSource="selectedDocuments"
          :pagination="{ pageSize: 5 }"
        >
          <template #renderItem="{ item, index }">
            <a-list-item>
              <a-list-item-meta>
                <template #title>
                  文档片段 {{ index + 1 }}
                </template>
                <template #description>
                  <div>
                    <p><strong>内容:</strong> {{ item.content ? item.content.substring(0, 200) + (item.content.length > 200 ? '...' : '') : '无内容' }}</p>
                    <p><strong>来源:</strong> {{ item.source || '未知' }}</p>
                    <p><strong>类型:</strong> {{ item.type || '未知' }}</p>
                    <p><strong>创建时间:</strong> {{ item.created || '未知' }}</p>
                  </div>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </div>
      <div v-else>
        <a-empty description="暂无解析数据" />
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'
import {
  CloudUploadOutlined,
  FileTextOutlined,
  FileExcelOutlined,
  FilePdfOutlined,
  FileWordOutlined,
  FileOutlined,
  CodeOutlined
} from '@ant-design/icons-vue'

// 主题检测
const isDark = ref(false)

const detectTheme = () => {
  const bodyBg = getComputedStyle(document.body).backgroundColor
  const htmlBg = getComputedStyle(document.documentElement).backgroundColor
  
  const isDarkTheme = bodyBg === 'rgb(20, 20, 20)' || 
                     htmlBg === 'rgb(20, 20, 20)' ||
                     bodyBg === '#141414' ||
                     htmlBg === '#141414'
  
  isDark.value = isDarkTheme
}

// 响应式数据
const fileList = ref([])
const uploading = ref(false)
const uploadUrl = '/api/file/upload'  // 修改为你的后端接口地址
const detailModalVisible = ref(false)
const selectedDocuments = ref([])

const uploadConfig = ref({
  targetGraph: 'main',
  threshold: 0.95,  // 新增相似度阈值配置
  parseMode: 'auto',
  dataProcessing: ['deduplicate', 'validate']
})

const uploadStats = ref({
  success: 0,
  failed: 0,
  totalEntities: 0  // 新增总实体数统计
})

const uploadHistory = ref([])

// 计算属性
const totalSize = computed(() => {
  return fileList.value.reduce((total, file) => total + (file.size || 0), 0)
})

// 表格列配置 - 新增实体数列
const columns = [
  {
    title: '文件名',
    dataIndex: 'name',
    key: 'name',
    width: '25%'
  },
  {
    title: '大小',
    dataIndex: 'size',
    key: 'size',
    width: '15%'
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: '15%'
  },
  {
    title: '进度',
    dataIndex: 'progress',
    key: 'progress',
    width: '20%'
  },
  {
    title: '实体数',
    dataIndex: 'entities',
    key: 'entities',
    width: '10%'
  },
  {
    title: '操作',
    key: 'action',
    width: '15%'
  }
]

// 样式计算函数
const getHeaderStyle = () => ({
  padding: '24px',
  backgroundColor: isDark.value ? '#262626' : '#ffffff',
  borderBottom: `1px solid ${isDark.value ? '#434343' : '#f0f0f0'}`,
  marginBottom: '24px'
})

const getCardStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff',
  border: `1px solid ${isDark.value ? '#434343' : '#f0f0f0'}`,
  marginBottom: '24px',
  transition: 'all 0.3s ease'
})

const getDraggerStyle = () => ({
  backgroundColor: isDark.value ? '#1f1f1f' : '#fafafa',
  border: `2px dashed ${isDark.value ? '#434343' : '#d9d9d9'}`,
  borderRadius: '6px'
})

const getSelectStyle = () => ({
  backgroundColor: isDark.value ? '#1f1f1f' : '#ffffff',
  borderColor: isDark.value ? '#434343' : '#d9d9d9',
  color: isDark.value ? '#ffffff' : '#333'
})

const getRadioGroupStyle = () => ({
  color: isDark.value ? '#ffffff' : '#333'
})

const getStatItemStyle = () => ({
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  padding: '12px 0',
  borderBottom: `1px solid ${isDark.value ? '#434343' : '#f0f0f0'}`
})

const getButtonStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff',
  borderColor: isDark.value ? '#434343' : '#d9d9d9',
  color: isDark.value ? '#ffffff' : '#333'
})

const getSmallButtonStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff',
  borderColor: isDark.value ? '#434343' : '#d9d9d9',
  color: isDark.value ? '#ffffff' : '#333'
})

const getTableStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff'
})

const getListStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff'
})

const getListItemStyle = () => ({
  borderBottomColor: isDark.value ? '#434343' : '#f0f0f0'
})

// 文件处理函数
const beforeUpload = (file) => {
  const allowedTypes = [
    'text/plain',
    'text/markdown',
    'text/csv',
    'application/json',
    'application/xml',
    'text/xml',
    'application/pdf',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/msword'
  ]
  
  if (!allowedTypes.includes(file.type)) {
    message.error(`不支持的文件类型: ${file.type}`)
    return false
  }
  
  if (file.size > 50 * 1024 * 1024) {
    message.error('文件大小不能超过 50MB')
    return false
  }
  
  // 添加到文件列表
  const fileObj = {
    uid: Date.now() + Math.random(),
    name: file.name,
    size: file.size,
    status: 'ready',
    progress: 0,
    file: file,
    entitiesCount: 0,
    documents: null
  }
  
  fileList.value.push(fileObj)
  return false // 阻止自动上传
}

const handleChange = (info) => {
  // 处理文件状态变化
}

const handleRemove = (file) => {
  const index = fileList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

const removeFile = (index) => {
  fileList.value.splice(index, 1)
}

const clearAll = () => {
  fileList.value = []
  uploadStats.value = { success: 0, failed: 0, totalEntities: 0 }
  message.info('文件列表已清空')
}

const startUpload = async () => {
  if (fileList.value.length === 0) {
    message.warning('请先选择要上传的文件')
    return
  }
  
  uploading.value = true
  uploadStats.value = { success: 0, failed: 0, totalEntities: 0 }
  
  for (let i = 0; i < fileList.value.length; i++) {
    const file = fileList.value[i]
    if (file.status === 'ready' || file.status === 'error') {
      await uploadSingleFile(file, i)
    }
  }
  
  uploading.value = false
  message.success(`上传完成！成功 ${uploadStats.value.success} 个，失败 ${uploadStats.value.failed} 个，共解析 ${uploadStats.value.totalEntities} 个实体`)
}

// 修改上传单个文件的函数
const uploadSingleFile = async (fileObj, index) => {
  fileObj.status = 'uploading'
  fileObj.progress = 0
  
  // 模拟上传进度
  const progressInterval = setInterval(() => {
    if (fileObj.progress < 90) {
      fileObj.progress += Math.random() * 20
    }
  }, 200)
  
  try {
    // 创建FormData
    const formData = new FormData()
    formData.append('file', fileObj.file)
    
    // 调用后端API
    const response = await axios.post('/api/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        fileObj.progress = percentCompleted
      }
    })
    
    clearInterval(progressInterval)
    fileObj.progress = 100
    fileObj.status = 'done'
    
    // 处理后端返回的数据
    if (response.data && response.data.code === 200) {
      const documents = response.data.data || []
      fileObj.documents = documents
      fileObj.entitiesCount = documents.length
      
      uploadStats.value.success++
      uploadStats.value.totalEntities += documents.length
      
      // 添加到历史记录
      uploadHistory.value.unshift({
        id: Date.now(),
        fileName: fileObj.name,
        uploadTime: new Date().toLocaleString(),
        status: '成功',
        size: fileObj.size,
        entitiesCount: documents.length,
        documents: documents
      })
      
      message.success(`${fileObj.name} 上传成功，解析出 ${documents.length} 个文档片段`)
    } else {
      throw new Error(response.data.message || '上传失败')
    }
    
  } catch (error) {
    clearInterval(progressInterval)
    fileObj.status = 'error'
    fileObj.progress = 0
    uploadStats.value.failed++
    
    const errorMessage = error.response?.data?.message || error.message || '上传失败'
    message.error(`${fileObj.name} 上传失败: ${errorMessage}`)
    
    uploadHistory.value.unshift({
      id: Date.now(),
      fileName: fileObj.name,
      uploadTime: new Date().toLocaleString(),
      status: '失败',
      size: fileObj.size,
      entitiesCount: 0,
      error: errorMessage
    })
  }
}

const retryUpload = async (fileObj, index) => {
  await uploadSingleFile(fileObj, index)
}

// 查看文档详情
const viewDetails = (fileObj) => {
  if (fileObj.documents && fileObj.documents.length > 0) {
    selectedDocuments.value = fileObj.documents
    detailModalVisible.value = true
  } else {
    message.info('暂无解析数据')
  }
}

const viewHistoryDetails = (item) => {
  if (item.documents && item.documents.length > 0) {
    selectedDocuments.value = item.documents
    detailModalVisible.value = true
  } else {
    message.info('暂无解析数据')
  }
}

// 工具函数
const formatFileSize = (size) => {
  if (size === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(size) / Math.log(k))
  return parseFloat((size / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileIcon = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase()
  const iconMap = {
    'txt': FileTextOutlined,
    'md': FileTextOutlined,
    'csv': FileExcelOutlined,
    'xlsx': FileExcelOutlined,
    'xls': FileExcelOutlined,
    'pdf': FilePdfOutlined,
    'doc': FileWordOutlined,
    'docx': FileWordOutlined,
    'json': CodeOutlined,
    'xml': CodeOutlined
  }
  return iconMap[ext] || FileOutlined
}

const getFileIconColor = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase()
  const colorMap = {
    'txt': '#1890ff',
    'md': '#1890ff',
    'csv': '#52c41a',
    'xlsx': '#52c41a',
    'xls': '#52c41a',
    'pdf': '#ff4d4f',
    'doc': '#1890ff',
    'docx': '#1890ff',
    'json': '#faad14',
    'xml': '#faad14'
  }
  return colorMap[ext] || '#666'
}

const getStatusColor = (status) => {
  const colorMap = {
    'ready': 'default',
    'uploading': 'processing',
    'done': 'success',
    'error': 'error'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    'ready': '准备中',
    'uploading': '上传中',
    'done': '已完成',
    'error': '上传失败'
  }
  return textMap[status] || '未知'
}

const deleteHistory = (item) => {
  const index = uploadHistory.value.findIndex(h => h.id === item.id)
  if (index > -1) {
    uploadHistory.value.splice(index, 1)
    message.success('历史记录已删除')
  }
}

onMounted(() => {
  detectTheme()
  
  const observer = new MutationObserver(() => {
    detectTheme()
  })
  
  observer.observe(document.body, {
    attributes: true,
    attributeFilter: ['style']
  })
  
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['style']
  })
})
</script>

<style scoped>
.upload-page {
  padding: 0;
  transition: background-color 0.3s ease;
}

.upload-section {
  padding: 24px;
}

.upload-content {
  padding: 40px 20px;
}

.config-section {
  margin-bottom: 24px;
}

.file-list-section {
  padding: 24px;
}

.upload-stats {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.stat-item:last-child {
  border-bottom: none !important;
}

.history-section {
  padding: 24px;
}

/* Ant Design 组件样式覆盖 */
:deep(.ant-upload-drag) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#fafafa"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-upload-drag:hover) {
  border-color: #1890ff !important;
}

:deep(.ant-upload-drag.ant-upload-drag-hover) {
  border-color: #1890ff !important;
  background-color: v-bind('isDark ? "rgba(24, 144, 255, 0.1)" : "rgba(24, 144, 255, 0.02)"') !important;
}

:deep(.ant-card) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#f0f0f0"') !important;
}

:deep(.ant-card-head) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
  border-bottom-color: v-bind('isDark ? "#434343" : "#f0f0f0"') !important;
}

:deep(.ant-card-body) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
}

:deep(.ant-table) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
}

:deep(.ant-table-thead > tr > th) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#fafafa"') !important;
  border-bottom-color: v-bind('isDark ? "#434343" : "#f0f0f0"') !important;
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-table-tbody > tr > td) {
  border-bottom-color: v-bind('isDark ? "#434343" : "#f0f0f0"') !important;
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background-color: v-bind('isDark ? "#434343" : "#f5f5f5"') !important;
}

:deep(.ant-select-selector) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-select-selection-item) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-radio-wrapper) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-checkbox-wrapper) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-checkbox-inner) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
}

:deep(.ant-btn:not(.ant-btn-primary)) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-btn:not(.ant-btn-primary):hover) {
  background-color: v-bind('isDark ? "#434343" : "#f5f5f5"') !important;
  border-color: v-bind('isDark ? "#666" : "#1890ff"') !important;
  color: v-bind('isDark ? "#ffffff" : "#1890ff"') !important;
}

:deep(.ant-list) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
}

:deep(.ant-list-item) {
  border-bottom-color: v-bind('isDark ? "#434343" : "#f0f0f0"') !important;
}

:deep(.ant-list-item-meta-title) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-list-item-meta-description) {
  color: v-bind('isDark ? "#b3b3b3" : "#666"') !important;
}

:deep(.ant-progress-bg) {
  background-color: #1890ff !important;
}

:deep(.ant-progress-inner) {
  background-color: v-bind('isDark ? "#434343" : "#f5f5f5"') !important;
}

/* 下拉菜单样式 */
:deep(.ant-select-dropdown) {
  background-color: v-bind('isDark ? "#262626" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
}

:deep(.ant-select-item) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

:deep(.ant-select-item:hover) {
  background-color: v-bind('isDark ? "#434343" : "#f5f5f5"') !important;
}

:deep(.ant-select-item-option-selected) {
  background-color: v-bind('isDark ? "#1890ff" : "#e6f7ff"') !important;
  color: v-bind('isDark ? "#ffffff" : "#1890ff"') !important;
}
</style>