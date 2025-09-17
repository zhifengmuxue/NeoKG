<template>
  <div class="query-page" :class="{ 'dark-mode': isDarkMode }">
    <div class="main-content">
      <!-- Left Sidebar - Filters -->
      <div class="sidebar" :class="{ 'dark-sidebar': isDarkMode }">
        <div class="filters-section">
          <h3>Filters</h3>
          <div class="filter-controls">
            <button class="btn-icon">⚏</button>
            <button class="btn-icon">+</button>
            <button class="btn-icon">🔍</button>
            <button class="btn-icon">⚗</button>
            <button class="btn-icon">⚙</button>
          </div>
        </div>

        <!-- Entity Types -->
        <div class="filter-group">
          <div class="filter-header">
            <span>📊</span>
            <span>Entity types</span>
            <span class="count">{{ selectedEntityCount }}/{{ entityTypes.length }}</span>
          </div>
          <div class="filter-items">
            <div class="filter-item" v-for="entityType in entityTypes" :key="entityType.name">
              <input type="checkbox" :id="entityType.name" v-model="entityType.selected">
              <label :for="entityType.name">
                <span class="color-indicator" :style="{ backgroundColor: entityType.color }"></span>
                {{ entityType.name }}
              </label>
            </div>
          </div>
        </div>

        <!-- Relations -->
        <div class="filter-group">
          <div class="filter-header">
            <span>🔗</span>
            <span>Relations</span>
            <span class="count">{{ selectedRelationCount }}/{{ relations.length }}</span>
          </div>
          <div class="filter-items">
            <div class="filter-item" v-for="relation in relations" :key="relation.name">
              <input type="checkbox" :id="relation.name" v-model="relation.selected">
              <label :for="relation.name">{{ relation.name }}</label>
            </div>
          </div>
        </div>

        <!-- View Options -->
        <div class="view-options">
          <div class="option-item">
            <input type="checkbox" id="hideUnselected" v-model="hideUnselectedNodes">
            <label for="hideUnselected">Hide unselected nodes</label>
          </div>
          <div class="option-item">
            <input type="checkbox" id="autoFit" v-model="autoFitView">
            <label for="autoFit">Auto-fit view to selection</label>
          </div>
          <div class="option-item">
            <input type="checkbox" id="lockSelection" v-model="lockSelection">
            <label for="lockSelection">Lock selection</label>
          </div>
        </div>
      </div>

      <!-- Main Graph Area -->
      <div class="graph-container" :class="{ 'dark-container': isDarkMode }">
        <div class="graph-canvas" ref="graphCanvas">
          <!-- ECharts will be rendered here -->
          <div v-if="!chartLoaded" class="loading-placeholder">
            {{ loadingMessage }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { isDarkMode } from '../stores/theme'

type EChartsOption = echarts.EChartsOption

interface RawNode {
  x: number;
  y: number;
  id: string;
  label: string;
  size: number;
  color: string;
}

interface RawEdge {
  sourceID: string;
  targetID: string;
}

interface EntityType {
  name: string;
  selected: boolean;
  color: string;
}

interface Relation {
  name: string;
  selected: boolean;
}

// 响应式数据
const hideUnselectedNodes = ref(false)
const autoFitView = ref(false)
const lockSelection = ref(false)
const myChart = ref<echarts.ECharts | null>(null)
const chartLoaded = ref(false)
const loadingMessage = ref('正在加载图表...')
const graphCanvas = ref<HTMLElement>()

const entityTypes = ref<EntityType[]>([
  { name: 'BusinessSegments', selected: true, color: '#4F46E5' },
  { name: 'CorporateStructure', selected: true, color: '#06B6D4' },
  { name: 'FinancialPerformance', selected: true, color: '#10B981' },
  { name: 'FutureOutlook', selected: true, color: '#F59E0B' },
  { name: 'OperationalInfrastructure', selected: false, color: '#EF4444' },
  { name: 'ProductsAndServices', selected: true, color: '#8B5CF6' },
  { name: 'RevenueStreams', selected: true, color: '#EC4899' },
  { name: 'RiskFactors', selected: true, color: '#6B7280' }
])

const relations = ref<Relation[]>([
  { name: 'comprises', selected: true },
  { name: 'contributes_to', selected: true },
  { name: 'depends_on', selected: true },
  { name: 'drives', selected: true },
  { name: 'influences', selected: true }
])

// 计算属性
const selectedEntityCount = computed(() => {
  return entityTypes.value.filter(entity => entity.selected).length
})

const selectedRelationCount = computed(() => {
  return relations.value.filter(relation => relation.selected).length
})

// 监听暗黑模式变化
watch(isDarkMode, (newVal: boolean) => {
  console.log('Query页面检测到全局暗黑模式变化:', newVal)
  if (myChart.value) {
    updateChartTheme()
  }
}, { immediate: true })

// 更新图表主题
const updateChartTheme = (): void => {
  if (!myChart.value) return
  
  console.log('更新图表主题到:', isDarkMode.value ? 'dark' : 'light')
  
  const currentOption = myChart.value.getOption() as any
  
  if (currentOption) {
    const newOption = {
      backgroundColor: isDarkMode.value ? '#1e293b' : '#ffffff',
      title: {
        ...currentOption.title?.[0],
        textStyle: {
          color: isDarkMode.value ? '#ffffff' : '#333333'
        }
      }
    }
    
    if (currentOption.series && currentOption.series[0]) {
      newOption.series = [{
        ...currentOption.series[0],
        lineStyle: {
          ...currentOption.series[0].lineStyle,
          color: isDarkMode.value ? '#64748b' : '#94a3b8'
        }
      }]
    }
    
    myChart.value.setOption(newOption, false)
  }
}

// 初始化图表
const initChart = async (): Promise<void> => {
  try {
    console.log('开始初始化图表，当前主题:', isDarkMode.value)
    
    if (!graphCanvas.value) {
      console.error('图表容器未找到')
      loadingMessage.value = '图表容器未找到'
      return
    }

    console.log('图表容器尺寸:', graphCanvas.value.offsetWidth, 'x', graphCanvas.value.offsetHeight)
    
    // 销毁现有图表实例
    if (myChart.value) {
      myChart.value.dispose()
    }
    
    // 根据主题模式初始化图表
    myChart.value = echarts.init(graphCanvas.value, isDarkMode.value ? 'dark' : undefined)
    console.log('ECharts实例已创建，主题:', isDarkMode.value ? 'dark' : 'default')
    
    // 显示加载动画
    myChart.value.showLoading()
    loadingMessage.value = '正在加载数据...'

    const ROOT_PATH = 'https://echarts.apache.org/examples'
    
    try {
      console.log('开始请求数据')
      const response = await axios.get(`${ROOT_PATH}/data/asset/data/npmdepgraph.min10.json`)
      const json = response.data
      console.log('数据加载成功:', json)
      
      // 隐藏加载动画
      myChart.value.hideLoading()

      const option: EChartsOption = {
        title: {
          text: 'NPM Dependencies',
          textStyle: {
            color: isDarkMode.value ? '#ffffff' : '#333333'
          }
        },
        backgroundColor: isDarkMode.value ? '#1e293b' : '#ffffff',
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            data: json.nodes.map((node: RawNode) => {
              return {
                x: node.x,
                y: node.y,
                id: node.id,
                name: node.label,
                symbolSize: node.size,
                itemStyle: {
                  color: node.color
                }
              };
            }),
            edges: json.edges.map((edge: RawEdge) => {
              return {
                source: edge.sourceID,
                target: edge.targetID
              };
            }),
            emphasis: {
              focus: 'adjacency',
              label: {
                position: 'right',
                show: true
              }
            },
            roam: true,
            roamTrigger: 'global',
            lineStyle: {
              width: 0.5,
              curveness: 0.3,
              opacity: 0.7,
              color: isDarkMode.value ? '#64748b' : '#94a3b8'
            }
          }
        ]
      }

      console.log('设置图表配置')
      myChart.value.setOption(option, true)
      chartLoaded.value = true
      console.log('图表配置完成')
      
    } catch (networkError) {
      console.error('网络请求失败，使用备用数据:', networkError)
      loadFallbackData()
    }
    
  } catch (error) {
    console.error('初始化图表失败:', error)
    loadingMessage.value = '图表初始化失败'
  }
}

// 加载备用数据
const loadFallbackData = (): void => {
  console.log('加载备用数据')
  myChart.value?.hideLoading()
  
  const option: EChartsOption = {
    title: {
      text: 'NPM Dependencies (备用数据)',
      textStyle: {
        color: isDarkMode.value ? '#ffffff' : '#333333'
      }
    },
    backgroundColor: isDarkMode.value ? '#1e293b' : '#ffffff',
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'none',
        data: [
          { x: 100, y: 100, id: '1', name: 'vue', symbolSize: 30, itemStyle: { color: '#4FC08D' } },
          { x: 200, y: 50, id: '2', name: 'echarts', symbolSize: 25, itemStyle: { color: '#5470c6' } },
          { x: 200, y: 150, id: '3', name: 'axios', symbolSize: 20, itemStyle: { color: '#91cc75' } },
          { x: 300, y: 100, id: '4', name: 'typescript', symbolSize: 35, itemStyle: { color: '#3ba0ff' } },
          { x: 400, y: 80, id: '5', name: 'vite', symbolSize: 25, itemStyle: { color: '#fac858' } }
        ],
        edges: [
          { source: '1', target: '2' },
          { source: '1', target: '3' },
          { source: '1', target: '4' },
          { source: '4', target: '5' },
          { source: '2', target: '4' }
        ],
        emphasis: {
          focus: 'adjacency',
          label: {
            position: 'right',
            show: true
          }
        },
        roam: true,
        roamTrigger: 'global',
        lineStyle: {
          width: 0.5,
          curveness: 0.3,
          opacity: 0.7,
          color: isDarkMode.value ? '#64748b' : '#94a3b8'
        }
      }
    ]
  }

  myChart.value?.setOption(option, true)
  chartLoaded.value = true
  console.log('备用数据加载完成')
}

// 处理窗口大小变化
const handleResize = (): void => {
  if (myChart.value) {
    myChart.value.resize()
  }
}

// 生命周期钩子
onMounted(async () => {
  console.log('Query组件已挂载，当前全局主题状态:', isDarkMode.value)
  await nextTick()
  await initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (myChart.value) {
    myChart.value.dispose()
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.query-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8fafc;
  transition: background-color 0.3s ease;
}

.query-page.dark-mode {
  background: #0f172a !important;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e2e8f0;
  padding: 1rem;
  overflow-y: auto;
  transition: all 0.3s ease;
}

.sidebar.dark-sidebar,
.dark-mode .sidebar {
  background: #1e293b !important;
  border-right-color: #334155 !important;
  color: #e2e8f0 !important;
}

.filters-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.filters-section h3 {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: inherit;
}

.dark-mode .filters-section h3 {
  color: #e2e8f0 !important;
}

.filter-controls {
  display: flex;
  gap: 0.25rem;
}

.btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  color: #64748b;
  border-radius: 0.375rem;
  transition: all 0.3s ease;
  min-width: 32px;
  min-height: 32px;
}

.btn-icon:hover {
  background: #f1f5f9;
}

.dark-mode .btn-icon {
  color: #94a3b8 !important;
}

.dark-mode .btn-icon:hover {
  background: #334155 !important;
  color: #e2e8f0 !important;
}

.filter-group {
  margin-bottom: 1.5rem;
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: inherit;
}

.dark-mode .filter-header {
  color: #e2e8f0 !important;
}

.count {
  margin-left: auto;
  color: #64748b;
  font-size: 0.875rem;
}

.dark-mode .count {
  color: #94a3b8 !important;
}

.filter-items {
  padding-left: 1.5rem;
}

.filter-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.filter-item label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-left: 0.5rem;
  cursor: pointer;
  color: inherit;
  transition: color 0.3s ease;
}

.dark-mode .filter-item label {
  color: #e2e8f0 !important;
}

.filter-item label:hover {
  color: #0ea5e9;
}

.color-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.view-options {
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.dark-mode .view-options {
  border-top-color: #334155 !important;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.option-item label {
  margin-left: 0.5rem;
  cursor: pointer;
  color: inherit;
  transition: color 0.3s ease;
}

.dark-mode .option-item label {
  color: #e2e8f0 !important;
}

.option-item label:hover {
  color: #0ea5e9;
}

.graph-container {
  flex: 1;
  position: relative;
  min-height: 400px;
  background: white;
  transition: background-color 0.3s ease;
}

.graph-container.dark-container,
.dark-mode .graph-container {
  background: #1e293b !important;
}

.graph-canvas {
  width: 100%;
  height: 100%;
  min-height: 400px;
  transition: background-color 0.3s ease;
}

.dark-mode .graph-canvas {
  background: #1e293b !important;
}

.loading-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748b;
  font-size: 1rem;
  transition: color 0.3s ease;
}

.dark-mode .loading-placeholder {
  color: #94a3b8 !important;
}

input[type="checkbox"] {
  accent-color: #0ea5e9;
}

.dark-mode input[type="checkbox"] {
  accent-color: #38bdf8;
}
</style>