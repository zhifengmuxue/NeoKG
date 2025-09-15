<template>
  <div class="triples-table">
    <a-table
      :columns="columns"
      :data-source="triples"
      :pagination="{ pageSize: 10, showSizeChanger: true }"
      :scroll="{ y: 200 }"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'sourceEntity'">
          <div class="entity-cell">
            <div class="entity-icon">●</div>
            {{ record.source }}
          </div>
        </template>
        <template v-if="column.key === 'targetEntity'">
          <div class="entity-cell">
            <div class="entity-icon">●</div>
            {{ record.target }}
          </div>
        </template>
        <template v-if="column.key === 'relation'">
          <a-tag color="blue">{{ record.relation }}</a-tag>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
const props = defineProps({
  triples: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['tripleSelect'])

const columns = [
  {
    title: 'Source entity',
    dataIndex: 'source',
    key: 'sourceEntity',
    width: 200,
    ellipsis: true
  },
  {
    title: 'Relation',
    dataIndex: 'relation',
    key: 'relation',
    width: 150,
    filters: [
      { text: 'depends_on', value: 'depends_on' },
      { text: 'contributes_to', value: 'contributes_to' },
      { text: 'comprises', value: 'comprises' }
    ],
    onFilter: (value, record) => record.relation === value
  },
  {
    title: 'Target entity',
    dataIndex: 'target',
    key: 'targetEntity',
    ellipsis: true
  }
]
</script>

<style scoped>
.triples-table {
  height: 100%;
  overflow: auto;
}

.entity-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.entity-icon {
  color: #1890ff;
  font-size: 8px;
}
</style>