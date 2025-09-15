<template>
  <div class="nodes-table">
    <a-table
      :columns="columns"
      :data-source="nodes"
      :pagination="{ pageSize: 10, showSizeChanger: true }"
      :row-selection="rowSelection"
      :scroll="{ y: 200 }"
      size="small"
      @row-click="handleRowClick"
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'nodeType'">
          <div class="node-type">
            <div 
              class="type-indicator" 
              :style="{ backgroundColor: getTypeColor(record.type) }"
            ></div>
            {{ record.type }}
          </div>
        </template>
        <template v-if="column.key === 'node'">
          <div class="node-info">
            <div class="node-label">{{ record.label }}</div>
            <div class="node-id">{{ record.id }}</div>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  nodes: {
    type: Array,
    default: () => []
  },
  selectedNode: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['nodeSelect'])

const columns = [
  {
    title: 'Node type',
    dataIndex: 'type',
    key: 'nodeType',
    width: 200,
    filters: [
      { text: 'BusinessSegments', value: 'BusinessSegments' },
      { text: 'ProductsAndServices', value: 'ProductsAndServices' },
      { text: 'OperationalInfrastructure', value: 'OperationalInfrastructure' }
    ],
    onFilter: (value, record) => record.type === value
  },
  {
    title: 'Node',
    dataIndex: 'label',
    key: 'node',
    ellipsis: true
  }
]

const rowSelection = {
  type: 'radio',
  selectedRowKeys: computed(() => 
    props.selectedNode ? [props.selectedNode.id] : []
  ),
  onSelect: (record) => {
    emit('nodeSelect', record)
  }
}

const getTypeColor = (type) => {
  const colorMap = {
    'BusinessSegments': '#52c41a',
    'CorporateStructure': '#1890ff',
    'FinancialPerformance': '#722ed1',
    'FutureOutlook': '#fa8c16',
    'OperationalInfrastructure': '#f5222d',
    'ProductsAndServices': '#13c2c2',
    'RevenueStreams': '#eb2f96',
    'RiskFactors': '#faad14'
  }
  return colorMap[type] || '#666'
}

const handleRowClick = (record) => {
  emit('nodeSelect', record)
}
</script>

<style scoped>
.nodes-table {
  height: 100%;
  overflow: auto;
}

.node-type {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.node-info {
  display: flex;
  flex-direction: column;
}

.node-label {
  font-weight: 500;
  font-size: 13px;
}

.node-id {
  font-size: 11px;
  color: #666;
}

:deep(.ant-table-tbody > tr) {
  cursor: pointer;
}

:deep(.ant-table-tbody > tr:hover) {
  background-color: #f5f5f5 !important;
}
</style>