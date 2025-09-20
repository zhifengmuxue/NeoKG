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
        name="file"
        :multiple="true"
        :beforeUpload="beforeUpload"
        :showUploadList="false"
        :fileList="[]"
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

    <!-- 文件列表 -->
    <div class="file-list-section" :style="getCardStyle()">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h3 :style="{ color: isDark ? '#ffffff' : '#333', margin: 0 }">文件列表</h3>
        <a-space>
          <a-button 
            @click="clearAll" 
            :disabled="fileList.length === 0 || uploading || syncing"
            :style="getButtonStyle()"
          >
            清空列表
          </a-button>
          <a-button 
            type="primary" 
            @click="startUpload" 
            :loading="uploading || syncing"
            :disabled="fileList.length === 0"
          >
            {{ uploading ? '上传中...' : syncing ? '同步中...' : '开始上传' }}
          </a-button>
        </a-space>
      </div>
      
      <!-- 同步进度提示 -->
      <div v-if="syncing" style="margin-bottom: 16px;">
        <a-alert 
          message="正在同步文档到图数据库" 
          description="请稍候，正在将上传的文档和关键词同步到Neo4j图数据库中..." 
          type="info" 
          show-icon 
        />
      </div>
      
      <a-table 
        :dataSource="fileList" 
        :columns="columns" 
        :pagination="false"
        :scroll="{ y: 400 }"
        :style="getTableStyle()"
        rowKey="uid"
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

    <!-- 上传配置和上传统计 -->
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
              
              <a-form-item label="更新模式">
                <template #label>
                  <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">更新模式</span>
                </template>
                <a-radio-group v-model:value="uploadConfig.updateMode" :style="getRadioGroupStyle()">
                  <a-radio value="incremental" :style="{ color: isDark ? '#ffffff' : '#333' }">增量更新</a-radio>
                  <a-radio value="full" :style="{ color: isDark ? '#ffffff' : '#333' }">全量更新</a-radio>
                </a-radio-group>
                <div :style="{ color: isDark ? '#b3b3b3' : '#999', fontSize: '12px', marginTop: '8px' }">
                  <div v-if="uploadConfig.updateMode === 'incremental'" :style="{ color: isDark ? '#52c41a' : '#52c41a' }">
                    💡 增量更新：只添加新内容，不会删除现有数据，处理速度较快
                  </div>
                  <div v-else :style="{ color: isDark ? '#faad14' : '#faad14' }">
                    ⚠️ 全量更新：会完全替换现有数据，请谨慎使用
                  </div>
                </div>
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
                <div :style="{ color: isDark ? '#1890ff' : '#1890ff', fontSize: '11px', marginTop: '2px' }">
                  💡 较高的阈值(>0.8)会产生更精确但数量较少的关键词；较低的阈值(<0.5)会产生更多但可能不太精确的关键词
                </div>
              </a-form-item>
              
              <a-form-item label="匹配方式">
                <template #label>
                  <span :style="{ color: isDark ? '#b3b3b3' : '#666' }">匹配方式</span>
                </template>
                <a-checkbox-group v-model:value="uploadConfig.matchingMethods">
                  <div style="display: flex; flex-direction: column; gap: 8px;">
                    <a-checkbox value="stringMatch" :checked="true" :style="{ color: isDark ? '#ffffff' : '#333' }">
                      字符串匹配
                    </a-checkbox>
                    <a-checkbox value="semanticMatch" :checked="true" :style="{ color: isDark ? '#ffffff' : '#333' }">
                      语义匹配
                    </a-checkbox>
                  </div>
                </a-checkbox-group>
                <div :style="{ color: isDark ? '#b3b3b3' : '#999', fontSize: '12px', marginTop: '8px' }">
                  <div :style="{ marginBottom: '4px' }">
                    <span :style="{ color: isDark ? '#1890ff' : '#1890ff' }">字符串匹配：</span>
                    基于文本的精确匹配，速度快但灵活性较低
                  </div>
                  <div>
                    <span :style="{ color: isDark ? '#52c41a' : '#52c41a' }">语义匹配：</span>
                    基于语义理解的匹配，更智能但处理时间较长
                  </div>
                </div>
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
                  实体数: {{ item.entitiesCount || 0 }} |
                  相似度阈值: {{ item.threshold || '未知' }} |
                  更新模式: {{ getUpdateModeText(item.updateMode) }} |
                  匹配方式: {{ getMatchingMethodsText(item.matchingMethods) }}
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
const detailModalVisible = ref(false)
const selectedDocuments = ref([])
const syncing = ref(false) // 新增：同步状态

const uploadConfig = ref({
  targetGraph: 'main',
  updateMode: 'incremental', // 新增：更新模式，默认增量更新
  threshold: 0.95,
  matchingMethods: ['stringMatch', 'semanticMatch'] // 修改：匹配方式
})

const uploadStats = ref({
  success: 0,
  failed: 0,
  totalEntities: 0
})

const uploadHistory = ref([])

// 用于生成唯一ID的计数器
let fileIdCounter = 0

// 计算属性
const totalSize = computed(() => {
  return fileList.value.reduce((total, file) => total + (file.size || 0), 0)
})

// 表格列配置
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

// 生成文件唯一标识
const generateFileKey = (file) => {
  return `${file.name}_${file.size}_${file.lastModified || Date.now()}`
}

// 修复重复文件问题的核心函数
const beforeUpload = (file) => {
  console.log('beforeUpload 被调用，文件:', file.name, '大小:', file.size)
  
  // 生成文件唯一标识
  const fileKey = generateFileKey(file)
  
  // 检查文件是否已存在（更严格的检查）
  const existingFile = fileList.value.find(item => {
    const existingKey = generateFileKey(item.file)
    return existingKey === fileKey
  })
  
  if (existingFile) {
    console.log('文件已存在，跳过添加:', file.name)
    message.warning(`文件 ${file.name} 已存在，请勿重复添加`)
    return false
  }
  
  // 检查文件类型
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
  
  // 使用计数器生成唯一ID
  fileIdCounter++
  
  // 添加到文件列表
  const fileObj = {
    uid: `file_${fileIdCounter}_${Date.now()}`,
    name: file.name,
    size: file.size,
    status: 'ready',
    progress: 0,
    file: file,
    entitiesCount: 0,
    documents: null,
    fileKey: fileKey // 保存文件唯一标识
  }
  
  console.log('添加文件到列表:', fileObj.name, '唯一ID:', fileObj.uid)
  fileList.value.push(fileObj)
  
  // 延迟一下确保UI更新
  setTimeout(() => {
    console.log('当前文件列表长度:', fileList.value.length)
  }, 100)
  
  return false // 阻止自动上传
}

const removeFile = (index) => {
  console.log('removeFile 被调用，索引:', index)
  if (index >= 0 && index < fileList.value.length) {
    const removedFile = fileList.value.splice(index, 1)[0]
    console.log('移除文件:', removedFile.name)
  }
}

const clearAll = () => {
  fileList.value = []
  uploadStats.value = { success: 0, failed: 0, totalEntities: 0 }
  fileIdCounter = 0 // 重置计数器
  message.info('文件列表已清空')
}

// 新增：同步文档到图数据库
const syncDocumentsToGraph = async () => {
  if (uploadStats.value.success === 0) {
    console.log('没有成功上传的文件，跳过图谱同步')
    return
  }

  syncing.value = true
  
  try {
    console.log('开始同步文档到图数据库，更新模式:', uploadConfig.value.updateMode)
    
    // 根据更新模式决定 fullUpdate 参数
    const fullUpdate = uploadConfig.value.updateMode === 'full'
    
    const response = await axios.post('/api/graph/sync-documents', null, {
      params: {
        fullUpdate: fullUpdate
      }
    })
    
    console.log('图谱同步响应:', response.data)
    
    if (response.data && response.data.code === "SUCCESS") {
      message.success(`图谱同步完成！(${getUpdateModeText(uploadConfig.value.updateMode)})`)
    } else {
      throw new Error(response.data?.message || '图谱同步失败')
    }
    
  } catch (error) {
    console.error('图谱同步失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '图谱同步失败'
    message.error(`图谱同步失败: ${errorMessage}`)
  } finally {
    syncing.value = false
  }
}

// 修改：开始上传函数，在完成后调用图谱同步
const startUpload = async () => {
  if (fileList.value.length === 0) {
    message.warning('请先选择要上传的文件')
    return
  }
  
  uploading.value = true
  uploadStats.value = { success: 0, failed: 0, totalEntities: 0 }
  
  // 上传所有文件
  for (let i = 0; i < fileList.value.length; i++) {
    const file = fileList.value[i]
    if (file.status === 'ready' || file.status === 'error') {
      await uploadSingleFile(file, i)
    }
  }
  
  uploading.value = false
  
  // 显示上传结果
  const uploadMessage = `文件上传完成！成功 ${uploadStats.value.success} 个，失败 ${uploadStats.value.failed} 个，共解析 ${uploadStats.value.totalEntities} 个实体`
  console.log(uploadMessage)
  
  if (uploadStats.value.success > 0) {
    message.success(uploadMessage)
    
    // 上传成功后自动同步到图数据库
    message.info('正在同步文档到图数据库...')
    await syncDocumentsToGraph()
  } else {
    message.warning('没有文件上传成功，无需同步到图数据库')
  }
}

// 控制进度条在5秒内完成
const uploadSingleFile = async (fileObj, index) => {
  console.log('开始上传文件:', fileObj.name)
  fileObj.status = 'uploading'
  fileObj.progress = 0
  
  // 控制进度条在5秒内完成
  const startTime = Date.now()
  const totalDuration = 5000 // 5秒
  
  const progressInterval = setInterval(() => {
    const elapsed = Date.now() - startTime
    const progressPercentage = Math.min((elapsed / totalDuration) * 90, 90) // 最多到90%
    fileObj.progress = Math.floor(progressPercentage)
  }, 100) // 每100ms更新一次进度
  
  try {
    // 创建FormData并添加配置参数
    const formData = new FormData()
    formData.append('file', fileObj.file)
    formData.append('threshold', uploadConfig.value.threshold.toString())
    formData.append('updateMode', uploadConfig.value.updateMode) // 添加更新模式
    formData.append('matchingMethods', JSON.stringify(uploadConfig.value.matchingMethods)) // 添加匹配方式
    
    console.log('调用后端API上传文件:', fileObj.name, '配置:', {
      threshold: uploadConfig.value.threshold,
      updateMode: uploadConfig.value.updateMode,
      matchingMethods: uploadConfig.value.matchingMethods
    })
    
    // 调用后端API
    const response = await axios.post('/api/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    console.log('上传响应:', response.data)
    
    // 清除进度定时器
    clearInterval(progressInterval)
    
    // 确保进度条完成
    fileObj.progress = 100
    fileObj.status = 'done'
    
    // 修正成功判断条件：支持 code: "SUCCESS" 或 code: 200
    if (response.data && (response.data.code === "SUCCESS" || response.data.code === 200)) {
      const documents = response.data.data || []
      fileObj.documents = documents
      fileObj.entitiesCount = documents.length
      
      uploadStats.value.success++
      uploadStats.value.totalEntities += documents.length
      
      // 添加到历史记录
      uploadHistory.value.unshift({
        id: Date.now() + Math.random(),
        fileName: fileObj.name,
        uploadTime: new Date().toLocaleString(),
        status: '成功',
        size: fileObj.size,
        entitiesCount: documents.length,
        documents: documents,
        threshold: uploadConfig.value.threshold,
        updateMode: uploadConfig.value.updateMode, // 记录更新模式
        matchingMethods: uploadConfig.value.matchingMethods // 记录匹配方式
      })
      
      console.log(`${fileObj.name} 上传成功，解析出 ${documents.length} 个文档片段`)
    } else {
      throw new Error(response.data.message || '上传失败')
    }
    
  } catch (error) {
    console.error('上传失败:', error)
    
    // 清除进度定时器
    clearInterval(progressInterval)
    
    fileObj.status = 'error'
    fileObj.progress = 0
    uploadStats.value.failed++
    
    const errorMessage = error.response?.data?.message || error.message || '上传失败'
    message.error(`${fileObj.name} 上传失败: ${errorMessage}`)
    
    uploadHistory.value.unshift({
      id: Date.now() + Math.random(),
      fileName: fileObj.name,
      uploadTime: new Date().toLocaleString(),
      status: '失败',
      size: fileObj.size,
      entitiesCount: 0,
      error: errorMessage,
      threshold: uploadConfig.value.threshold,
      updateMode: uploadConfig.value.updateMode,
      matchingMethods: uploadConfig.value.matchingMethods
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

// 新增：获取更新模式文本
const getUpdateModeText = (updateMode) => {
  const textMap = {
    'incremental': '增量更新',
    'full': '全量更新'
  }
  return textMap[updateMode] || '未知'
}

// 新增：获取匹配方式文本
const getMatchingMethodsText = (matchingMethods) => {
  if (!matchingMethods || matchingMethods.length === 0) return '未设置'
  
  const methodMap = {
    'stringMatch': '字符串匹配',
    'semanticMatch': '语义匹配'
  }
  
  return matchingMethods.map(method => methodMap[method] || method).join(' + ')
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

/* 修复 Checkbox 样式 */
:deep(.ant-checkbox-wrapper) {
  color: v-bind('isDark ? "#ffffff" : "#333"') !important;
}

/* Checkbox 基本样式 */
:deep(.ant-checkbox-inner) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
  transition: all 0.3s ease !important;
}

/* Checkbox 悬浮状态 */
:deep(.ant-checkbox:hover .ant-checkbox-inner) {
  border-color: #1890ff !important;
}

/* Checkbox 选中状态 */
:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #1890ff !important;
  border-color: #1890ff !important;
}

/* Checkbox 选中状态下的勾选标记 */
:deep(.ant-checkbox-checked .ant-checkbox-inner::after) {
  border-color: #ffffff !important;
  opacity: 1 !important;
}

/* Checkbox 半选中状态 */
:deep(.ant-checkbox-indeterminate .ant-checkbox-inner) {
  background-color: #1890ff !important;
  border-color: #1890ff !important;
}

:deep(.ant-checkbox-indeterminate .ant-checkbox-inner::after) {
  background-color: #ffffff !important;
}

/* Checkbox 禁用状态 */
:deep(.ant-checkbox-wrapper-disabled .ant-checkbox-inner) {
  background-color: v-bind('isDark ? "#262626" : "#f5f5f5"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
}

:deep(.ant-checkbox-wrapper-disabled) {
  color: v-bind('isDark ? "#666666" : "#00000040"') !important;
}

/* Radio 样式修复 */
:deep(.ant-radio-inner) {
  background-color: v-bind('isDark ? "#1f1f1f" : "#ffffff"') !important;
  border-color: v-bind('isDark ? "#434343" : "#d9d9d9"') !important;
}

:deep(.ant-radio:hover .ant-radio-inner) {
  border-color: #1890ff !important;
}

:deep(.ant-radio-checked .ant-radio-inner) {
  background-color: #1890ff !important;
  border-color: #1890ff !important;
}

:deep(.ant-radio-checked .ant-radio-inner::after) {
  background-color: #ffffff !important;
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

/* Alert 组件样式 */
:deep(.ant-alert) {
  background-color: v-bind('isDark ? "#111b26" : "#e6f7ff"') !important;
  border-color: v-bind('isDark ? "#003a8c" : "#91d5ff"') !important;
}

:deep(.ant-alert-message) {
  color: v-bind('isDark ? "#ffffff" : "#003a8c"') !important;
}

:deep(.ant-alert-description) {
  color: v-bind('isDark ? "#b3b3b3" : "#666"') !important;
}

/* 滑块样式 */
:deep(.ant-slider-rail) {
  background-color: v-bind('isDark ? "#434343" : "#f5f5f5"') !important;
}

:deep(.ant-slider-track) {
  background-color: #1890ff !important;
}

:deep(.ant-slider-handle) {
  border-color: #1890ff !important;
  background-color: #ffffff !important;
}

:deep(.ant-slider-handle:hover) {
  border-color: #40a9ff !important;
}

:deep(.ant-slider-handle:focus) {
  border-color: #40a9ff !important;
  box-shadow: 0 0 0 5px rgba(24, 144, 255, 0.12) !important;
}

:deep(.ant-slider-mark-text) {
  color: v-bind('isDark ? "#b3b3b3" : "#666"') !important;
}
</style>