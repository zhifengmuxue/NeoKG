<template>
  <div class="filter-panel">
    <!-- 过滤器标题 -->
    <div class="filter-header">
      <h3>Filters</h3>
      <a-button size="small" type="text" @click="resetFilters">
        重置
      </a-button>
    </div>

    <!-- 实体类型过滤 -->
    <div class="filter-section">
      <div class="section-header">
        <span>Entity types</span>
        <span class="count">{{ selectedEntityTypes.length }}/{{ entityTypes.length }}</span>
      </div>
      <div class="filter-items">
        <div v-for="type in entityTypes" :key="type.name" class="filter-item">
          <a-checkbox 
            v-model:checked="type.checked"
            @change="updateEntityTypeFilter"
          >
            <div class="type-item">
              <div class="type-color" :style="{ backgroundColor: type.color }"></div>
              <span class="type-name">{{ type.name }}</span>
              <span class="type-count">{{ type.count }}</span>
            </div>
          </a-checkbox>
        </div>
      </div>
    </div>

    <!-- 关系类型过滤 -->
    <div class="filter-section">
      <div class="section-header">
        <span>Relations</span>
        <span class="count">{{ selectedRelations.length }}/{{ relations.length }}</span>
      </div>
      <div class="filter-items">
        <div v-for="relation in relations" :key="relation.name" class="filter-item">
          <a-checkbox 
            v-model:checked="relation.checked"
            @change="updateRelationFilter"
          >
            <div class="relation-item">
              <span class="relation-name">{{ relation.name }}</span>
              <span class="relation-count">{{ relation.count }}</span>
            </div>
          </a-checkbox>
        </div>
      </div>
    </div>

    <!-- 视图选项 -->
    <div class="filter-section">
      <div class="section-header">
        <span>View options</span>
      </div>
      <div class="view-options">
        <div class="option-item">
          <a-switch v-model:checked="options.hideUnselected" size="small" />
          <span>Hide unselected nodes</span>
        </div>
        <div class="option-item">
          <a-switch v-model:checked="options.autoFitView" size="small" />
          <span>Auto-fit view to selection</span>
        </div>
        <div class="option-item">
          <a-switch v-model:checked="options.lockSelection" size="small" />
          <span>Lock selection</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'filterChange'])

// 实体类型数据
const entityTypes = ref([
  { name: 'BusinessSegments', color: '#52c41a', count: 15, checked: true },
  { name: 'CorporateStructure', color: '#1890ff', count: 8, checked: true },
  { name: 'FinancialPerformance', color: '#722ed1', count: 12, checked: true },
  { name: 'FutureOutlook', color: '#fa8c16', count: 6, checked: true },
  { name: 'OperationalInfrastructure', color: '#f5222d', count: 10, checked: false },
  { name: 'ProductsAndServices', color: '#13c2c2', count: 18, checked: true },
  { name: 'RevenueStreams', color: '#eb2f96', count: 9, checked: true },
  { name: 'RiskFactors', color: '#faad14', count: 7, checked: true }
])

// 关系类型数据
const relations = ref([
  { name: 'comprises', count: 25, checked: true },
  { name: 'contributes_to', count: 18, checked: true },
  { name: 'depends_on', count: 15, checked: true },
  { name: 'drives', count: 12, checked: true },
  { name: 'influences', count: 20, checked: true }
])

// 视图选项
const options = ref({
  hideUnselected: false,
  autoFitView: true,
  lockSelection: false
})

// 计算属性
const selectedEntityTypes = computed(() => 
  entityTypes.value.filter(type => type.checked)
)

const selectedRelations = computed(() => 
  relations.value.filter(relation => relation.checked)
)

// 更新过滤器
const updateEntityTypeFilter = () => {
  emitFilterChange()
}

const updateRelationFilter = () => {
  emitFilterChange()
}

const emitFilterChange = () => {
  const filterData = {
    entityTypes: selectedEntityTypes.value,
    relations: selectedRelations.value,
    options: options.value
  }
  emit('update:modelValue', filterData)
  emit('filterChange', filterData)
}

const resetFilters = () => {
  entityTypes.value.forEach(type => {
    type.checked = true
  })
  relations.value.forEach(relation => {
    relation.checked = true
  })
  options.value = {
    hideUnselected: false,
    autoFitView: true,
    lockSelection: false
  }
  emitFilterChange()
}

// 监听选项变化
watch(options, () => {
  emitFilterChange()
}, { deep: true })
</script>

<style scoped>
.filter-panel {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.filter-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.filter-section {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 500;
}

.count {
  font-size: 12px;
  color: #666;
}

.filter-items {
  max-height: 200px;
  overflow-y: auto;
}

.filter-item {
  margin-bottom: 8px;
}

.type-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.type-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.type-name {
  flex: 1;
  font-size: 13px;
}

.type-count {
  font-size: 12px;
  color: #666;
}

.relation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.relation-name {
  font-size: 13px;
}

.relation-count {
  font-size: 12px;
  color: #666;
}

.view-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-item span {
  font-size: 13px;
}
</style>