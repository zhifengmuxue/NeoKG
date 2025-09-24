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

        <!-- 异常检测部分 -->
        <div class="anomaly-detection">
          <div class="anomaly-header">
            <h4>异常检测</h4>
          </div>
          <div class="anomaly-content">
            <button 
              class="anomaly-btn" 
              @click="runAnomalyDetection"
              :disabled="anomalyLoading"
              :class="{ 'loading': anomalyLoading, 'active': anomalyActive }"
            >
              {{ getAnomalyButtonText() }}
            </button>
            
            <!-- 异常检测结果 -->
            <div v-if="anomalyResults && anomalyActive" class="anomaly-results">
              <div class="result-item">
                <span class="result-label">自环:</span>
                <span class="result-value" :class="{ 'has-issue': anomalyResults.selfLoops.length > 0 }">
                  {{ anomalyResults.selfLoops.length }}
                </span>
              </div>
              <div class="result-item">
                <span class="result-label">孤立节点:</span>
                <span class="result-value" :class="{ 'has-issue': anomalyResults.isolatedNodes.length > 0 }">
                  {{ anomalyResults.isolatedNodes.length }}
                </span>
              </div>
              <div class="result-item">
                <span class="result-label">重复关系:</span>
                <span class="result-value" :class="{ 'has-issue': anomalyResults.duplicateRelations.length > 0 }">
                  {{ anomalyResults.duplicateRelations.length }}
                </span>
              </div>
              <div class="result-item">
                <span class="result-label">无效关系:</span>
                <span class="result-value" :class="{ 'has-issue': anomalyResults.invalidRelations.length > 0 }">
                  {{ anomalyResults.invalidRelations.length }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 社区发现部分 -->
        <div class="community-detection">
          <div class="community-header">
            <h4>社区发现</h4>
          </div>
          <div class="community-content">
            <button 
              class="community-btn" 
              @click="runCommunityDetection"
              :disabled="communityLoading"
              :class="{ 'loading': communityLoading, 'active': communityActive }"
            >
              {{ getCommunityButtonText() }}
            </button>
            
            <!-- 社区检测结果 -->
            <div v-if="communityResults && communityActive" class="community-results">
              <div class="result-item">
                <span class="result-label">发现社区:</span>
                <span class="result-value">{{ getCommunityCount() }}个</span>
              </div>
              <div class="result-item">
                <span class="result-label">最大社区:</span>
                <span class="result-value">{{ getLargestCommunitySize() }}个节点</span>
              </div>
              
              <!-- 社区详情列表 -->
              <div class="community-details" v-if="communityResults && communityResults.communities && communityResults.communities.length > 0">
                <div class="community-list-header">
                  <span class="community-list-title">社区详情:</span>
                </div>
                <div class="community-item" v-for="(community, index) in communityResults.communities" :key="community.id || index">
                  <span 
                    class="community-color" 
                    :style="{ backgroundColor: community.color }"
                  ></span>
                  <span class="community-text">
                    社区{{ index + 1 }}: {{ community.nodes?.length || 0 }}个节点
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- BFS路径搜索部分 -->
        <div class="path-search">
          <div class="path-header">
            <h4>路径搜索</h4>
          </div>
          <div class="path-content">
            <div class="search-inputs">
              <!-- 起始节点选择 -->
              <div class="input-group">
                <label class="input-label">起始节点:</label>
                <select v-model="pathSearch.startType" class="type-select">
                  <option value="KEYWORD">关键词</option>
                  <option value="DOCUMENT">文档</option>
                </select>
                <select 
                  v-model="pathSearch.startKeyword"
                  class="path-select"
                >
                  <option value="">{{ pathSearch.startType === 'KEYWORD' ? '选择起始关键词' : '选择起始文档' }}</option>
                  <option 
                    v-for="node in getAvailableNodes(pathSearch.startType)" 
                    :key="node.id" 
                    :value="node.label"
                  >
                    {{ node.label }}
                  </option>
                </select>
              </div>
              
              <!-- 目标节点选择 -->
              <div class="input-group">
                <label class="input-label">目标节点:</label>
                <select v-model="pathSearch.endType" class="type-select">
                  <option value="KEYWORD">关键词</option>
                  <option value="DOCUMENT">文档</option>
                </select>
                <select 
                  v-model="pathSearch.endKeyword"
                  class="path-select"
                >
                  <option value="">{{ pathSearch.endType === 'KEYWORD' ? '选择目标关键词' : '选择目标文档' }}</option>
                  <option 
                    v-for="node in getAvailableNodes(pathSearch.endType)" 
                    :key="node.id" 
                    :value="node.label"
                  >
                    {{ node.label }}
                  </option>
                </select>
              </div>
            </div>
            
            <!-- 添加快速输入按钮 -->
            <div class="quick-select">
              <span class="quick-label">快速选择:</span>
              <button 
                class="quick-btn" 
                @click="setQuickPath('KEYWORD', '机器学习', 'KEYWORD', '深度学习')"
                v-if="hasNode('KEYWORD', '机器学习') && hasNode('KEYWORD', '深度学习')"
              >
                机器学习→深度学习
              </button>
              <button 
                class="quick-btn" 
                @click="setQuickPath('KEYWORD', '监督学习', 'KEYWORD', '神经网络')"
                v-if="hasNode('KEYWORD', '监督学习') && hasNode('KEYWORD', '神经网络')"
              >
                监督学习→神经网络
              </button>
            </div>
            
            <button 
              class="path-btn" 
              @click="runPathSearch"
              :disabled="pathLoading || !pathSearch.startKeyword || !pathSearch.endKeyword"
              :class="{ 'loading': pathLoading }"
            >
              {{ pathLoading ? '搜索中...' : 'BFS路径搜索' }}
            </button>
            
            <!-- 路径搜索结果 -->
            <div v-if="pathResults" class="path-results">
              <div class="result-item">
                <span class="result-label">路径长度:</span>
                <span class="result-value">{{ getPathLength() }}个节点</span>
              </div>
              <div v-if="pathResults.nodes && pathResults.nodes.length > 0" class="path-nodes">
                <div class="path-node" v-for="(node, index) in pathResults.nodes" :key="node.id">
                  <span class="node-type">{{ node.id.startsWith('doc-') ? '[文档]' : '[关键词]' }}</span>
                  {{ node.label }}
                  <span v-if="index < pathResults.nodes.length - 1" class="arrow">→</span>
                </div>
              </div>
            </div>
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

// 异常检测数据类型
interface AnomalyData {
  selfLoops: any[]
  isolatedNodes: any[]
  duplicateRelations: any[]
  invalidRelations: any[]
}

interface AnomalyResponse {
  code: string
  message: string | null
  data: AnomalyData
  timestamp: number
}

// 修改社区发现数据类型 - 简化为只存储社区信息
interface CommunityInfo {
  communities: Array<{
    id: number
    nodes: string[]
    color: string
  }>
}

interface CommunityResponse {
  code: string
  message: string | null
  data: {
    nodes: Node[]
    edges: Edge[]
  } | null
  timestamp: number
}

// 添加缺失的路径搜索数据类型
interface PathSearchData {
  nodes: Node[]
  edges: Edge[]
}

// 修正路径搜索响应类型定义
interface PathSearchResponse {
  code: string
  message: string | null
  data: {
    nodes: Node[]
    edges: Edge[]
  } | null
  timestamp: number
}

// 预定义的关键词颜色数组
const keywordColors = [
  '#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4', '#feca57',
  '#ff9ff3', '#54a0ff', '#5f27cd', '#00d2d3', '#ff9f43',
  '#ee5a6f', '#0abde3', '#7bed9f', '#70a1ff', '#5352ed',
  '#ff6348', '#2ed573', '#1e90ff', '#ff7675', '#74b9ff'
]

// 社区颜色数组
const communityColors = [
  '#EE6666', '#73C0DE', '#91CC75', '#FAC858', '#5470C6',
  '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FECA57',
  '#FF9FF3', '#54A0FF', '#5F27CD', '#00D2D3', '#FF9F43'
]

// 生成随机颜色的函数
const getRandomKeywordColor = (): string => {
  return keywordColors[Math.floor(Math.random() * keywordColors.length)]
}

// 为节点分配颜色（保持一致性）
const nodeColorMap = new Map<string, string>()

const getNodeColor = (node: Node): string => {
  // 优先级1: 路径高亮 - 使用更鲜艳的颜色
  if (pathHighlightNodes.value.has(node.id)) {
    return '#FF6B35' // 更鲜艳的橙红色高亮
  }
  
  // 优先级2: 社区颜色
  if (communityResults.value && communityActive.value && communityNodeColors.value.has(node.id)) {
    const communityColor = communityNodeColors.value.get(node.id)
    console.log(`节点 ${node.id} 使用社区颜色: ${communityColor}`)
    return communityColor!
  }
  
  // 优先级3: 默认节点类型颜色
  if (node.id.startsWith('doc-')) {
    return '#5470c6' // 蓝色 - 文档
  } else if (node.id.startsWith('kw-')) {
    // 关键词使用一致的随机颜色
    if (!nodeColorMap.has(node.id)) {
      nodeColorMap.set(node.id, getRandomKeywordColor())
    }
    return nodeColorMap.get(node.id)!
  }
  
  // 优先级4: 备用颜色
  return node.color || '#91cc75'
}

// 响应式数据
const myChart = ref<echarts.ECharts | null>(null)
const chartLoaded = ref(false)
const loadingMessage = ref('正在加载图表...')
const graphCanvas = ref<HTMLElement>()

// 异常检测相关状态
const anomalyLoading = ref(false)
const anomalyResults = ref<AnomalyData | null>(null)
// 新增：异常检测激活状态
const anomalyActive = ref(false)

// 社区发现相关状态
const communityLoading = ref(false)
const communityResults = ref<CommunityInfo | null>(null)
const communityNodeColors = ref<Map<string, string>>(new Map())
// 新增：社区发现激活状态
const communityActive = ref(false)

// 新增：路径搜索相关状态
const pathLoading = ref(false)
const pathResults = ref<PathSearchData | null>(null)
const pathSearch = ref({
  startKeyword: '',
  endKeyword: '',
  startType: 'KEYWORD',
  endType: 'KEYWORD'
})
const pathHighlightNodes = ref<Set<string>>(new Set())
const pathHighlightEdges = ref<Set<string>>(new Set())

// 存储原始图数据，用于筛选
const originalGraphData = ref<ApiGraphData | null>(null)

// 修复：entityTypes 数组语法错误
const entityTypes = ref<EntityType[]>([
  { name: '文档 (Document)', selected: true, color: '#5470c6' },
  { name: '关键词 (Keyword)', selected: true, color: '#91cc75' }
])

// API配置
const ANOMALY_API_URL = import.meta.env.DEV ? '/api/graph/analysis/anomalies' : 'http://localhost:8080/api/graph/analysis/anomalies'
const COMMUNITY_API_URL = import.meta.env.DEV ? '/api/graph/community' : 'http://localhost:8080/api/graph/community'
const PATH_SEARCH_API_URL = import.meta.env.DEV ? '/api/graph/path' : 'http://localhost:8080/api/graph/path'
// 新增：分析刷新API
const ANALYSIS_REFRESH_API_URL = import.meta.env.DEV ? '/api/graph/analysis/refresh' : 'http://localhost:8080/api/graph/analysis/refresh'

// 计算属性
const selectedEntityCount = computed(() => {
  return entityTypes.value.filter(entity => entity.selected).length
})

// 新增：获取可用节点的辅助函数
const getAvailableNodes = (type: string) => {
  if (!originalGraphData.value) return []
  
  const prefix = type === 'KEYWORD' ? 'kw-' : 'doc-'
  return originalGraphData.value.nodes.filter(node => node.id.startsWith(prefix))
}

// 新增：检查节点是否存在的辅助函数
const hasNode = (type: string, label: string): boolean => {
  if (!originalGraphData.value) return false
  
  const prefix = type === 'KEYWORD' ? 'kw-' : 'doc-'
  return originalGraphData.value.nodes.some(node => 
    node.id.startsWith(prefix) && node.label === label
  )
}

// 新增：快速设置路径的辅助函数
const setQuickPath = (startType: string, startKeyword: string, endType: string, endKeyword: string) => {
  pathSearch.value.startType = startType
  pathSearch.value.startKeyword = startKeyword
  pathSearch.value.endType = endType
  pathSearch.value.endKeyword = endKeyword
}

// 社区发现按钮文本
const getCommunityButtonText = (): string => {
  if (communityLoading.value) return '检测中...'
  return communityActive.value ? '收起社区发现' : '社区发现'
}

// 异常检测按钮文本  
const getAnomalyButtonText = (): string => {
  if (anomalyLoading.value) return '刷新检测中...'
  return anomalyActive.value ? '收起异常检测' : '运行异常检测'
}

// 获取路径长度的辅助函数
const getPathLength = (): number => {
  return pathResults.value?.nodes?.length || 0
}

// 修复社区详情显示函数
const getCommunityCount = (): number => {
  if (!communityResults.value?.communities || !Array.isArray(communityResults.value.communities)) {
    return 0
  }
  return communityResults.value.communities.length
}

const getLargestCommunitySize = (): number => {
  if (!communityResults.value?.communities || !Array.isArray(communityResults.value.communities)) {
    return 0
  }
  
  const sizes = communityResults.value.communities.map(community => community.nodes.length)
  return sizes.length > 0 ? Math.max(...sizes) : 0
}

// 新增：从节点ID中提取数字ID的辅助函数
const extractNumericId = (nodeId: string): string => {
  // 从 "kw-1970310627054456832" 或 "doc-1969949281729253376" 中提取数字部分
  const match = nodeId.match(/(?:kw-|doc-)(\d+)/)
  return match ? match[1] : nodeId
}

// 添加核心的数据加载函数
const loadGraphData = async (): Promise<void> => {
  try {
    loadingMessage.value = '正在获取图数据...'
    console.log('开始加载图数据')
    
    const data = await getDocKeywordGraph()
    console.log('获取到的图数据:', data)
    
    if (!data || !data.nodes || !data.edges) {
      throw new Error('获取的图数据格式不正确')
    }
    
    // 保存原始数据
    originalGraphData.value = data
    
    console.log(`成功加载图数据: ${data.nodes.length} 个节点, ${data.edges.length} 条边`)
    
    // 初始化图表
    await initChart()
    
  } catch (error: any) {
    console.error('加载图数据失败:', error)
    loadingMessage.value = `加载失败: ${error.message || '未知错误'}`
  }
}

// 添加图表初始化函数
const initChart = async (): Promise<void> => {
  if (!graphCanvas.value || !originalGraphData.value) {
    console.error('图表容器或数据未准备好')
    return
  }
  
  try {
    loadingMessage.value = '正在初始化图表...'
    
    // 如果已存在图表实例，先销毁
    if (myChart.value) {
      myChart.value.dispose()
    }
    
    // 创建新的图表实例
    myChart.value = echarts.init(graphCanvas.value, isDarkMode.value ? 'dark' : 'default')
    
    console.log('ECharts 实例创建成功')
    
    // 设置基础图表配置
    const baseOption: EChartsOption = {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          if (params.dataType === 'node') {
            return `<div style="font-size: 12px;">
              <strong>${params.data.name}</strong><br/>
              类型: ${params.data.id.startsWith('doc-') ? '文档' : '关键词'}<br/>
              ID: ${params.data.id}
            </div>`
          } else if (params.dataType === 'edge') {
            return `<div style="font-size: 12px;">
              关系: ${params.data.source} → ${params.data.target}
            </div>`
          }
          return ''
        }
      },
      legend: {
        show: false
      },
      series: [
        {
          type: 'graph',
          layout: 'force',
          data: [],
          edges: [],
          categories: [
            { name: '文档', itemStyle: { color: '#5470c6' } },
            { name: '关键词', itemStyle: { color: '#91cc75' } }
          ],
          roam: true,
          focusNodeAdjacency: true,
          draggable: true,
          symbol: 'circle',
          symbolSize: (value: any, params: any) => {
            return params.data.symbolSize || 20
          },
          edgeSymbol: ['none', 'arrow'],
          edgeSymbolSize: 6,
          edgeLabel: {
            show: false
          },
          lineStyle: {
            opacity: 0.6,
            width: 1,
            curveness: 0.1
          },
          label: {
            show: true,
            position: 'bottom',
            fontSize: 9,
            color: isDarkMode.value ? '#ffffff' : '#333333',
            formatter: (params: any) => {
              const name = params.data.name || ''
              return name.length > 10 ? name.substring(0, 10) + '...' : name
            }
          },
          force: {
            repulsion: 800,
            gravity: 0.1,
            edgeLength: 100,
            layoutAnimation: true
          },
          emphasis: {
            focus: 'adjacency',
            lineStyle: {
              width: 3
            }
          }
        }
      ]
    }
    
    myChart.value.setOption(baseOption)
    
    // 应用初始筛选（这会设置图表数据）
    applyFilters()
    
    // 监听窗口大小变化
    const resizeObserver = new ResizeObserver(() => {  
      if (myChart.value) {
        myChart.value.resize()
      }
    })
    
    resizeObserver.observe(graphCanvas.value)
    
    chartLoaded.value = true
    console.log('图表初始化完成')
    
  } catch (error) {
    console.error('初始化图表失败:', error)
    loadingMessage.value = `初始化失败: ${error.message || '未知错误'}`
  }
}

// 添加应用筛选函数 - 这是渲染图表的核心函数
const applyFilters = (): void => {
  if (!myChart.value || !originalGraphData.value) {
    console.warn('图表实例或原始数据未准备好')
    return
  }
  
  console.log('应用筛选和颜色更新...')
  
  // 筛选显示的节点类型
  const filteredNodes = originalGraphData.value.nodes.filter(node => {
    if (node.id.startsWith('doc-')) {
      return entityTypes.value.find(et => et.name.includes('Document'))?.selected ?? true
    } else if (node.id.startsWith('kw-')) {
      return entityTypes.value.find(et => et.name.includes('Keyword'))?.selected ?? true
    }
    return true
  })
  
  // 筛选对应的边
  const nodeIds = new Set(filteredNodes.map(node => node.id))
  const filteredEdges = originalGraphData.value.edges.filter(edge => 
    nodeIds.has(edge.source) && nodeIds.has(edge.target)
  )
  
  console.log(`筛选结果: ${filteredNodes.length} 个节点, ${filteredEdges.length} 条边`)
  
  // 更新图表配置 - 增强路径高亮效果
  const option: EChartsOption = {
    series: [
      {
        type: 'graph',
        layout: 'force',
        data: filteredNodes.map((node: Node) => {
          const isHighlighted = pathHighlightNodes.value.has(node.id)
          return {
            id: node.id,
            name: node.label,
            symbolSize: isHighlighted ? 25 : (node.size || 20), // 高亮节点更大
            itemStyle: {
              color: getNodeColor(node),
              borderColor: isHighlighted ? '#FF6B35' : undefined,
              borderWidth: isHighlighted ? 3 : 0, // 高亮节点加边框
              shadowColor: isHighlighted ? '#FF6B35' : undefined,
              shadowBlur: isHighlighted ? 10 : 0, // 高亮节点加阴影
              opacity: pathHighlightNodes.value.size > 0 ? (isHighlighted ? 1 : 0.3) : 1 // 非高亮节点半透明
            },
            category: node.id.startsWith('doc-') ? 0 : 1,
            label: {
              show: true,
              position: 'bottom',
              fontSize: isHighlighted ? 11 : 9, // 高亮节点标签更大
              color: isDarkMode.value ? '#ffffff' : '#333333',
              fontWeight: isHighlighted ? 'bold' : 'normal', // 高亮节点标签加粗
              formatter: (params: any) => {
                const name = params.data.name || ''
                return name.length > 10 ? name.substring(0, 10) + '...' : name
              }
            }
          }
        }),
        edges: filteredEdges.map((edge: Edge) => {
          const edgeKey = `${edge.source}-${edge.target}`
          const reverseEdgeKey = `${edge.target}-${edge.source}`
          
          // 检查是否为路径高亮边
          const isHighlighted = pathHighlightEdges.value.has(edgeKey) || 
                               pathHighlightEdges.value.has(reverseEdgeKey)
          
          return {
            source: edge.source,
            target: edge.target,
            lineStyle: {
              width: isHighlighted ? 4 : 1, // 高亮边更粗
              opacity: pathHighlightEdges.value.size > 0 ? (isHighlighted ? 1 : 0.2) : 0.6, // 非高亮边更透明
              color: isHighlighted ? '#FF6B35' : (isDarkMode.value ? '#64748b' : '#94a3b8'), // 高亮边鲜艳颜色
              shadowColor: isHighlighted ? '#FF6B35' : undefined,
              shadowBlur: isHighlighted ? 5 : 0 // 高亮边加阴影
            }
          }
        }),
        categories: [
          { name: '文档', itemStyle: { color: '#5470c6' } },
          { name: '关键词', itemStyle: { color: '#91cc75' } }
        ],
        roam: true,
        focusNodeAdjacency: true,
        draggable: true,
        symbol: 'circle',
        edgeSymbol: ['none', 'arrow'],
        edgeSymbolSize: 8, // 稍微增大箭头
        edgeLabel: {
          show: false
        },
        lineStyle: {
          opacity: 0.6,
          width: 1,
          curveness: 0.1
        },
        force: {
          repulsion: 800,
          gravity: 0.1,
          edgeLength: 100,
          layoutAnimation: true
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 3
          }
        }
      }
    ]
  }
  
  myChart.value.setOption(option, true) // 第二个参数 true 表示不合并，完全替换
  console.log('图表数据更新完成')
}

// 完全重写社区发现函数 - 纯染色逻辑，不重构图
const runCommunityDetection = async (): Promise<void> => {
  // 如果已经激活，则收起
  if (communityActive.value) {
    communityActive.value = false
    communityResults.value = null
    communityNodeColors.value.clear()
    pathHighlightNodes.value.clear()
    pathHighlightEdges.value.clear()
    applyFilters()
    return
  }

  communityLoading.value = true
  try {
    console.log('开始社区发现...')
    const response = await fetch(COMMUNITY_API_URL, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    console.log('社区发现API响应状态:', response.status)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    console.log('社区发现API完整响应:', JSON.stringify(result, null, 2))
    
    // 简化成功检查 - 只要有nodes数据就认为成功
    let nodes = null
    
    // 检查多种可能的数据结构
    if (result && result.nodes && Array.isArray(result.nodes)) {
      nodes = result.nodes
      console.log('从result.nodes提取数据')
    } else if (result && result.data && result.data.nodes && Array.isArray(result.data.nodes)) {
      nodes = result.data.nodes
      console.log('从result.data.nodes提取数据')
    } else if (Array.isArray(result)) {
      nodes = result
      console.log('result本身就是数组')
    }
    
    console.log('提取的节点数据:', nodes)
    console.log('节点数据长度:', nodes ? nodes.length : 0)
    
    if (!nodes || !Array.isArray(nodes) || nodes.length === 0) {
      console.warn('没有找到有效的节点数据')
      alert('社区发现API调用成功，但没有返回有效的节点数据')
      return
    }
    
    console.log('API返回成功，开始处理社区颜色映射...')
    console.log('前3个节点数据示例:', nodes.slice(0, 3))
    
    // 从API返回的节点中提取颜色信息
    const colorToCommunityMap = new Map<string, any[]>()
    const newCommunityNodeColors = new Map<string, string>()
    
    // 按颜色分组节点
    nodes.forEach((apiNode: any, index: number) => {
      const color = apiNode.color
      const nodeId = apiNode.id
      const nodeLabel = apiNode.label || apiNode.name
      
      console.log(`处理节点${index + 1}:`, { id: nodeId, label: nodeLabel, color: color })
      
      if (!color || !nodeId) {
        console.warn('节点缺少必要字段:', apiNode)
        return
      }
      
      if (!colorToCommunityMap.has(color)) {
        colorToCommunityMap.set(color, [])
      }
      colorToCommunityMap.get(color)!.push(apiNode)
      
      // 将颜色映射应用到节点
      newCommunityNodeColors.set(nodeId, color)
      console.log(`节点 ${nodeId}(${nodeLabel}) 分配社区颜色: ${color}`)
    })
    
    // 构建社区统计信息
    const communities = Array.from(colorToCommunityMap.entries()).map(([color, nodes], index) => ({
      id: index + 1,
      nodes: nodes.map(node => node.id),
      color: color
    }))
    
    // 更新状态
    communityResults.value = { communities }
    communityNodeColors.value = newCommunityNodeColors
    communityActive.value = true
    
    console.log('社区发现完成:', {
      社区数量: communities.length,
      着色节点数: newCommunityNodeColors.size,
      社区详情: communities.map(c => ({ 颜色: c.color, 节点数: c.nodes.length }))
    })
    
    // 清除路径高亮
    pathHighlightNodes.value.clear()
    pathHighlightEdges.value.clear()
    
    // 应用新的社区颜色
    applyFilters()
    
    console.log('社区颜色已应用到图表')
  } catch (error) {
    console.error('社区发现失败:', error)
    console.error('错误详情:', error instanceof Error ? error.message : String(error))
    
    // 清除状态
    communityResults.value = null
    communityNodeColors.value.clear()
    communityActive.value = false
    
    const errorMessage = error instanceof Error ? error.message : '网络请求失败'
    alert(`社区发现失败: ${errorMessage}`)
  } finally {
    communityLoading.value = false
  }
}

// 修复路径搜索函数 - 简化错误检查逻辑
const runPathSearch = async (): Promise<void> => {
  pathLoading.value = true
  try {
    console.log('开始BFS路径搜索...')
    console.log('起始节点:', pathSearch.value.startKeyword, '类型:', pathSearch.value.startType)
    console.log('目标节点:', pathSearch.value.endKeyword, '类型:', pathSearch.value.endType)
    
    // 查找节点
    const startNode = originalGraphData.value?.nodes.find(node => {
      const prefix = pathSearch.value.startType === 'KEYWORD' ? 'kw-' : 'doc-'
      return node.id.startsWith(prefix) && node.label === pathSearch.value.startKeyword
    })
    
    const endNode = originalGraphData.value?.nodes.find(node => {
      const prefix = pathSearch.value.endType === 'KEYWORD' ? 'kw-' : 'doc-'
      return node.id.startsWith(prefix) && node.label === pathSearch.value.endKeyword
    })
    
    if (!startNode || !endNode) {
      throw new Error(`未找到节点: ${!startNode ? pathSearch.value.startKeyword : pathSearch.value.endKeyword}`)
    }
    
    console.log('找到起始节点:', startNode)
    console.log('找到目标节点:', endNode)
    
    // 提取数字ID
    const startNumericId = extractNumericId(startNode.id)
    const endNumericId = extractNumericId(endNode.id)
    
    console.log('提取的数字ID:', startNumericId, '->', endNumericId)
    
    const params = new URLSearchParams({
      startId: startNumericId,
      endId: endNumericId
    })
    
    console.log('请求URL:', `${PATH_SEARCH_API_URL}?${params.toString()}`)
    
    const response = await fetch(`${PATH_SEARCH_API_URL}?${params.toString()}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    console.log('BFS路径搜索API响应状态:', response.status)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    console.log('BFS路径搜索API完整响应:', JSON.stringify(result, null, 2))
    
    // 简化成功检查 - 只要有nodes数据就认为成功
    let pathData = null
    
    // 检查多种可能的数据结构
    if (result && result.nodes && Array.isArray(result.nodes)) {
      pathData = { nodes: result.nodes, edges: result.edges || [] }
      console.log('从result直接提取数据')
    } else if (result && result.data && result.data.nodes && Array.isArray(result.data.nodes)) {
      pathData = result.data
      console.log('从result.data提取数据')
    } else if (result && typeof result === 'object') {
      // 尝试从任何包含nodes的对象中提取
      const keys = Object.keys(result)
      for (const key of keys) {
        if (result[key] && result[key].nodes && Array.isArray(result[key].nodes)) {
          pathData = result[key]
          console.log(`从result.${key}提取数据`)
          break
        }
      }
    }
    
    console.log('提取的路径数据:', pathData)
    
    if (!pathData || !pathData.nodes || !Array.isArray(pathData.nodes) || pathData.nodes.length === 0) {
      console.log('API调用成功但没有找到路径')
      pathHighlightNodes.value.clear()
      pathHighlightEdges.value.clear()
      pathResults.value = null
      applyFilters()
      alert('未找到连接这两个节点的路径')
      return
    }
    
    pathResults.value = pathData
    
    console.log('找到路径:', pathData.nodes.map((n: any) => `${n.id}(${n.label})`))
    
    // 高亮路径节点
    const highlightNodes = new Set(pathData.nodes.map((n: any) => n.id))
    
    // 处理路径边
    const highlightEdges = new Set<string>()
    if (pathData.edges && Array.isArray(pathData.edges) && pathData.edges.length > 0) {
      console.log('处理API返回的边数据:', pathData.edges)
      
      // 创建数字ID到完整ID的映射
      const numericToFullIdMap = new Map<string, string>()
      if (originalGraphData.value) {
        originalGraphData.value.nodes.forEach(node => {
          const numericId = extractNumericId(node.id)
          numericToFullIdMap.set(numericId, node.id)
        })
      }
      
      pathData.edges.forEach((edge: any) => {
        console.log('处理边:', edge)
        const sourceFullId = numericToFullIdMap.get(edge.source) || edge.source
        const targetFullId = numericToFullIdMap.get(edge.target) || edge.target
        
        console.log(`边映射: ${edge.source}→${sourceFullId}, ${edge.target}→${targetFullId}`)
        
        highlightEdges.add(`${sourceFullId}-${targetFullId}`)
        highlightEdges.add(`${targetFullId}-${sourceFullId}`)
      })
    } else {
      console.log('没有边数据，根据节点顺序生成边')
      // 根据节点顺序生成边
      for (let i = 0; i < pathData.nodes.length - 1; i++) {
        const currentNode = pathData.nodes[i]
        const nextNode = pathData.nodes[i + 1]
        highlightEdges.add(`${currentNode.id}-${nextNode.id}`)
        highlightEdges.add(`${nextNode.id}-${currentNode.id}`)
      }
    }
    
    pathHighlightNodes.value = highlightNodes
    pathHighlightEdges.value = highlightEdges
    
    console.log('路径高亮节点:', Array.from(highlightNodes))
    console.log('路径高亮边:', Array.from(highlightEdges))
    
    // 更新图表
    applyFilters()
    
    console.log('BFS路径搜索完成')
  } catch (error) {
    console.error('BFS路径搜索失败:', error)
    console.error('错误详情:', error instanceof Error ? error.message : String(error))
    
    pathHighlightNodes.value.clear()
    pathHighlightEdges.value.clear()
    pathResults.value = null
    applyFilters()
    
    const errorMessage = error instanceof Error ? error.message : '网络请求失败'
    alert(`路径搜索失败: ${errorMessage}`)
  } finally {
    pathLoading.value = false
  }
}

// 异常检测函数
const runAnomalyDetection = async (): Promise<void> => {
  // 如果已经激活，则收起
  if (anomalyActive.value) {
    anomalyActive.value = false
    anomalyResults.value = null
    return
  }

  anomalyLoading.value = true
  try {
    console.log('开始异常检测...')
    const response = await fetch(ANOMALY_API_URL, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result: AnomalyResponse = await response.json()
    console.log('异常检测API响应:', result)
    
    if (result.code === 'SUCCESS') {
      anomalyResults.value = result.data
      anomalyActive.value = true
      console.log('异常检测结果:', result.data)
    } else {
      throw new Error(result.message || '异常检测失败')
    }
  } catch (error) {
    console.error('异常检测失败:', error)
    anomalyResults.value = null
    anomalyActive.value = false
    alert(`异常检测失败: ${error.message || '网络请求失败'}`)
  } finally {
    anomalyLoading.value = false
  }
}

// 刷新数据函数
const refreshData = async (): Promise<void> => {
  console.log('刷新图数据')
  chartLoaded.value = false
  loadingMessage.value = '正在刷新数据...'
  
  // 清除所有状态
  anomalyResults.value = null
  anomalyActive.value = false
  communityResults.value = null
  communityActive.value = false
  communityNodeColors.value.clear()
  pathResults.value = null
  pathHighlightNodes.value.clear()
  pathHighlightEdges.value.clear()
  
  await loadGraphData()
}

// 监听主题变化
watch(isDarkMode, () => {
  if (myChart.value) {
    // 重新初始化图表以应用新主题
    initChart()
  }
})

// 监听实体类型变化，重新应用筛选
watch(entityTypes, () => {
  applyFilters()
}, { deep: true })

// 页面加载时初始化
onMounted(async () => {
  console.log('Query页面已挂载')
  await nextTick()
  await loadGraphData()
})

// 页面卸载时清理
onUnmounted(() => {
  if (myChart.value) {
    myChart.value.dispose()
  }
})
</script>

<style scoped>
/* 保持原有样式 */
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

/* 异常检测样式 */
.anomaly-detection {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.dark-mode .anomaly-detection {
  border-top-color: #334155 !important;
}

.anomaly-header h4 {
  margin: 0 0 0.8rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: inherit;
}

.dark-mode .anomaly-header h4 {
  color: #e2e8f0 !important;
}

.anomaly-btn {
  width: 100%;
  padding: 0.6rem 1rem;
  background: #0ea5e9;
  color: white;
  border: none;
  border-radius: 0.375rem;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-bottom: 0.8rem;
}

.anomaly-btn:hover {
  background: #0284c7;
}

.anomaly-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.anomaly-btn.loading {
  background: #94a3b8;
}

.dark-mode .anomaly-btn {
  background: #38bdf8;
}

.dark-mode .anomaly-btn:hover {
  background: #0ea5e9;
}

.anomaly-btn.active {
  background: #0284c7;
}

.dark-mode .anomaly-btn.active {
  background: #0ea5e9;
}

.anomaly-results {
  background: #f8fafc;
  border-radius: 0.375rem;
  padding: 0.6rem;
  border: 1px solid #e2e8f0;
}

.dark-mode .anomaly-results {
  background: #334155 !important;
  border-color: #475569 !important;
}

/* 社区发现样式 */
.community-detection {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.dark-mode .community-detection {
  border-top-color: #334155 !important;
}

.community-header h4 {
  margin: 0 0 0.8rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: inherit;
}

.dark-mode .community-header h4 {
  color: #e2e8f0 !important;
}

.community-btn {
  width: 100%;
  padding: 0.6rem 1rem;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 0.375rem;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-bottom: 0.8rem;
}

.community-btn:hover {
  background: #059669;
}

.community-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.community-btn.loading {
  background: #94a3b8;
}

.dark-mode .community-btn {
  background: #34d399;
}

.dark-mode .community-btn:hover {
  background: #10b981;
}

.community-btn.active {
  background: #059669;
}

.dark-mode .community-btn.active {
  background: #10b981;
}

.community-results {
  background: #f0fdf4;
  border-radius: 0.375rem;
  padding: 0.8rem;
  border: 1px solid #bbf7d0;
  margin-top: 0.5rem;
}

.dark-mode .community-results {
  background: #14532d !important;
  border-color: #166534 !important;
}

.community-details {
  margin-top: 0.8rem;
  padding-top: 0.6rem;
  border-top: 1px solid #bbf7d0;
}

.dark-mode .community-details {
  border-top-color: #166534 !important;
}

.community-list-header {
  margin-bottom: 0.5rem;
}

.community-list-title {
  font-size: 0.7rem;
  font-weight: 600;
  color: #059669;
}

.dark-mode .community-list-title {
  color: #34d399 !important;
}

.community-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.4rem;
  gap: 0.6rem;
  padding: 0.3rem;
  border-radius: 0.25rem;
  transition: background-color 0.2s ease;
}

.community-item:hover {
  background: rgba(16, 185, 129, 0.1);
}

.dark-mode .community-item:hover {
  background: rgba(52, 211, 153, 0.1) !important;
}

.community-color {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.dark-mode .community-color {
  border-color: #14532d;
}

.community-text {
  color: #059669;
  font-size: 0.75rem;
  font-weight: 500;
  flex: 1;
}

.dark-mode .community-text {
  color: #34d399 !important;
}

/* 路径搜索样式 */
.path-search {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.dark-mode .path-search {
  border-top-color: #334155 !important;
}

.path-header h4 {
  margin: 0 0 0.8rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: inherit;
}

.dark-mode .path-header h4 {
  color: #e2e8f0 !important;
}

.search-inputs {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  margin-bottom: 0.8rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.input-label {
  font-size: 0.7rem;
  color: #64748b;
  font-weight: 500;
}

.dark-mode .input-label {
  color: #94a3b8;
}

.type-select {
  padding: 0.4rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.25rem;
  font-size: 0.7rem;
  background: white;
  color: inherit;
  cursor: pointer;
}

.dark-mode .type-select {
  background: #475569;
  border-color: #64748b;
  color: #e2e8f0;
}

.path-select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  font-size: 0.75rem;
  background: white;
  color: inherit;
  cursor: pointer;
}

.dark-mode .path-select {
  background: #475569 !important;
  border-color: #64748b !important;
  color: #e2e8f0 !important;
}

.path-select option {
  background: white;
  color: #333;
}

.dark-mode .path-select option {
  background: #475569;
  color: #e2e8f0;
}

.quick-select {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.8rem;
  font-size: 0.7rem;
}

.quick-label {
  color: #64748b;
  font-weight: 500;
}

.dark-mode .quick-label {
  color: #94a3b8;
}

.quick-btn {
  padding: 0.3rem 0.6rem;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 0.25rem;
  font-size: 0.7rem;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #64748b;
}

.quick-btn:hover {
  background: #e2e8f0;
  color: #334155;
}

.dark-mode .quick-btn {
  background: #475569;
  border-color: #64748b;
  color: #94a3b8;
}

.dark-mode .quick-btn:hover {
  background: #64748b;
  color: #e2e8f0;
}

.path-btn {
  width: 100%;
  padding: 0.6rem 1rem;
  background: #8b5cf6;
  color: white;
  border: none;
  border-radius: 0.375rem;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-bottom: 0.8rem;
}

.path-btn:hover {
  background: #7c3aed;
}

.path-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.path-btn.loading {
  background: #94a3b8;
}

.dark-mode .path-btn {
  background: #a78bfa;
}

.dark-mode .path-btn:hover {
  background: #8b5cf6;
}

.path-results {
  background: #faf5ff;
  border-radius: 0.375rem;
  padding: 0.6rem;
  border: 1px solid #e9d5ff;
}

.dark-mode .path-results {
  background: #44337a !important;
  border-color: #6d28d9 !important;
}

.path-nodes {
  margin-top: 0.6rem;
  padding-top: 0.6rem;
  border-top: 1px solid #e9d5ff;
}

.dark-mode .path-nodes {
  border-top-color: #6d28d9 !important;
}

.path-node {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  margin-bottom: 0.3rem;
  font-size: 0.7rem;
  color: #7c3aed;
  font-weight: 500;
}

.dark-mode .path-node {
  color: #a78bfa !important;
}

.node-type {
  font-size: 0.6rem;
  color: #8b5cf6;
  font-weight: 600;
  margin-right: 0.2rem;
}

.dark-mode .node-type {
  color: #a78bfa;
}

.arrow {
  color: #8b5cf6;
  font-weight: bold;
  margin: 0 0.2rem;
}

.dark-mode .arrow {
  color: #a78bfa;
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.4rem;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-label {
  font-size: 0.7rem;
  color: #64748b;
}

.dark-mode .result-label {
  color: #94a3b8 !important;
}

.result-value {
  font-size: 0.7rem;
  font-weight: 600;
  color: #10b981;
}

.result-value.has-issue {
  color: #ef4444;
}

.dark-mode .result-value {
  color: #34d399 !important;
}

.dark-mode .result-value.has-issue {
  color: #f87171 !important;
}

/* 图表容器样式 */
.graph-container {
  flex: 1;
  background: white;
  transition: background-color 0.3s ease;
  overflow: hidden;
  position: relative;
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

/* 其他通用样式 */
input[type="checkbox"] {
  accent-color: #0ea5e9;
  transform: scale(0.8);
}

.dark-mode input[type="checkbox"] {
  accent-color: #38bdf8;
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

/* 调试信息样式 */
.debug-info {
  margin-top: 0.8rem;
  padding: 0.5rem;
  background: #f8fafc;
  border-radius: 0.25rem;
  border: 1px solid #e2e8f0;
  font-size: 0.6rem;
  max-height: 200px;
  overflow-y: auto;
}

.debug-info pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.dark-mode .debug-info {
  background: #334155;
  border-color: #475569;
  color: #e2e8f0;
}
</style>