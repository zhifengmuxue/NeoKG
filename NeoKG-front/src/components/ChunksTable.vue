<template>
  <div class="chunks-container">
    <div class="chunks-table">
      <a-table
        :columns="columns"
        :data-source="chunks"
        :pagination="{ pageSize: 5 }"
        :scroll="{ y: 160 }"
        size="small"
        @row-click="handleRowClick"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'source'">
            <div class="source-info">
              <file-text-outlined />
              {{ record.source }}
            </div>
          </template>
          <template v-if="column.key === 'content'">
            <div class="content-preview">
              {{ record.content.substring(0, 100) }}...
            </div>
          </template>
        </template>
      </a-table>
    </div>
    
    <!-- 选中的文档块详情 -->
    <div v-if="selectedChunk" class="chunk-viewer">
      <div class="viewer-header">
        <div class="chunk-info">
          <h4>{{ selectedChunk.source }}</h4>
          <a-tag>{{ selectedChunk.type }}</a-tag>
        </div>
        <a-button size="small" @click="closeViewer">
          <close-outlined />
        </a-button>
      </div>
      <div class="viewer-content">
        <div class="content-text">
          {{ selectedChunk.content }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { FileTextOutlined, CloseOutlined } from '@ant-design/icons-vue'

const props = defineProps({
  chunks: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['chunkSelect'])

const selectedChunk = ref(null)

const columns = [
  {
    title: '#',
    dataIndex: 'id',
    key: 'id',
    width: 50
  },
  {
    title: 'Type',
    dataIndex: 'type',
    key: 'type',
    width: 80
  },
  {
    title: 'Source',
    dataIndex: 'source',
    key: 'source',
    width: 150
  },
  {
    title: 'Created',
    dataIndex: 'created',
    key: 'created',
    width: 100
  },
  {
    title: 'Content',
    dataIndex: 'content',
    key: 'content'
  }
]

const handleRowClick = (record) => {
  selectedChunk.value = record
  emit('chunkSelect', record)
}

const closeViewer = () => {
  selectedChunk.value = null
}
</script>

<style scoped>
.chunks-container {
  display: flex;
  height: 100%;
}

.chunks-table {
  flex: 1;
  overflow: auto;
  border-right: 1px solid #e8e8e8;
}

.chunk-viewer {
  width: 400px;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
}

.chunk-info h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
}

.viewer-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.content-text {
  line-height: 1.6;
  font-size: 13px;
  color: #333;
}

.source-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-preview {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

:deep(.ant-table-tbody > tr) {
  cursor: pointer;
}

:deep(.ant-table-tbody > tr:hover) {
  background-color: #f5f5f5 !important;
}
</style>