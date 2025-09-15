<template>
  <div class="upload-page">
    <a-row :gutter="24">
      <!-- 上传区域 -->
      <a-col :span="12">
        <a-card title="文件上传">
          <a-upload-dragger
            v-model:fileList="fileList"
            name="file"
            :multiple="true"
            action="/api/upload"
            @change="handleChange"
            @drop="handleDrop"
          >
            <p class="ant-upload-drag-icon">
              <inbox-outlined />
            </p>
            <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
            <p class="ant-upload-hint">
              支持单个或批量上传。支持格式：.txt, .pdf, .docx, .csv, .json
            </p>
          </a-upload-dragger>
          
          <a-divider />
          
          <a-form layout="vertical">
            <a-form-item label="处理选项">
              <a-checkbox-group v-model:value="processOptions">
                <a-checkbox value="extract_entities">提取实体</a-checkbox>
                <a-checkbox value="extract_relations">提取关系</a-checkbox>
                <a-checkbox value="generate_embeddings">生成向量</a-checkbox>
                <a-checkbox value="auto_merge">自动合并</a-checkbox>
              </a-checkbox-group>
            </a-form-item>
            
            <a-form-item label="目标图谱">
              <a-select v-model:value="targetGraph" placeholder="选择目标图谱">
                <a-select-option value="default">默认图谱</a-select-option>
                <a-select-option value="knowledge_base">知识库</a-select-option>
                <a-select-option value="documents">文档图谱</a-select-option>
              </a-select>
            </a-form-item>
            
            <a-form-item>
              <a-button type="primary" @click="startProcessing" :loading="processing">
                <cloud-upload-outlined />
                开始处理
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
      
      <!-- 处理状态 -->
      <a-col :span="12">
        <a-card title="处理状态">
          <div v-if="!tasks.length" class="empty-state">
            <empty-outlined style="font-size: 48px; color: #d9d9d9;" />
            <p style="color: #999; margin-top: 16px;">暂无处理任务</p>
          </div>
          
          <div v-else class="task-list">
            <div v-for="task in tasks" :key="task.id" class="task-item">
              <div class="task-header">
                <span class="task-name">{{ task.fileName }}</span>
                <a-tag :color="getStatusColor(task.status)">
                  {{ getStatusText(task.status) }}
                </a-tag>
              </div>
              
              <a-progress 
                :percent="task.progress" 
                :status="task.status === 'error' ? 'exception' : 'normal'"
                size="small"
              />
              
              <div class="task-details">
                <small style="color: #666;">
                  {{ task.message }}
                </small>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 上传历史 -->
    <a-card title="上传历史" style="margin-top: 24px;">
      <a-table
        :columns="historyColumns"
        :data-source="historyData"
        :pagination="{ pageSize: 10 }"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewResult(record)">
                查看结果
              </a-button>
              <a-button type="link" size="small" @click="deleteRecord(record)" danger>
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  InboxOutlined,
  CloudUploadOutlined,
  EmptyOutlined
} from '@ant-design/icons-vue'

const fileList = ref([])
const processOptions = ref(['extract_entities', 'extract_relations'])
const targetGraph = ref('default')
const processing = ref(false)
const tasks = ref([])
const historyData = ref([
  {
    key: '1',
    fileName: 'document1.pdf',
    uploadTime: '2024-01-15 10:30:00',
    status: 'success',
    entitiesCount: 156,
    relationsCount: 89
  },
  {
    key: '2',
    fileName: 'data.csv',
    uploadTime: '2024-01-15 09:15:00',
    status: 'success',
    entitiesCount: 234,
    relationsCount: 145
  }
])

const historyColumns = [
  {
    title: '文件名',
    dataIndex: 'fileName',
    key: 'fileName',
  },
  {
    title: '上传时间',
    dataIndex: 'uploadTime',
    key: 'uploadTime',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '实体数',
    dataIndex: 'entitiesCount',
    key: 'entitiesCount',
  },
  {
    title: '关系数',
    dataIndex: 'relationsCount',
    key: 'relationsCount',
  },
  {
    title: '操作',
    key: 'action',
  }
]

const handleChange = (info) => {
  const { status } = info.file
  if (status !== 'uploading') {
    console.log(info.file, info.fileList)
  }
  if (status === 'done') {
    message.success(`${info.file.name} 文件上传成功`)
  } else if (status === 'error') {
    message.error(`${info.file.name} 文件上传失败`)
  }
}

const handleDrop = (e) => {
  console.log('Dropped files', e.dataTransfer.files)
}

const startProcessing = () => {
  if (!fileList.value.length) {
    message.warning('请先选择要处理的文件')
    return
  }
  
  processing.value = true
  
  // 为每个文件创建处理任务
  fileList.value.forEach((file, index) => {
    const task = {
      id: Date.now() + index,
      fileName: file.name,
      status: 'processing',
      progress: 0,
      message: '正在处理...'
    }
    tasks.value.push(task)
    
    // 模拟处理进度
    simulateProgress(task)
  })
  
  setTimeout(() => {
    processing.value = false
  }, 2000)
}

const simulateProgress = (task) => {
  const interval = setInterval(() => {
    task.progress += Math.random() * 20
    if (task.progress >= 100) {
      task.progress = 100
      task.status = 'success'
      task.message = '处理完成'
      clearInterval(interval)
    }
  }, 500)
}

const getStatusColor = (status) => {
  const colorMap = {
    'processing': 'blue',
    'success': 'green',
    'error': 'red',
    'pending': 'orange'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    'processing': '处理中',
    'success': '成功',
    'error': '失败',
    'pending': '等待中'
  }
  return textMap[status] || '未知'
}

const viewResult = (record) => {
  message.info(`查看 ${record.fileName} 的处理结果`)
}

const deleteRecord = (record) => {
  const index = historyData.value.findIndex(item => item.key === record.key)
  if (index > -1) {
    historyData.value.splice(index, 1)
    message.success('删除成功')
  }
}
</script>

<style scoped>
.upload-page {
  padding: 0;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.task-list {
  max-height: 400px;
  overflow-y: auto;
}

.task-item {
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  margin-bottom: 12px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-name {
  font-weight: 500;
}

.task-details {
  margin-top: 8px;
}
</style>