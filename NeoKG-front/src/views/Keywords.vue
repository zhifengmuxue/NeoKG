<template>
  <div class="keywords-container">
    <a-card :bordered="false" :style="{ background: themeStyles.contentBg }">
      <template #title>
        <div class="header-title" :style="{ color: themeStyles.textColor }">
          <tags-outlined style="margin-right: 8px;" />
          关键词管理
        </div>
      </template>

      <template #extra>
        <a-button type="primary" @click="showAddModal">
          <plus-outlined />
          添加关键词
        </a-button>
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
              placeholder="搜索关键词"
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
            <a-button @click="handleRefresh" :style="{ borderColor: themeStyles.borderColor }">
              <reload-outlined />
              刷新
            </a-button>
          </a-col>
          <a-col :span="10">
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

      <!-- 关键词表格 -->
      <a-table
        :columns="columns"
        :data-source="filteredKeywords"
        :pagination="paginationConfig"
        :loading="loading"
        :row-selection="rowSelection"
        row-key="id"
        :scroll="{ x: 800 }"
        @change="handleTableChange"
        :style="{ 
          backgroundColor: themeStyles.tableBg,
          color: themeStyles.textColor
        }"
        class="custom-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a-tag color="blue">{{ record.name }}</a-tag>
          </template>
          
          <template v-else-if="column.key === 'alias'">
            <div v-if="record.alias && record.alias.length > 0">
              <a-tag 
                v-for="aliasItem in record.alias" 
                :key="aliasItem" 
                color="orange"
                style="margin: 2px;"
              >
                {{ aliasItem }}
              </a-tag>
            </div>
            <span v-else :style="{ color: themeStyles.placeholderColor }">暂无别名</span>
          </template>
          
          <template v-else-if="column.key === 'createdAt'">
            <span :style="{ color: themeStyles.textColor }">{{ formatDate(record.createdAt) }}</span>
          </template>
          
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showEditModal(record)">
                <edit-outlined />
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除这个关键词吗？"
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

    <!-- 添加/编辑关键词模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEditing ? '编辑关键词' : '添加关键词'"
      ok-text="确定"
      cancel-text="取消"
      @ok="handleSubmit"
      @cancel="handleCancel"
      width="600px"
      class="custom-modal"
      :style="{ 
        color: themeStyles.textColor
      }"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="rules"
        layout="vertical"
        :style="{ backgroundColor: themeStyles.modalBg }"
      >
        <a-form-item label="关键词名称" name="name">
          <a-input 
            v-model:value="form.name" 
            placeholder="请输入关键词名称"
            :style="{ 
              backgroundColor: themeStyles.inputBg,
              borderColor: themeStyles.borderColor,
              color: themeStyles.textColor
            }"
          />
        </a-form-item>
        
        <a-form-item label="描述" name="description">
          <a-textarea
            v-model:value="form.description"
            placeholder="请输入关键词描述"
            :rows="3"
            :style="{ 
              backgroundColor: themeStyles.inputBg,
              borderColor: themeStyles.borderColor,
              color: themeStyles.textColor
            }"
          />
        </a-form-item>

        <a-form-item label="别名" name="alias">
          <div style="margin-bottom: 8px;">
            <a-input
              v-model:value="aliasInput"
              placeholder="输入别名后按回车添加"
              @press-enter="addAlias"
              style="margin-bottom: 8px;"
              :style="{ 
                backgroundColor: themeStyles.inputBg,
                borderColor: themeStyles.borderColor,
                color: themeStyles.textColor
              }"
            />
          </div>
          <div v-if="form.alias && form.alias.length > 0">
            <a-tag
              v-for="(aliasItem, index) in form.alias"
              :key="index"
              closable
              @close="removeAlias(index)"
              style="margin: 2px;"
            >
              {{ aliasItem }}
            </a-tag>
          </div>
          <div v-else :style="{ color: themeStyles.placeholderColor, fontSize: '12px' }">
            暂无别名，可以添加关键词的同义词或相关词汇
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  TagsOutlined,
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { isDarkMode } from '@/stores/theme'
import axios from 'axios'

// 主题样式 - 扩展更多样式配置
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
const keywords = ref([])
const searchText = ref('')
const selectedRowKeys = ref([])
const modalVisible = ref(false)
const isEditing = ref(false)
const currentKeyword = ref(null)
const formRef = ref()
const aliasInput = ref('')

// 分页状态
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表单数据
const form = reactive({
  name: '',
  description: '',
  alias: []
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入关键词名称', trigger: 'blur' },
    { min: 1, max: 100, message: '关键词名称长度为1-100个字符', trigger: 'blur' }
  ]
}

// 表格列定义 - 更新字段名以匹配后端响应
const columns = [
  {
    title: '关键词名称',
    dataIndex: 'name',
    key: 'name',
    width: 180,
    sorter: (a, b) => a.name.localeCompare(b.name)
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true,
    width: 250
  },
  {
    title: '别名',
    dataIndex: 'alias',
    key: 'alias',
    width: 200,
    ellipsis: true
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
    width: 150,
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
    selectedRowKeys.value = keys
  }
}))

// 计算属性
const hasSelected = computed(() => selectedRowKeys.value.length > 0)

const filteredKeywords = computed(() => {
  let result = keywords.value

  // 搜索过滤（前端搜索）
  if (searchText.value) {
    const searchLower = searchText.value.toLowerCase()
    result = result.filter(keyword => {
      // 搜索名称、描述和别名
      const nameMatch = keyword.name && keyword.name.toLowerCase().includes(searchLower)
      const descMatch = keyword.description && keyword.description.toLowerCase().includes(searchLower)
      const aliasMatch = keyword.alias && keyword.alias.some(alias => 
        alias.toLowerCase().includes(searchLower)
      )
      return nameMatch || descMatch || aliasMatch
    })
  }

  return result
})

// API 配置
const API_BASE_URL = import.meta.env.DEV ? '/api/keyword' : 'http://localhost:8080/api/keyword'

// API 方法
const keywordAPI = {
  // 获取关键词列表（分页）
  async getKeywordsPage(currentPage = 1, pageSize = 10) {
    console.log('正在请求:', `${API_BASE_URL}/page`, { currentPage, pageSize })
    try {
      const response = await axios.get(`${API_BASE_URL}/page`, {
        params: { currentPage, pageSize }
      })
      console.log('API响应:', response)
      return response.data
    } catch (error) {
      console.error('API请求失败:', error)
      if (error.response) {
        console.error('响应状态:', error.response.status)
        console.error('响应数据:', error.response.data)
      }
      throw error
    }
  },

  // 创建关键词
  async createKeyword(data) {
    const response = await axios.post(`${API_BASE_URL}`, data)
    return response.data
  },

  // 更新关键词
  async updateKeyword(id, data) {
    const response = await axios.put(`${API_BASE_URL}/${id}`, data)
    return response.data
  },

  // 删除关键词
  async deleteKeyword(id) {
    const response = await axios.delete(`${API_BASE_URL}/${id}`)
    return response.data
  },

  // 批量删除关键词
  async batchDeleteKeywords(ids) {
    const response = await axios.delete(`${API_BASE_URL}/batch`, {
      data: { ids }
    })
    return response.data
  }
}

// 方法
const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const loadKeywords = async () => {
  loading.value = true
  try {
    console.log('开始加载关键词数据...')
    const result = await keywordAPI.getKeywordsPage(pagination.current, pagination.pageSize)
    console.log('后端返回结果:', result)
    
    // 根据您的实际响应结构处理数据
    if (result.code === 'SUCCESS') {
      const pageData = result.data
      keywords.value = pageData.records || []
      pagination.total = pageData.total || 0
      pagination.current = pageData.current || pagination.current
      pagination.pageSize = pageData.size || pagination.pageSize
      
      console.log('关键词列表加载成功:', keywords.value.length, '条数据')
      message.success('数据加载成功')
    } else {
      throw new Error(result.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载关键词列表失败:', error)
    
    // 显示具体的错误信息
    let errorMsg = '加载关键词列表失败'
    if (error.response) {
      errorMsg += `: ${error.response.status} - ${error.response.data?.message || error.response.statusText}`
    } else if (error.message) {
      errorMsg += `: ${error.message}`
    }
    message.error(errorMsg)
    
    // 使用模拟数据进行测试
    console.log('使用模拟数据...')
    keywords.value = [
      {
        id: 1,
        name: 'Vue.js',
        description: '前端框架，用于构建用户界面',
        alias: ['Vue', '前端框架'],
        createdAt: '2024-01-15T10:30:00Z',
        updatedAt: '2024-01-15T10:30:00Z'
      },
      {
        id: 2,
        name: '数据分析',
        description: '通过数据挖掘获得业务洞察',
        alias: ['数据挖掘', '业务分析'],
        createdAt: '2024-01-16T14:20:00Z',
        updatedAt: '2024-01-16T14:20:00Z'
      }
    ]
    pagination.total = keywords.value.length
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag, filters, sorter) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadKeywords()
}

const showAddModal = () => {
  isEditing.value = false
  currentKeyword.value = null
  form.name = ''
  form.description = ''
  form.alias = []
  aliasInput.value = ''
  modalVisible.value = true
}

const showEditModal = (keyword) => {
  isEditing.value = true
  currentKeyword.value = keyword
  form.name = keyword.name || ''
  form.description = keyword.description || ''
  form.alias = keyword.alias || []
  aliasInput.value = ''
  modalVisible.value = true
}

const addAlias = () => {
  const alias = aliasInput.value.trim()
  if (alias && !form.alias.includes(alias)) {
    form.alias.push(alias)
    aliasInput.value = ''
  }
}

const removeAlias = (index) => {
  form.alias.splice(index, 1)
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    const submitData = {
      name: form.name,
      description: form.description,
      alias: form.alias
    }

    if (isEditing.value && currentKeyword.value) {
      // 编辑关键词
      try {
        const result = await keywordAPI.updateKeyword(currentKeyword.value.id, submitData)
        
        if (result.code === 'SUCCESS') {
          message.success('关键词更新成功')
          // 重新加载当前页数据
          loadKeywords()
        } else {
          throw new Error(result.message || '更新失败')
        }
      } catch (error) {
        console.error('更新关键词失败:', error)
        message.error('更新关键词失败: ' + (error.message || '网络错误'))
        return
      }
    } else {
      // 添加关键词
      try {
        const result = await keywordAPI.createKeyword(submitData)
        
        if (result.code === 'SUCCESS') {
          message.success('关键词添加成功')
          // 重新加载第一页数据
          pagination.current = 1
          loadKeywords()
        } else {
          throw new Error(result.message || '添加失败')
        }
      } catch (error) {
        console.error('添加关键词失败:', error)
        message.error('添加关键词失败: ' + (error.message || '网络错误'))
        return
      }
    }
    
    modalVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
  aliasInput.value = ''
}

const handleDelete = async (id) => {
  try {
    const result = await keywordAPI.deleteKeyword(id)
    
    if (result.code === 'SUCCESS') {
      message.success('关键词删除成功')
      // 如果当前页没有数据了，回到上一页
      if (keywords.value.length === 1 && pagination.current > 1) {
        pagination.current = pagination.current - 1
      }
      loadKeywords()
    } else {
      throw new Error(result.message || '删除失败')
    }
  } catch (error) {
    console.error('删除关键词失败:', error)
    message.error('删除关键词失败: ' + (error.message || '网络错误'))
  }
}

const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) return
  
  try {
    const result = await keywordAPI.batchDeleteKeywords(selectedRowKeys.value)
    
    if (result.code === 'SUCCESS') {
      const deletedCount = selectedRowKeys.value.length
      selectedRowKeys.value = []
      message.success(`成功删除 ${deletedCount} 个关键词`)
      
      // 重新加载数据
      loadKeywords()
    } else {
      throw new Error(result.message || '批量删除失败')
    }
  } catch (error) {
    console.error('批量删除失败:', error)
    message.error('批量删除失败: ' + (error.message || '网络错误'))
  }
}

const handleSearch = () => {
  // 搜索逻辑在计算属性中处理（前端搜索）
  // 如果需要后端搜索，可以在这里调用API
}

const handleRefresh = () => {
  loadKeywords()
}

// 生命周期
onMounted(() => {
  loadKeywords()
})
</script>

<style scoped>
.keywords-container {
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

/* 深度样式，适配暗夜模式 */
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

/* 表格复选框样式 */
:deep(.ant-checkbox-wrapper) {
  color: v-bind('themeStyles.textColor') !important;
}

:deep(.ant-checkbox-inner) {
  background: v-bind('themeStyles.inputBg') !important;
  border-color: v-bind('themeStyles.borderColor') !important;
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
</style>