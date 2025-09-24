<template>
  <div class="documents-container">
    <a-card :bordered="false" :style="{ background: themeStyles.contentBg }">
      <template #title>
        <div class="header-title" :style="{ color: themeStyles.textColor }">
          <file-text-outlined style="margin-right: 8px;" />
          文档管理
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-section" :style="{ 
        background: themeStyles.searchSectionBg,
        borderColor: themeStyles.borderColor 
      }">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-input
              v-model:value="searchText"
              placeholder="搜索文档标题或内容"
              allow-clear
              @change="handleSearch"
              :style="{ 
                backgroundColor: themeStyles.inputBg,
                borderColor: themeStyles.borderColor,
                color: themeStyles.textColor
              }"
            >
              <template #prefix>
                <search-outlined :style="{ color: themeStyles.iconColor }" />
              </template>
            </a-input>
          </a-col>
          <a-col :span="6">
            <a-select
              v-model:value="typeFilter"
              placeholder="文档类型"
              allow-clear
              :style="{ 
                backgroundColor: themeStyles.inputBg,
                borderColor: themeStyles.borderColor,
                color: themeStyles.textColor,
                width: '100%'
              }"
            >
              <a-select-option value="">全部类型</a-select-option>
              <a-select-option value="CSV">CSV</a-select-option>
              <a-select-option value="TXT">TXT</a-select-option>
              <a-select-option value="MD">Markdown</a-select-option>
              <a-select-option value="PDF">PDF</a-select-option>
              <a-select-option value="DOCX">DOCX</a-select-option>
              <a-select-option value="JSON">JSON</a-select-option>
              <a-select-option value="XML">XML</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-button @click="handleRefresh" :style="{ borderColor: themeStyles.borderColor }">
              <reload-outlined />
              刷新
            </a-button>
          </a-col>
          <a-col :span="6">
            <a-space>
              <a-button 
                @click="handleBatchDelete" 
                :disabled="!hasSelected" 
                danger
                :style="{ borderColor: themeStyles.borderColor }"
              >
                <delete-outlined />
                批量删除
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>

      <!-- 文档表格 -->
      <a-table
        :columns="columns"
        :data-source="filteredDocuments"
        :pagination="paginationConfig"
        :loading="loading"
        :row-selection="rowSelection"
        :row-key="getRowKey"
        :scroll="{ x: 800 }"
        @change="handleTableChange"
        :style="{ 
          backgroundColor: themeStyles.tableBg,
          color: themeStyles.textColor
        }"
        class="custom-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <div class="file-cell">
              <!-- 删除图标，只显示文本链接 -->
              <a @click="viewDocument(record)" :title="record.title">
                {{ truncateText(record.title, 30) }}
              </a>
            </div>
          </template>
          
          <template v-else-if="column.key === 'content'">
            <div :title="record.content" :style="{ color: themeStyles.textColor }">
              {{ truncateText(record.content, 50) }}
            </div>
          </template>
          
          <template v-else-if="column.key === 'type'">
            <a-tag :color="getTypeColor(record.type)">
              {{ getDisplayType(record.type) }}
            </a-tag>
          </template>
          
          <template v-else-if="column.key === 'createdAt'">
            <span :style="{ color: themeStyles.textColor }">{{ formatDate(record.createdAt) }}</span>
          </template>
          
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewDocument(record)">
                <eye-outlined />
                查看
              </a-button>
              <a-button type="link" size="small" @click="showEditModal(record)">
                <edit-outlined />
                编辑
              </a-button>
              <a-popconfirm
                :title="`确定要删除文档 '${truncateText(record.title, 20)}' 吗？ID: ${safeStringifyId(record.id)}`"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>
                  <delete-outlined />
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 查看文档详情弹窗 -->
    <a-modal
      v-model:open="viewModalVisible"
      title="文档详情"
      :width="800"
      :footer="null"
      class="custom-modal"
      :style="{ color: themeStyles.textColor }"
    >
      <div v-if="selectedDocument" :style="{ backgroundColor: themeStyles.modalBg }">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="文档ID">
            {{ selectedDocument.id }}
          </a-descriptions-item>
          <a-descriptions-item label="文档标题">
            {{ selectedDocument.title || '未知' }}
          </a-descriptions-item>
          <a-descriptions-item label="文档类型">
            <a-tag :color="getTypeColor(selectedDocument.type)">
              {{ getDisplayType(selectedDocument.type) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatDate(selectedDocument.createdAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ formatDate(selectedDocument.updatedAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="关键词">
            {{ selectedDocument.keywords || '无' }}
          </a-descriptions-item>
        </a-descriptions>
        
        <div class="content-section">
          <h4 :style="{ color: themeStyles.textColor }">文档内容</h4>
          <div class="document-content" :style="{ 
            backgroundColor: themeStyles.inputBg,
            borderColor: themeStyles.borderColor,
            color: themeStyles.textColor
          }">
            {{ selectedDocument.content || '无内容' }}
          </div>
        </div>
        
        <div v-if="selectedDocument.vec" class="vector-section">
          <h4 :style="{ color: themeStyles.textColor }">向量信息</h4>
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="向量维度">
              {{ selectedDocument.vec ? selectedDocument.vec.length : 0 }}
            </a-descriptions-item>
            <a-descriptions-item label="向量预览">
              {{ getVectorPreview(selectedDocument.vec) }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </div>
    </a-modal>

    <!-- 编辑文档弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      title="编辑文档"
      ok-text="确定"
      cancel-text="取消"
      @ok="handleSubmit"
      @cancel="handleCancel"
      width="600px"
      class="custom-modal"
      :style="{ color: themeStyles.textColor }"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="rules"
        layout="vertical"
        :style="{ backgroundColor: themeStyles.modalBg }"
      >
        <a-form-item label="文档标题" name="title">
          <a-input 
            v-model:value="form.title" 
            placeholder="请输入文档标题"
            :style="{ 
              backgroundColor: themeStyles.inputBg,
              borderColor: themeStyles.borderColor,
              color: themeStyles.textColor
            }"
          />
        </a-form-item>
        
        <a-form-item label="文档类型" name="type">
          <a-select
            v-model:value="form.type"
            placeholder="选择文档类型"
            :style="{ 
              backgroundColor: themeStyles.inputBg,
              borderColor: themeStyles.borderColor,
              color: themeStyles.textColor,
              width: '100%'
            }"
          >
            <a-select-option value="CSV">CSV</a-select-option>
            <a-select-option value="TXT">TXT</a-select-option>
            <a-select-option value="MD">Markdown</a-select-option>
            <a-select-option value="PDF">PDF</a-select-option>
            <a-select-option value="DOCX">DOCX</a-select-option>
            <a-select-option value="JSON">JSON</a-select-option>
            <a-select-option value="XML">XML</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="文档内容" name="content">
          <a-textarea
            v-model:value="form.content"
            placeholder="请输入文档内容"
            :rows="8"
            show-count
            :maxlength="10000"
            :style="{ 
              backgroundColor: themeStyles.inputBg,
              borderColor: themeStyles.borderColor,
              color: themeStyles.textColor
            }"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  FileTextOutlined,
  FilePdfOutlined,
  FileWordOutlined,
  FileMarkdownOutlined,
  FileOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'
import { isDarkMode } from '@/stores/theme'
import axios from 'axios'
import { safeStringifyId, safeJsonParse } from '@/utils/id'

// 创建自定义axios实例，处理响应中的大整数
const apiClient = axios.create({
  transformResponse: [function (data) {
    if (typeof data === 'string') {
      try {
        return safeJsonParse(data)
      } catch (e) {
        return data
      }
    }
    return data
  }]
})

// 主题样式
const themeStyles = computed(() => {
  if (isDarkMode.value) {
    return {
      contentBg: '#1f1f1f',
      cardBg: '#262626',
      textColor: '#ffffff',
      searchSectionBg: '#262626',
      inputBg: '#1f1f1f',
      tableBg: '#1f1f1f',
      modalBg: '#1f1f1f',
      borderColor: '#434343',
      iconColor: '#ffffff',
      placeholderColor: '#999999',
      tableHeaderBg: '#262626',
      tableRowBg: '#1f1f1f',
      tableRowHoverBg: '#262626'
    }
  } else {
    return {
      contentBg: '#ffffff',
      cardBg: '#ffffff',
      textColor: '#000000',
      searchSectionBg: '#fafafa',
      inputBg: '#ffffff',
      tableBg: '#ffffff',
      modalBg: '#ffffff',
      borderColor: '#d9d9d9',
      iconColor: '#666666',
      placeholderColor: '#999999',
      tableHeaderBg: '#fafafa',
      tableRowBg: '#ffffff',
      tableRowHoverBg: '#f5f5f5'
    }
  }
})

// 状态管理
const loading = ref(false)
const documents = ref([])
const searchText = ref('')
const typeFilter = ref('')
const selectedRowKeys = ref([])
const modalVisible = ref(false)
const viewModalVisible = ref(false)
const currentDocument = ref(null)
const selectedDocument = ref(null)
const formRef = ref()

// 分页状态
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表单数据
const form = reactive({
  id: null,
  title: '',
  type: '',
  content: '',
  keywords: ''
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入文档标题', trigger: 'blur' },
    { min: 1, max: 500, message: '文档标题长度为1-500个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入文档内容', trigger: 'blur' },
    { min: 1, max: 10000, message: '文档内容长度为1-10000个字符', trigger: 'blur' }
  ]
}

// 表格列定义
const columns = [
  {
    title: '文档标题',
    dataIndex: 'title',
    key: 'title',
    width: 250,
    ellipsis: true
  },
  {
    title: '内容预览',
    dataIndex: 'content',
    key: 'content',
    ellipsis: true,
    width: 300
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 180,
    sorter: (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right'
  }
]

// 分页配置
const paginationConfig = computed(() => ({
  current: pagination.current,
  pageSize: pagination.pageSize,
  total: pagination.total,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`,
  pageSizeOptions: ['10', '20', '50', '100']
}))

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys.map(key => safeStringifyId(key))
    console.log('选中的行keys:', selectedRowKeys.value)
  }
}))

// 计算属性
const hasSelected = computed(() => selectedRowKeys.value.length > 0)

const filteredDocuments = computed(() => {
  let result = documents.value

  if (searchText.value) {
    const searchLower = searchText.value.toLowerCase()
    result = result.filter(doc => {
      const titleMatch = doc.title && doc.title.toLowerCase().includes(searchLower)
      const contentMatch = doc.content && doc.content.toLowerCase().includes(searchLower)
      return titleMatch || contentMatch
    })
  }

  if (typeFilter.value) {
    result = result.filter(doc => {
      // 支持多种Markdown类型的匹配
      if (typeFilter.value === 'MD' || typeFilter.value === 'MARKDOWN') {
        return doc.type === 'MD' || doc.type === 'MARKDOWN' || doc.type === 'md' || doc.type === 'markdown'
      }
      return doc.type === typeFilter.value
    })
  }

  return result
})

const API_BASE_URL = '/api/file'

// API 方法
const documentAPI = {
  async getDocumentsPage(currentPage = 1, pageSize = 10) {
    console.log('正在请求:', `${API_BASE_URL}/page`, { currentPage, pageSize })
    try {
      const response = await apiClient.get(`${API_BASE_URL}/page`, {
        params: { currentPage, pageSize }
      })
      console.log('API响应:', response)
      return response.data
    } catch (error) {
      console.error('API请求失败:', error)
      throw error
    }
  },

  async updateDocument(document) {
    const safeDocument = {
      ...document,
      id: safeStringifyId(document.id)
    }
    const response = await apiClient.put(`${API_BASE_URL}`, safeDocument)
    return response.data
  },

  async deleteDocument(id) {
    const safeId = safeStringifyId(id)
    console.log('删除文档 - 原始ID:', id, '类型:', typeof id)
    console.log('删除文档 - 安全ID:', safeId, '类型:', typeof safeId)
    
    const response = await apiClient.delete(`${API_BASE_URL}/${safeId}`)
    return response.data
  },

  async batchDeleteDocuments(ids) {
    const safeIds = ids.map(id => safeStringifyId(id))
    console.log('批量删除 - 原始ID列表:', ids)
    console.log('批量删除 - 安全ID列表:', safeIds)
    
    const response = await apiClient.delete(`${API_BASE_URL}/batch`, {
      data: safeIds
    })
    return response.data
  },

  async getDocumentCount() {
    const response = await apiClient.get(`${API_BASE_URL}/num`)
    return response.data
  },

  // 修复：同步文档到图谱 - 添加 fullUpdate 参数
  async syncDocumentsToGraph(fullUpdate = true) {
    console.log('正在同步文档到图谱...', { fullUpdate })
    try {
      const response = await apiClient.post('/api/graph/sync-documents', null, {
        params: { fullUpdate }
      })
      console.log('文档同步响应:', response.data)
      return response.data
    } catch (error) {
      console.error('文档同步失败:', error)
      throw error
    }
  }
}

// 方法
const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const truncateText = (text, maxLength) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const getFileType = (fileName) => {
  if (!fileName) return 'unknown'
  const ext = fileName.split('.').pop()?.toLowerCase()
  return ext || 'unknown'
}

const getFileIcon = (fileName) => {
  const ext = getFileType(fileName)
  const iconMap = {
    'csv': FileTextOutlined,
    'txt': FileTextOutlined,
    'md': FileMarkdownOutlined,
    'markdown': FileMarkdownOutlined,
    'pdf': FilePdfOutlined,
    'doc': FileWordOutlined,
    'docx': FileWordOutlined,
    'json': FileTextOutlined,
    'xml': FileTextOutlined
  }
  return iconMap[ext] || FileOutlined
}

const getFileIconColor = (fileName) => {
  const ext = getFileType(fileName)
  const colorMap = {
    'csv': '#52c41a',
    'txt': '#1890ff',
    'md': '#52c41a',
    'markdown': '#52c41a',
    'pdf': '#ff4d4f',
    'doc': '#1890ff',
    'docx': '#1890ff',
    'json': '#faad14',
    'xml': '#faad14'
  }
  return colorMap[ext] || '#666'
}

const getTypeColor = (type) => {
  const colorMap = {
    'CSV': 'green',
    'TXT': 'blue',
    'MD': 'green',
    'MARKDOWN': 'green',
    'md': 'green',
    'markdown': 'green',
    'PDF': 'red',
    'DOC': 'blue',
    'DOCX': 'blue',
    'JSON': 'orange',
    'XML': 'orange'
  }
  return colorMap[type || 'unknown'] || 'default'
}

const getDisplayType = (type) => {
  // 统一显示格式
  const typeMap = {
    'MD': 'Markdown',
    'MARKDOWN': 'Markdown',
    'md': 'Markdown',
    'markdown': 'Markdown',
    'CSV': 'CSV',
    'TXT': 'TXT',
    'PDF': 'PDF',
    'DOC': 'Word',
    'DOCX': 'Word',
    'JSON': 'JSON',
    'XML': 'XML'
  }
  return typeMap[type] || type || 'Unknown'
}

const getVectorPreview = (vector) => {
  if (!vector || !Array.isArray(vector)) return '无向量数据'
  const preview = vector.slice(0, 5).map(v => v.toFixed(4)).join(', ')
  return `[${preview}${vector.length > 5 ? '...' : ''}]`
}

const loadDocuments = async () => {
  loading.value = true
  try {
    console.log('开始加载文档数据...')
    const result = await documentAPI.getDocumentsPage(pagination.current, pagination.pageSize)
    console.log('后端返回结果:', result)
    
    if (result.code === 'SUCCESS') {
      const pageData = result.data
      documents.value = (pageData.records || []).map(doc => {
        const safeId = safeStringifyId(doc.id)
        console.log(`文档 ${doc.title} - 原始ID: ${doc.id}, 安全ID: ${safeId}`)
        return {
          ...doc,
          id: safeId
        }
      })
      
      pagination.total = pageData.total || 0
      pagination.current = pageData.current || pagination.current
      pagination.pageSize = pageData.size || pagination.pageSize
      
      console.log('文档列表加载成功:', documents.value.length, '条数据')
      message.success('数据加载成功')
    } else {
      throw new Error(result.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载文档列表失败:', error)
    message.error('加载文档列表失败: ' + (error.message || '网络错误'))
    documents.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag, filters, sorter) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadDocuments()
}

const viewDocument = (document) => {
  selectedDocument.value = document
  viewModalVisible.value = true
}

const showEditModal = (document) => {
  currentDocument.value = document
  form.id = document.id
  form.title = document.title || ''
  form.type = document.type || ''
  form.content = document.content || ''
  form.keywords = document.keywords || ''
  modalVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    const submitData = {
      id: form.id,
      title: form.title,
      type: form.type,
      content: form.content,
      keywords: form.keywords || null
    }

    try {
      const result = await documentAPI.updateDocument(submitData)
      
      if (result.code === 'SUCCESS') {
        message.success('文档更新成功')
        loadDocuments()
      } else {
        throw new Error(result.message || '更新失败')
      }
    } catch (error) {
      console.error('更新文档失败:', error)
      message.error('更新文档失败: ' + (error.message || '网络错误'))
      return
    }
    
    modalVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

// 修复：同步文档到图谱的方法 - 添加 fullUpdate 参数
const syncDocumentsToGraph = async (fullUpdate = true) => {
  try {
    const syncResult = await documentAPI.syncDocumentsToGraph(fullUpdate)
    
    if (syncResult.code === 'SUCCESS') {
      console.log('文档同步到图谱成功')
      message.success('图谱同步成功')
    } else {
      console.warn('文档同步到图谱失败:', syncResult.message)
      message.warning('图谱同步失败: ' + (syncResult.message || '未知错误'))
    }
  } catch (error) {
    console.error('文档同步到图谱时发生错误:', error)
    message.error('图谱同步时发生错误: ' + (error.message || '网络错误'))
  }
}

const handleDelete = async (id) => {
  console.log('删除操作开始 - 收到的ID:', id, '类型:', typeof id)
  
  try {
    const result = await documentAPI.deleteDocument(id)
    
    if (result.code === 'SUCCESS') {
      message.success('文档删除成功')
      
      // 删除成功后，立即同步图谱 (使用 fullUpdate=true)
      await syncDocumentsToGraph(true)
      
      if (documents.value.length === 1 && pagination.current > 1) {
        pagination.current = pagination.current - 1
      }
      loadDocuments()
    } else {
      throw new Error(result.message || '删除失败')
    }
  } catch (error) {
    console.error('删除文档失败:', error)
    message.error('删除文档失败: ' + (error.message || '网络错误'))
  }
}

const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) return
  
  console.log('批量删除操作 - 收到的ID列表:', selectedRowKeys.value, '类型:', selectedRowKeys.value.map(id => typeof id))
  
  try {
    const result = await documentAPI.batchDeleteDocuments(selectedRowKeys.value)
    
    if (result.code === 'SUCCESS') {
      const deletedCount = selectedRowKeys.value.length
      selectedRowKeys.value = []
      message.success(`成功删除 ${deletedCount} 个文档`)
      
      // 批量删除成功后，立即同步图谱 (使用 fullUpdate=true)
      await syncDocumentsToGraph(true)
      
      loadDocuments()
    } else {
      throw new Error(result.message || '批量删除失败')
    }
  } catch (error) {
    console.error('批量删除失败:', error)
    message.error('批量删除失败: ' + (error.message || '网络错误'))
  }
}

const handleSearch = () => {
  // 搜索逻辑在计算属性中处理
}

const handleRefresh = () => {
  loadDocuments()
}

const getRowKey = (record) => {
  const key = safeStringifyId(record.id)
  console.log('表格行key:', key, '原始record.id:', record.id)
  return key
}

// 生命周期
onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.documents-container {
  padding: 0;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}

.search-section {
  margin-bottom: 16px;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid;
}

.file-cell {
  display: flex;
  align-items: center;
}

.content-section {
  margin-top: 16px;
}

.document-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-wrap: break-word;
  border: 1px solid;
}

.vector-section {
  margin-top: 16px;
}

/* 复用 Keywords 页面的深度样式 */
:deep(.ant-table-wrapper) {
  border-radius: 6px;
}

:deep(.ant-table) {
  background: v-bind('themeStyles.tableBg') !important;
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-table-thead > tr > th) {
  background: v-bind('themeStyles.tableHeaderBg') !important;
  color: v-bind('themeStyles.textColor') !important;
  font-weight: 600;
  border-bottom: 1px solid v-bind('themeStyles.borderColor') !important;
}

:deep(.ant-table-tbody > tr > td) {
  background: v-bind('themeStyles.tableRowBg') !important;
  color: v-bind('themeStyles.textColor') !important;
  border-bottom: 1px solid v-bind('themeStyles.borderColor') !important;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: v-bind('themeStyles.tableRowHoverBg') !important;
}

:deep(.ant-table-pagination) {
  background: v-bind('themeStyles.tableBg') !important;
}

:deep(.ant-pagination-item) {
  background: v-bind('themeStyles.inputBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
}

:deep(.ant-pagination-item a) {
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-pagination-item-active) {
  border-color: #1890ff !important;
}

:deep(.ant-pagination-item-active a) {
  color: #1890ff !important;
}

:deep(.ant-select-selector) {
  background: v-bind('themeStyles.inputBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-input) {
  background: v-bind('themeStyles.inputBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-input::placeholder) {
  color: v-bind('themeStyles.placeholderColor') !important;
}

:deep(.ant-textarea) {
  background: v-bind('themeStyles.inputBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-textarea::placeholder) {
  color: v-bind('themeStyles.placeholderColor') !important;
}

/* 模态框样式 */
:deep(.ant-modal-content) {
  background: v-bind('themeStyles.modalBg') !important;
  border-radius: 8px;
}

:deep(.ant-modal-header) {
  background: v-bind('themeStyles.modalBg') !important;
  border-bottom: 1px solid v-bind('themeStyles.borderColor') !important;
}

:deep(.ant-modal-title) {
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-modal-body) {
  background: v-bind('themeStyles.modalBg') !important;
}

:deep(.ant-modal-footer) {
  background: v-bind('themeStyles.modalBg') !important;
  border-top: 1px solid v-bind('themeStyles.borderColor') !important;
}

:deep(.ant-form-item-label > label) {
  color: v-bind('themeStyles.textColor') !important;
  font-weight: 500;
}

:deep(.ant-tag) {
  margin: 2px;
}

:deep(.ant-btn) {
  border-color: v-bind('themeStyles.borderColor');
}

:deep(.ant-btn:not(.ant-btn-primary):not(.ant-btn-danger)) {
  background: v-bind('themeStyles.inputBg');
  color: v-bind('themeStyles.textColor');
}

:deep(.ant-btn:not(.ant-btn-primary):not(.ant-btn-danger):hover) {
  background: v-bind('themeStyles.tableRowHoverBg');
  border-color: #1890ff;
  color: #1890ff;
}

/* Checkbox 样式 */
:deep(.ant-checkbox-wrapper) {
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-checkbox-inner) {
  background-color: v-bind('themeStyles.inputBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
  transition: all 0.3s ease !important;
  width: 16px !important;
  height: 16px !important;
  border-radius: 2px !important;
  position: relative !important;
}

:deep(.ant-checkbox:hover .ant-checkbox-inner) {
  border-color: #1890ff !important;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #1890ff !important;
  border-color: #1890ff !important;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner::after) {
  position: absolute !important;
  top: 50% !important;
  left: 50% !important;
  display: table !important;
  width: 5.71428571px !important;
  height: 9.14285714px !important;
  border: 2px solid #ffffff !important;
  border-top: 0 !important;
  border-left: 0 !important;
  transform: translate(-50%, -65%) rotate(45deg) scale(1) !important;
  opacity: 1 !important;
  transition: all 0.2s cubic-bezier(0.12, 0.4, 0.29, 1.46) 0.1s !important;
  content: '' !important;
}

:deep(.ant-checkbox-indeterminate .ant-checkbox-inner) {
  background-color: #1890ff !important;
  border-color: #1890ff !important;
}

:deep(.ant-checkbox-indeterminate .ant-checkbox-inner::after) {
  position: absolute !important;
  top: 50% !important;
  left: 50% !important;
  width: 8px !important;
  height: 8px !important;
  background-color: #ffffff !important;
  border: 0 !important;
  transform: translate(-50%, -50%) scale(1) !important;
  opacity: 1 !important;
  content: '' !important;
}

/* 分页器样式 */
:deep(.ant-pagination-prev),
:deep(.ant-pagination-next),
:deep(.ant-pagination-jump-prev),
:deep(.ant-pagination-jump-next) {
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-pagination-options-quick-jumper input) {
  background: v-bind('themeStyles.inputBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
  color: v-bind('themeStyles.textColor') !important;
}

/* 气泡确认框样式 */
:deep(.ant-popover-inner) {
  background: v-bind('themeStyles.modalBg') !important;
}

:deep(.ant-popover-title) {
  color: v-bind('themeStyles.textColor') !important;
  border-bottom: 1px solid v-bind('themeStyles.borderColor') !important;
}

:deep(.ant-popover-inner-content) {
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-popover-arrow::before) {
  background: v-bind('themeStyles.modalBg') !important;
  border: 1px solid v-bind('themeStyles.borderColor') !important;
}

/* 下拉菜单样式 */
:deep(.ant-select-dropdown) {
  background-color: v-bind('themeStyles.modalBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
}

:deep(.ant-select-item) {
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-select-item:hover) {
  background-color: v-bind('themeStyles.tableRowHoverBg') !important;
}

:deep(.ant-select-item-option-selected) {
  background-color: v-bind('isDarkMode ? "#1890ff" : "#e6f7ff"') !important;
  color: v-bind('isDarkMode ? "#ffffff" : "#1890ff"') !important;
}

/* Descriptions 样式 */
:deep(.ant-descriptions-item-label) {
  font-weight: 600;
  background: v-bind('themeStyles.tableHeaderBg') !important;
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-descriptions-item-content) {
  background: v-bind('themeStyles.tableRowBg') !important;
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-descriptions-bordered .ant-descriptions-item) {
  border-color: v-bind('themeStyles.borderColor') !important;
}
</style>