<template>
  <div class="query-page" :class="{ 'dark-mode': isDarkMode }">
    <div class="main-content">
      <!-- Left Sidebar - Filters -->
      <div class="sidebar" :class="{ 'dark-sidebar': isDarkMode }">
        <div class="filters-section">
          <h3>Filters</h3>
          <div class="filter-controls">
            <button class="btn-icon" @click="refreshData" title="刷新数据">↻</button>
          </div>
        </div>

        <!-- Entity Types -->
        <div class="filter-group">
          <div class="filter-header">
            <span>Entity types</span>
            <span class="count">{{ selectedEntityCount }}/{{ entityTypes.length }}</span>
          </div>
          <div class="filter-items">
            <div class="filter-item" v-for="entityType in entityTypes" :key="entityType.name">
              <input type="checkbox" :id="entityType.name" v-model="entityType.selected">
              <label :for="entityType.name">
                <span 
                  class="color-indicator" 
                  :class="{
                    'keyword-gradient': entityType.name.includes('Keyword') || entityType.name.includes('关键词')
                  }"
                  :style="{ 
                    backgroundColor: (entityType.name.includes('Keyword') || entityType.name.includes('关键词')) ? undefined : entityType.color 
                  }"
                ></span>
                {{ entityType.name }}
              </label>
            </div>
          </div>
        </div>

        <!-- Relations -->
        <div class="filter-group">
          <div class="filter-header">
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

        <!-- 操作说明 -->
        <div class="help-section">
          <h4>操作说明</h4>
          <ul class="help-list">
            <li><strong>悬浮</strong>节点查看标签</li>
            <li><strong>拖拽</strong>和<strong>滚轮</strong>缩放图表</li>
            <li>点击刷新按钮重新加载数据</li>
          </ul>
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
import { isDarkMode } from '../stores/theme'
import type { ApiGraphData, Node, Edge } from '../types/graph'
import { getDocKeywordGraph } from '../services/api'

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

// 预定义的关键词颜色数组
const keywordColors = [
  '#ff6b6b', // 红色
  '#4ecdc4', // 青色
  '#45b7d1', // 蓝色
  '#96ceb4', // 绿色
  '#feca57', // 黄色
  '#ff9ff3', // 粉色
  '#54a0ff', // 亮蓝色
  '#5f27cd', // 紫色
  '#00d2d3', // 青绿色
  '#ff9f43', // 橙色
  '#ee5a6f', // 粉红色
  '#0abde3', // 天蓝色
  '#7bed9f', // 浅绿色
  '#70a1ff', // 淡蓝色
  '#5352ed', // 深紫色
  '#ff6348', // 橙红色
  '#2ed573', // 绿色
  '#1e90ff', // 道奇蓝
  '#ff7675', // 浅红色
  '#74b9ff'  // 浅蓝色
]

// 生成随机颜色的函数
const getRandomKeywordColor = (): string => {
  return keywordColors[Math.floor(Math.random() * keywordColors.length)]
}

// 为节点分配颜色（保持一致性）
const nodeColorMap = new Map<string, string>()

const getNodeColor = (node: Node): string => {
  // 如果是文档节点，使用固定颜色
  if (node.id.startsWith('doc-')) {
    return '#5470c6' // 蓝色
  }
  
  // 如果是关键词节点，使用随机颜色（但保持一致性）
  if (node.id.startsWith('kw-')) {
    if (!nodeColorMap.has(node.id)) {
      nodeColorMap.set(node.id, getRandomKeywordColor())
    }
    return nodeColorMap.get(node.id)!
  }
  
  // 默认颜色
  return node.color || '#91cc75'
}

// 响应式数据
const hideUnselectedNodes = ref(false)
const autoFitView = ref(false)
const lockSelection = ref(false)
const myChart = ref<echarts.ECharts | null>(null)
const chartLoaded = ref(false)
const loadingMessage = ref('正在加载图表...')
const graphCanvas = ref<HTMLElement>()

// 存储原始图数据，用于筛选
const originalGraphData = ref<ApiGraphData | null>(null)

const entityTypes = ref<EntityType[]>([
  { name: '文档 (Document)', selected: true, color: '#5470c6' },
  { name: '关键词 (Keyword)', selected: true, color: '#91cc75' }
])

const relations = ref<Relation[]>([
  { name: '包含关键词', selected: true },
  { name: '相关概念', selected: true }
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

// 监听筛选条件变化
watch([entityTypes, relations, hideUnselectedNodes, autoFitView], () => {
  if (myChart.value && chartLoaded.value) {
    applyFilters()
  }
}, { deep: true })

// 应用筛选条件
const applyFilters = (): void => {
  if (!myChart.value || !originalGraphData.value) return
  
  console.log('开始应用筛选条件')
  
  // 获取选中的实体类型
  const selectedEntityTypes = entityTypes.value
    .filter(entity => entity.selected)
    .map(entity => entity.name)
  
  // 获取选中的关系
  const selectedRelations = relations.value
    .filter(relation => relation.selected)
    .map(relation => relation.name)
  
  console.log('选中的实体类型:', selectedEntityTypes)
  console.log('选中的关系:', selectedRelations)
  
  // 筛选节点
  let filteredNodes = originalGraphData.value.nodes.filter((node: Node) => {
    // 根据节点类型筛选
    if (node.id.startsWith('doc-')) {
      return selectedEntityTypes.includes('文档 (Document)')
    } else if (node.id.startsWith('kw-')) {
      return selectedEntityTypes.includes('关键词 (Keyword)')
    }
    return true
  })
  
  // 获取筛选后的节点ID集合
  const filteredNodeIds = new Set(filteredNodes.map(node => node.id))
  
  // 筛选边：只保留两端节点都存在的边
  let filteredEdges = originalGraphData.value.edges.filter((edge: Edge) => {
    return filteredNodeIds.has(edge.source) && filteredNodeIds.has(edge.target)
  })
  
  // 如果启用"隐藏未选中节点"功能
  if (hideUnselectedNodes.value) {
    // 可以在这里添加更复杂的筛选逻辑
    // 比如只显示有至少一个连接的节点
    const connectedNodeIds = new Set<string>()
    filteredEdges.forEach(edge => {
      connectedNodeIds.add(edge.source)
      connectedNodeIds.add(edge.target)
    })
    
    filteredNodes = filteredNodes.filter(node => connectedNodeIds.has(node.id))
  }
  
  // 转换为ECharts数据格式，使用随机颜色
  const chartData = filteredNodes.map((node: Node) => {
    return {
      id: node.id,
      name: node.label,
      symbolSize: node.size || 20,
      itemStyle: {
        color: getNodeColor(node) // 使用新的颜色分配函数
      },
      category: node.id.startsWith('doc-') ? 0 : 1,
      label: {
        fontSize: 9,
        color: isDarkMode.value ? '#ffffff' : '#333333'
      }
    };
  })
  
  const chartEdges = filteredEdges.map((edge: Edge) => {
    return {
      source: edge.source,
      target: edge.target,
      lineStyle: {
        width: 1,
        opacity: 0.6
      }
    };
  })
  
  // 更新图表
  myChart.value.setOption({
    series: [{
      data: chartData,
      edges: chartEdges
    }]
  }, false)
  
  console.log(`筛选完成: 显示 ${filteredNodes.length} 个节点，${filteredEdges.length} 条边`)
  
  // 如果启用自动适应视图
  if (autoFitView.value) {
    setTimeout(() => {
      // 可以添加自动缩放逻辑
      console.log('自动适应视图已启用')
    }, 300)
  }
}

// 更新图表主题
const updateChartTheme = (): void => {
  if (!myChart.value) return
  
  console.log('更新图表主题到:', isDarkMode.value ? 'dark' : 'light')
  
  const currentOption = myChart.value.getOption() as any
  
  if (currentOption) {
    const newOption = {
      ...currentOption,
      backgroundColor: isDarkMode.value ? '#1e293b' : '#ffffff',
      title: [
        {
          ...(currentOption.title?.[0] || {}),
          textStyle: {
            color: isDarkMode.value ? '#ffffff' : '#333333'
          }
        }
      ]
    }

    if (currentOption.series && currentOption.series[0]) {
      newOption.series = [
        {
          ...currentOption.series[0],
          lineStyle: {
            ...(currentOption.series[0].lineStyle || {}),
            color: isDarkMode.value ? '#64748b' : '#94a3b8'
          }
        }
      ]
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

    // 加载真实数据
    await loadRealData()
    
  } catch (error) {
    console.error('初始化图表失败:', error)
    loadingMessage.value = '图表初始化失败'
  }
}

// 从API加载真实数据
const loadRealData = async (): Promise<void> => {
  try {
    console.log('开始从API加载真实数据')
    loadingMessage.value = '正在从服务器获取数据...'
    
    // 调用真实的API接口
    const graphData = await getDocKeywordGraph()
    console.log('API数据加载成功:', graphData)
    
    // 保存原始数据用于筛选
    originalGraphData.value = graphData
    
    // 隐藏加载动画
    myChart.value?.hideLoading()

    const option: EChartsOption = {
      title: {
        text: '',
        textStyle: {
          color: isDarkMode.value ? '#ffffff' : '#333333',
          fontSize: 16
        }
      },
      backgroundColor: isDarkMode.value ? '#1e293b' : '#ffffff',
      animationDurationUpdate: 1500,
      animationEasingUpdate: 'quinticInOut',
      series: [
        {
          type: 'graph',
          layout: 'force',
          force: {
            repulsion: 1000,
            edgeLength: 100,
            gravity: 0.1
          },
          data: graphData.nodes.map((node: Node) => {
            return {
              id: node.id,
              name: node.label,
              symbolSize: node.size || 20,
              itemStyle: {
                color: getNodeColor(node) // 使用新的颜色分配函数
              },
              category: node.id.startsWith('doc-') ? 0 : 1, // 文档和关键词分类
              label: {
                fontSize: 9,
                color: isDarkMode.value ? '#ffffff' : '#333333'
              }
            };
          }),
          edges: graphData.edges.map((edge: Edge) => {
            return {
              source: edge.source,
              target: edge.target,
              lineStyle: {
                width: 1,
                opacity: 0.6
              }
            };
          }),
          categories: [
            {
              name: '文档',
              itemStyle: {
                color: '#5470c6'
              }
            },
            {
              name: '关键词',
              itemStyle: {
                color: '#91cc75' // 这里保留原来的颜色作为图例显示
              }
            }
          ],
          emphasis: {
            focus: 'adjacency',
            label: {
              show: true, // 悬浮时显示标签
              position: 'right',
              fontSize: 12,
              color: isDarkMode.value ? '#ffffff' : '#333333',
              fontWeight: 'bold'
            }
          },
          roam: true,
          lineStyle: {
            width: 1,
            curveness: 0.1,
            opacity: 0.6,
            color: isDarkMode.value ? '#64748b' : '#94a3b8'
          },
          tooltip: {
            show: true,
            formatter: (params: any) => {
              if (params.dataType === 'node') {
                return `${params.data.name}<br/>ID: ${params.data.id}<br/>大小: ${params.data.symbolSize}`;
              } else if (params.dataType === 'edge') {
                return `${params.data.source} → ${params.data.target}`;
              }
              return '';
            }
          }
        }
      ]
    }

    console.log('设置图表配置')
    myChart.value?.setOption(option, true)
    chartLoaded.value = true
    console.log('真实数据图表配置完成')
    
  } catch (error) {
    console.error('API请求失败，使用备用数据:', error)
    loadingMessage.value = 'API请求失败，正在加载备用数据...'
    loadFallbackData()
  }
}

// 加载备用数据
const loadFallbackData = (): void => {
  console.log('加载备用数据')
  myChart.value?.hideLoading()
  
  // 模拟你的API数据格式的备用数据
  const fallbackData: ApiGraphData = {
    nodes: [
      {
        id: "doc-example-1",
        label: "机器学习基础概念",
        size: 30.0,
        color: "#5470c6"
      },
      {
        id: "kw-ml-1",
        label: "机器学习",
        size: 25.0,
        color: "#91cc75"
      },
      {
        id: "kw-supervised-1",
        label: "监督学习",
        size: 20.0,
        color: "#91cc75"
      },
      {
        id: "kw-unsupervised-1",
        label: "无监督学习",
        size: 20.0,
        color: "#91cc75"
      },
      {
        id: "doc-example-2",
        label: "深度学习应用",
        size: 28.0,
        color: "#5470c6"
      },
      {
        id: "kw-deep-learning",
        label: "深度学习",
        size: 22.0,
        color: "#91cc75"
      },
      {
        id: "kw-neural-network",
        label: "神经网络",
        size: 18.0,
        color: "#91cc75"
      }
    ],
    edges: [
      { source: "doc-example-1", target: "kw-ml-1" },
      { source: "doc-example-1", target: "kw-supervised-1" },
      { source: "doc-example-1", target: "kw-unsupervised-1" },
      { source: "doc-example-2", target: "kw-deep-learning" },
      { source: "doc-example-2", target: "kw-neural-network" },
      { source: "doc-example-2", target: "kw-ml-1" }
    ]
  }
  
  // 保存原始数据用于筛选
  originalGraphData.value = fallbackData
  
  const option: EChartsOption = {
    title: {
      text: '文档-关键词知识图谱',
      textStyle: {
        color: isDarkMode.value ? '#ffffff' : '#333333',
        fontSize: 16
      }
    },
    backgroundColor: isDarkMode.value ? '#1e293b' : '#ffffff',
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'force',
        force: {
          repulsion: 800,
          edgeLength: 80,
          gravity: 0.1
        },
        data: fallbackData.nodes.map((node: Node) => {
          return {
            id: node.id,
            name: node.label,
            symbolSize: node.size,
            itemStyle: {
              color: getNodeColor(node) // 使用新的颜色分配函数
            },
            category: node.id.startsWith('doc-') ? 0 : 1,
            label: {
              show: false, // 默认不显示标签，悬浮时显示
              fontSize: 10,
              color: isDarkMode.value ? '#ffffff' : '#333333'
            }
          };
        }),
        edges: fallbackData.edges.map((edge: Edge) => {
          return {
            source: edge.source,
            target: edge.target,
            lineStyle: {
              width: 1,
              opacity: 0.6
            }
          };
        }),
        categories: [
          {
            name: '文档',
            itemStyle: {
              color: '#5470c6'
            }
          },
          {
            name: '关键词',
            itemStyle: {
              color: '#91cc75'
            }
          }
        ],
        emphasis: {
          focus: 'adjacency',
          label: {
            show: true, // 悬浮时显示标签
            position: 'right',
            fontSize: 12,
            color: isDarkMode.value ? '#ffffff' : '#333333',
            fontWeight: 'bold'
          }
        },
        roam: true,
        lineStyle: {
          width: 1,
          curveness: 0.1,
          opacity: 0.6,
          color: isDarkMode.value ? '#64748b' : '#94a3b8'
        },
        tooltip: {
          show: true,
          formatter: (params: any) => {
            if (params.dataType === 'node') {
              return `${params.data.name}<br/>ID: ${params.data.id}<br/>大小: ${params.data.symbolSize}`;
            } else if (params.dataType === 'edge') {
              return `${params.data.source} → ${params.data.target}`;
            }
            return '';
          }
        }
      }
    ]
  }

  myChart.value?.setOption(option, true)
  chartLoaded.value = true
  console.log('备用数据加载完成')
}

// 刷新数据
const refreshData = async (): Promise<void> => {
  if (myChart.value) {
    // 清除颜色缓存，重新生成随机颜色
    nodeColorMap.clear()
    myChart.value.showLoading()
    loadingMessage.value = '正在刷新数据...'
    chartLoaded.value = false
    await loadRealData()
  }
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
  height: 100%;
  width: 100%;
  background: #f8fafc;
  transition: background-color 0.3s ease;
  overflow: hidden;
}

.query-page.dark-mode {
  background: #0f172a !important;
}

.main-content {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.sidebar {
  width: 280px;
  min-width: 280px;
  max-width: 280px;
  background: white;
  border-right: 1px solid #e2e8f0;
  padding: 1rem;
  overflow-y: auto;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
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
  margin-bottom: 0.8rem;
  flex-shrink: 0;
}

.filters-section h3 {
  margin: 0;
  font-size: 0.9rem;
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
  padding: 0.4rem;
  cursor: pointer;
  color: #64748b;
  border-radius: 0.375rem;
  transition: all 0.3s ease;
  min-width: 28px;
  min-height: 28px;
  font-size: 0.8rem;
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
  margin-bottom: 0.8rem;
  flex-shrink: 0;
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.4rem;
  font-weight: 500;
  font-size: 0.8rem;
  color: inherit;
}

.dark-mode .filter-header {
  color: #e2e8f0 !important;
}

.count {
  margin-left: auto;
  color: #64748b;
  font-size: 0.7rem;
}

.dark-mode .count {
  color: #94a3b8 !important;
}

.filter-items {
  padding-left: 0.8rem;
}

.filter-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.3rem;
}

.filter-item label {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-left: 0.4rem;
  cursor: pointer;
  color: inherit;
  transition: color 0.3s ease;
  font-size: 0.75rem;
}

.dark-mode .filter-item label {
  color: #e2e8f0 !important;
}

.filter-item label:hover {
  color: #0ea5e9;
}

.color-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* 关键词彩色渐变背景 */
.keyword-gradient {
  background: linear-gradient(45deg, 
    #ff6b6b 0%, 
    #4ecdc4 14%, 
    #45b7d1 28%, 
    #96ceb4 42%, 
    #feca57 57%, 
    #ff9ff3 71%, 
    #54a0ff 85%, 
    #5f27cd 100%) !important;
  background-size: 400% 400% !important;
  animation: gradientShift 3s ease infinite !important;
}

/* 彩色渐变动画 */
@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.view-options {
  margin-top: 0.8rem;
  padding-top: 0.8rem;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.dark-mode .view-options {
  border-top-color: #334155 !important;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.3rem;
}

.option-item label {
  margin-left: 0.4rem;
  cursor: pointer;
  color: inherit;
  transition: color 0.3s ease;
  font-size: 0.75rem;
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
  background: white;
  transition: background-color 0.3s ease;
  overflow: hidden;
  height: 100%;
}

.graph-container.dark-container,
.dark-mode .graph-container {
  background: #1e293b !important;
}

.graph-canvas {
  width: 100%;
  height: 100%;
  transition: background-color 0.3s ease;
  overflow: hidden;
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
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.dark-mode .loading-placeholder {
  color: #94a3b8 !important;
}

input[type="checkbox"] {
  accent-color: #0ea5e9;
  transform: scale(0.8);
}

.dark-mode input[type="checkbox"] {
  accent-color: #38bdf8;
}

.help-section {
  margin-top: 0.8rem;
  padding-top: 0.8rem;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.dark-mode .help-section {
  border-top-color: #334155 !important;
}

.help-section h4 {
  margin: 0 0 0.4rem 0;
  font-size: 0.75rem;
  font-weight: 600;
  color: inherit;
}

.dark-mode .help-section h4 {
  color: #e2e8f0 !important;
}

.help-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.help-list li {
  font-size: 0.65rem;
  color: #64748b;
  margin-bottom: 0.25rem;
  line-height: 1.2;
}

.dark-mode .help-list li {
  color: #94a3b8 !important;
}

.help-list li strong {
  color: #374151;
}

.dark-mode .help-list li strong {
  color: #d1d5db !important;
}

/* 滚动条样式 */
.sidebar::-webkit-scrollbar {
  width: 3px;
}

.sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 2px;
}

.dark-mode .sidebar::-webkit-scrollbar-thumb {
  background: #4a5568;
}
</style>