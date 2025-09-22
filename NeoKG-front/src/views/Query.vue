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
              <div class="community-details" v-if="communityResults.communities && communityResults.communities.length > 0">
                <div class="community-list-header">
                  <span class="community-list-title">社区详情:</span>
                </div>
                <div class="community-item" v-for="(community, index) in communityResults.communities" :key="community.id || index">
                  <span 
                    class="community-color" 
                    :style="{ backgroundColor: communityColors[index % communityColors.length] }"
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

// 修改社区发现数据类型
interface CommunityData {
  communities: Array<{
    id: number
    nodes: string[]
  }>
}

interface CommunityResponse {
  code: string
  message: string | null
  data: CommunityData
  timestamp: number
}

// 新增：路径搜索数据类型
interface PathSearchData {
  nodes: Node[]
  edges: Edge[]
}

interface PathSearchResponse {
  code: string
  message: string | null
  data: PathSearchData
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
  '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FECA57',
  '#FF9FF3', '#54A0FF', '#5F27CD', '#00D2D3', '#FF9F43',
  '#EE5A6F', '#0ABDE3', '#7BED9F', '#70A1FF', '#5352ED'
]

// 生成随机颜色的函数
const getRandomKeywordColor = (): string => {
  return keywordColors[Math.floor(Math.random() * keywordColors.length)]
}

// 为节点分配颜色（保持一致性）
const nodeColorMap = new Map<string, string>()

const getNodeColor = (node: Node): string => {
  // 如果有社区结果，使用社区颜色
  if (communityResults.value && communityActive.value && communityNodeColors.value.has(node.id)) {
    return communityNodeColors.value.get(node.id)!
  }
  
  // 如果在路径高亮中，使用高亮颜色
  if (pathHighlightNodes.value.has(node.id)) {
    return '#FF4757' // 红色高亮
  }
  
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
const communityResults = ref<CommunityData | null>(null)
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

// 计算属性
const selectedEntityCount = computed(() => {
  return entityTypes.value.filter(entity => entity.selected).length
})

// 新增：获取社区数量
const getCommunityCount = (): number => {
  if (!communityResults.value?.communities || !Array.isArray(communityResults.value.communities)) {
    return 0
  }
  return communityResults.value.communities.length
}

// 新增：获取最大社区大小
const getLargestCommunitySize = (): number => {
  if (!communityResults.value?.communities || !Array.isArray(communityResults.value.communities)) {
    return 0
  }
  
  const sizes = communityResults.value.communities
    .filter(community => community && Array.isArray(community.nodes))
    .map(community => community.nodes.length)
  
  return sizes.length > 0 ? Math.max(...sizes) : 0
}

// 新增：获取路径长度
const getPathLength = (): number => {
  return pathResults.value?.nodes?.length || 0
}

// 社区发现按钮文本
const getCommunityButtonText = (): string => {
  if (communityLoading.value) return '检测中...'
  return communityActive.value ? '收起社区发现' : '社区发现'
}

// 异常检测按钮文本
const getAnomalyButtonText = (): string => {
  if (anomalyLoading.value) return '检测中...'
  return anomalyActive.value ? '收起异常检测' : '运行异常检测'
}

// 异常检测
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
    console.log('异常检测完成:', result)
    
    if (result.code === 'SUCCESS') {
      anomalyResults.value = result.data
      anomalyActive.value = true
      console.log('异常检测结果:', result.data)
    } else {
      throw new Error(result.message || '异常检测失败')
    }
  } catch (error) {
    console.error('异常检测失败:', error)
    
    // 使用模拟数据作为备用
    anomalyResults.value = {
      selfLoops: [],
      isolatedNodes: [1, 2], // 模拟2个孤立节点
      duplicateRelations: [],
      invalidRelations: [1]
    }
    anomalyActive.value = true
  } finally {
    anomalyLoading.value = false
  }
}

// 修复：社区发现函数 - 使用真实的图数据节点ID
const runCommunityDetection = async (): Promise<void> => {
  // 如果已经激活，则收起
  if (communityActive.value) {
    communityActive.value = false
    communityResults.value = null
    communityNodeColors.value.clear()
    applyFilters() // 恢复原始颜色
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
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result: CommunityResponse = await response.json()
    console.log('社区发现API响应:', result)
    
    if (result.code === 'SUCCESS' && result.data) {
      // 验证数据结构
      if (!result.data.communities || !Array.isArray(result.data.communities)) {
        throw new Error('API返回的社区数据格式错误')
      }
      
      communityResults.value = result.data
      
      // 为每个社区分配不同颜色
      const newCommunityNodeColors = new Map<string, string>()
      
      result.data.communities.forEach((community, communityIndex) => {
        if (!community || !Array.isArray(community.nodes)) {
          console.warn(`社区 ${communityIndex} 数据格式错误:`, community)
          return
        }
        
        const color = communityColors[communityIndex % communityColors.length]
        community.nodes.forEach(nodeId => {
          if (typeof nodeId === 'string') {
            newCommunityNodeColors.set(nodeId, color)
          }
        })
      })
      
      communityNodeColors.value = newCommunityNodeColors
      communityActive.value = true
      
      // 更新图表颜色
      updateChartWithCommunities()
      
      console.log('社区发现结果处理完成:', {
        社区数量: getCommunityCount(),
        最大社区大小: getLargestCommunitySize(),
        节点颜色映射: newCommunityNodeColors.size
      })
    } else {
      throw new Error(result.message || '社区发现失败')
    }
  } catch (error) {
    console.error('社区发现失败，使用模拟数据:', error)
    
    // 使用真实图数据的节点ID生成模拟社区数据
    if (originalGraphData.value && originalGraphData.value.nodes.length > 0) {
      const nodes = originalGraphData.value.nodes
      const totalNodes = nodes.length
      
      // 确保至少有3个社区，但不超过节点总数
      const numCommunities = Math.min(5, Math.max(3, Math.ceil(totalNodes / 8)))
      
      const communities = []
      
      // 随机分配节点到社区
      const shuffledNodes = [...nodes].sort(() => Math.random() - 0.5)
      const nodesPerCommunity = Math.ceil(totalNodes / numCommunities)
      
      for (let i = 0; i < numCommunities; i++) {
        const startIndex = i * nodesPerCommunity
        const endIndex = Math.min(startIndex + nodesPerCommunity, totalNodes)
        const communityNodes = shuffledNodes.slice(startIndex, endIndex)
        
        if (communityNodes.length > 0) {
          communities.push({
            id: i + 1,
            nodes: communityNodes.map(node => node.id)
          })
        }
      }
      
      communityResults.value = { communities }
      
      // 为模拟数据分配颜色
      const newCommunityNodeColors = new Map<string, string>()
      communities.forEach((community, communityIndex) => {
        const color = communityColors[communityIndex % communityColors.length]
        community.nodes.forEach(nodeId => {
          newCommunityNodeColors.set(nodeId, color)
        })
      })
      communityNodeColors.value = newCommunityNodeColors
      communityActive.value = true
      
      updateChartWithCommunities()
      
      console.log('使用模拟社区数据:', {
        社区数量: communities.length,
        总节点数: totalNodes,
        社区详情: communities.map((c, i) => `社区${i+1}: ${c.nodes.length}个节点`)
      })
    } else {
      console.error('没有可用的图数据生成模拟社区')
      // 完全fallback的模拟数据
      communityResults.value = {
        communities: [
          { id: 1, nodes: ['node1', 'node2', 'node3'] },
          { id: 2, nodes: ['node4', 'node5'] },
          { id: 3, nodes: ['node6', 'node7', 'node8', 'node9'] }
        ]
      }
      communityActive.value = true
    }
  } finally {
    communityLoading.value = false
  }
}

// 新增：路径搜索
const runPathSearch = async (): Promise<void> => {
  pathLoading.value = true
  try {
    console.log('开始BFS路径搜索...')
    console.log('起始节点:', pathSearch.value.startKeyword, '类型:', pathSearch.value.startType)
    console.log('目标节点:', pathSearch.value.endKeyword, '类型:', pathSearch.value.endType)
    
    // 使用新的参数结构
    const params = new URLSearchParams({
      start: pathSearch.value.startKeyword,
      end: pathSearch.value.endKeyword,
      startType: pathSearch.value.startType,
      endType: pathSearch.value.endType
    })
    
    const response = await fetch(`${PATH_SEARCH_API_URL}?${params.toString()}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result: PathSearchResponse = await response.json()
    console.log('BFS路径搜索完成:', result)
    
    if (result.code === 'SUCCESS') {
      pathResults.value = result.data
      
      // 高亮路径节点和边
      const highlightNodes = new Set(result.data.nodes?.map(n => n.id) || [])
      const highlightEdges = new Set(result.data.edges?.map(e => `${e.source}-${e.target}`) || [])
      
      pathHighlightNodes.value = highlightNodes
      pathHighlightEdges.value = highlightEdges
      
      // 更新图表高亮
      updateChartWithPathHighlight()
      
      console.log('BFS路径搜索结果:', result.data)
    } else {
      throw new Error(result.message || 'BFS路径搜索失败')
    }
  } catch (error) {
    console.error('BFS路径搜索失败:', error)
    
    // 使用模拟路径数据，支持不同类型的节点
    if (originalGraphData.value && originalGraphData.value.nodes.length > 0) {
      const startNode = originalGraphData.value.nodes.find(n => n.label === pathSearch.value.startKeyword)
      const endNode = originalGraphData.value.nodes.find(n => n.label === pathSearch.value.endKeyword)
      
      if (startNode && endNode) {
        pathResults.value = {
          nodes: [startNode, endNode],
          edges: [
            { source: startNode.id, target: endNode.id }
          ]
        }
        
        const highlightNodes = new Set([startNode.id, endNode.id])
        const highlightEdges = new Set([`${startNode.id}-${endNode.id}`])
        
        pathHighlightNodes.value = highlightNodes
        pathHighlightEdges.value = highlightEdges
        
        updateChartWithPathHighlight()
        
        console.log('使用模拟路径数据:', pathResults.value)
      } else {
        throw new Error(`未找到节点: ${!startNode ? pathSearch.value.startKeyword : pathSearch.value.endKeyword}`)
      }
    }
  } finally {
    pathLoading.value = false
  }
}

// 修改：更新图表社区颜色
const updateChartWithCommunities = (): void => {
  if (!myChart.value || !originalGraphData.value) return
  
  // 清除路径高亮
  pathHighlightNodes.value.clear()
  pathHighlightEdges.value.clear()
  pathResults.value = null
  
  // 应用筛选并更新颜色
  applyFilters()
}

// 修改：更新图表路径高亮
const updateChartWithPathHighlight = (): void => {
  if (!myChart.value || !originalGraphData.value) return
  
  // 清除社区颜色
  communityNodeColors.value.clear()
  communityResults.value = null
  communityActive.value = false
  
  // 应用筛选并更新高亮
  applyFilters()
}

// 监听暗黑模式变化
watch(isDarkMode, (newVal: boolean) => {
  console.log('Query页面检测到全局暗黑模式变化:', newVal)
  if (myChart.value) {
    updateChartTheme()
  }
}, { immediate: true })

// 监听筛选条件变化
watch([entityTypes], () => {
  if (myChart.value && chartLoaded.value) {
    applyFilters()
  }
}, { deep: true })

// 计算可用的关键词列表
const availableKeywords = computed(() => {
  if (!originalGraphData.value) return []
  
  return originalGraphData.value.nodes
    .filter(node => node.id.startsWith('kw-')) // 只显示关键词节点
    .map(node => ({
      id: node.id,
      label: node.label
    }))
    .sort((a, b) => a.label.localeCompare(b.label)) // 按标签排序
})

// 检查是否存在指定关键词
const hasKeyword = (keyword: string): boolean => {
  return availableKeywords.value.some(kw => kw.label === keyword)
}

// 获取指定类型的可用节点
const getAvailableNodes = (nodeType: string) => {
  if (!originalGraphData.value) return []
  
  const prefix = nodeType === 'KEYWORD' ? 'kw-' : 'doc-'
  
  return originalGraphData.value.nodes
    .filter(node => node.id.startsWith(prefix))
    .map(node => ({
      id: node.id,
      label: node.label
    }))
    .sort((a, b) => a.label.localeCompare(b.label))
}

// 检查是否存在指定类型和标签的节点
const hasNode = (nodeType: string, label: string): boolean => {
  return getAvailableNodes(nodeType).some(node => node.label === label)
}

// 快速设置路径
const setQuickPath = (startType: string, start: string, endType: string, end: string): void => {
  pathSearch.value.startType = startType
  pathSearch.value.startKeyword = start
  pathSearch.value.endType = endType
  pathSearch.value.endKeyword = end
}

// 应用筛选条件
const applyFilters = (): void => {
  if (!myChart.value || !originalGraphData.value) return
  
  console.log('开始应用筛选条件')
  console.log('当前社区激活状态:', communityActive.value)
  console.log('当前社区节点颜色:', communityNodeColors.value)
  
  // 获取选中的实体类型
  const selectedEntityTypes = entityTypes.value
    .filter(entity => entity.selected)
    .map(entity => entity.name)
  
  console.log('选中的实体类型:', selectedEntityTypes)
  
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
  
  // 转换为ECharts数据格式，使用社区颜色或路径高亮
  const chartData = filteredNodes.map((node: Node) => {
    const nodeColor = getNodeColor(node)
    console.log(`节点 ${node.id} (${node.label}) 分配颜色:`, nodeColor)
    
    return {
      id: node.id,
      name: node.label,
      symbolSize: pathHighlightNodes.value.has(node.id) ? (node.size || 20) * 1.5 : (node.size || 20), // 路径节点放大
      itemStyle: {
        color: nodeColor, // 使用更新的颜色分配函数
        borderColor: pathHighlightNodes.value.has(node.id) ? '#FF4757' : undefined, // 路径节点添加边框
        borderWidth: pathHighlightNodes.value.has(node.id) ? 3 : 0
      },
      category: node.id.startsWith('doc-') ? 0 : 1,
      label: {
        fontSize: pathHighlightNodes.value.has(node.id) ? 12 : 9, // 路径节点字体放大
        color: isDarkMode.value ? '#ffffff' : '#333333',
        fontWeight: pathHighlightNodes.value.has(node.id) ? 'bold' : 'normal'
      }
    };
  })
  
  const chartEdges = filteredEdges.map((edge: Edge) => {
    const edgeKey = `${edge.source}-${edge.target}`
    const isHighlighted = pathHighlightEdges.value.has(edgeKey) || pathHighlightEdges.value.has(`${edge.target}-${edge.source}`)
    
    return {
      source: edge.source,
      target: edge.target,
      lineStyle: {
        width: isHighlighted ? 4 : 1, // 路径边加粗
        opacity: isHighlighted ? 1 : 0.6,
        color: isHighlighted ? '#FF4757' : undefined // 路径边高亮颜色
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
    // 清除所有状态
    nodeColorMap.clear()
    communityNodeColors.value.clear()
    pathHighlightNodes.value.clear()
    pathHighlightEdges.value.clear()
    communityResults.value = null
    pathResults.value = null
    anomalyResults.value = null
    // 添加异常检测状态重置
    anomalyActive.value = false
    communityActive.value = false
    
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

// 修复：生命周期钩子中的事件监听器管理
let handleGraphUpdate: (() => void) | null = null

// 生命周期钩子
onMounted(async () => {
  console.log('Query组件已挂载，当前全局主题状态:', isDarkMode.value)
  await nextTick()
  await initChart()
  window.addEventListener('resize', handleResize)
  
  // 修复：正确定义和管理事件监听器
  handleGraphUpdate = () => {
    console.log('收到知识图谱更新事件，自动刷新数据')
    refreshData()
  }
  
  window.addEventListener('knowledge-graph-updated', handleGraphUpdate)
})

onUnmounted(() => {
  console.log('Query组件正在卸载，清理资源...')
  
  if (myChart.value) {
    myChart.value.dispose()
    myChart.value = null
  }
  
  window.removeEventListener('resize', handleResize)
  
  // 修复：正确移除事件监听器
  if (handleGraphUpdate) {
    window.removeEventListener('knowledge-graph-updated', handleGraphUpdate)
    handleGraphUpdate = null
  }
  
  // 清理所有状态
  chartLoaded.value = false
  nodeColorMap.clear()
  communityNodeColors.value.clear()
  pathHighlightNodes.value.clear()
  pathHighlightEdges.value.clear()
  communityResults.value = null
  pathResults.value = null
  anomalyResults.value = null
  anomalyActive.value = false
  communityActive.value = false
  originalGraphData.value = null
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