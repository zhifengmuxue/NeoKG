<template>
  <div class="dashboard" :style="{ backgroundColor: isDark ? '#1f1f1f' : '#ffffff' }">
    <!-- 统计卡片区域 -->
    <div class="stats-container">
      <div class="stat-item" :style="getStatItemStyle()">
        <div class="stat-icon density-icon" :style="getIconStyle('density')">🕸️</div>
        <div class="stat-content">
          <div class="stat-label" :style="{ color: isDark ? '#b3b3b3' : '#666' }">图谱密度</div>
          <div class="stat-value" :style="{ color: isDark ? '#ffffff' : '#333' }">
            {{ loading ? '加载中...' : formatDensity(metricsData.density) }}
          </div>
        </div>
      </div>
      
      <div class="stat-item" :style="getStatItemStyle()">
        <div class="stat-icon connectivity-icon" :style="getIconStyle('connectivity')">🔗</div>
        <div class="stat-content">
          <div class="stat-label" :style="{ color: isDark ? '#b3b3b3' : '#666' }">连通组件</div>
          <div class="stat-value" :style="{ color: isDark ? '#ffffff' : '#333' }">
            {{ loading ? '加载中...' : getConnectivityCount(metricsData.connectivity) }}
          </div>
        </div>
      </div>
      
      <!-- 异常检测卡片 -->
      <div class="stat-item" :style="getStatItemStyle()">
        <div class="stat-icon anomaly-icon" :style="getIconStyle('anomaly')">⚠️</div>
        <div class="stat-content">
          <div class="stat-label" :style="{ color: isDark ? '#b3b3b3' : '#666' }">异常检测</div>
          <div class="stat-value" :style="{ color: anomalyLoading ? (isDark ? '#ffffff' : '#333') : getAnomalyColor() }">
            {{ anomalyLoading ? '检测中...' : getTotalAnomaliesText() }}
          </div>
        </div>
      </div>
      
      <div class="stat-item" :style="getStatItemStyle()">
        <div class="stat-icon isolated-icon" :style="getIconStyle('isolated')">⚪</div>
        <div class="stat-content">
          <div class="stat-label" :style="{ color: isDark ? '#b3b3b3' : '#666' }">孤立结点比例</div>
          <div class="stat-value" :style="{ color: isDark ? '#ffffff' : '#333' }">
            {{ loading ? '加载中...' : formatIsolatedRatio(metricsData.isolatedRatio) }}
          </div>
        </div>
      </div>
      
      <div class="stat-item" :style="getStatItemStyle()">
        <div class="stat-icon file-icon" :style="getIconStyle('file')">📁</div>
        <div class="stat-content">
          <div class="stat-label" :style="{ color: isDark ? '#b3b3b3' : '#666' }">文档实体量</div>
          <div class="stat-value" :style="{ color: isDark ? '#ffffff' : '#333' }">
            {{ fileLoading ? '加载中...' : fileCount }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-container">
      <div class="chart-section" :style="getChartSectionStyle()">
        <h3 :style="getChartTitleStyle()">文档实体量趋势</h3>
        <!-- ECharts 图表容器 -->
        <div ref="fileChartRef" class="chart-container"></div>
      </div>
      
      <div class="chart-section" :style="getChartSectionStyle()">
        <h3 :style="getChartTitleStyle()">关键词降维分布图</h3>
        <!-- ECharts 图表容器 -->
        <div ref="queryChartRef" class="chart-container"></div>
      </div>
    </div>
    
    <!-- 最近活动 -->
    <!-- <div class="activity-section" :style="getActivitySectionStyle()">
      <h3 :style="getChartTitleStyle()">最近活动</h3>
      <div class="activity-table">
        <div class="table-header" :style="getTableHeaderStyle()">
          <span>操作</span>
          <span>时间</span>
          <span>状态</span>
        </div>
      </div>
    </div> -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import * as echarts from 'echarts'
import { isDarkMode } from '@/stores/theme'

type EChartsOption = echarts.EChartsOption

// 图表实例引用
const fileChartRef = ref<HTMLDivElement>()
const queryChartRef = ref<HTMLDivElement>()

// 使用全局主题状态
const isDark = computed(() => isDarkMode.value)
const loading = ref(false)
const fileLoading = ref(false)
const fileCount = ref<string>('0')

// 文件统计数据加载状态
const fileStatsLoading = ref(false)

// 降维数据加载状态
const dimReduceLoading = ref(false)
const dimReplaceLoading = ref(false)
// 异常检测加载状态
const anomalyLoading = ref(false)

// 文件统计数据类型定义
interface FileStatItem {
  count: number
  type: string
  day: string
}

interface FileStatsResponse {
  code: string
  message: string | null
  data: FileStatItem[]
  timestamp: number
}

// 降维数据类型定义
interface Vec2D {
  id: number
  name: string
  x: number
  y: number
}

interface DimReduceResponse {
  code: string
  message: string | null
  data: Vec2D[]
  timestamp: number
}

// 降维替换接口响应类型
interface DimReplaceResponse {
  code: string
  message: string | null
  data: string
  timestamp: number
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

// 文件统计数据状态
const fileStatsData = ref<FileStatItem[]>([])
// 降维数据状态
const dimReduceData = ref<Vec2D[]>([])
// 异常检测数据状态
const anomalyData = ref<AnomalyData>({
  selfLoops: [],
  isolatedNodes: [],
  duplicateRelations: [],
  invalidRelations: []
})

// 图谱分析数据类型
interface ConnectivityComponent {
  componentId: number
  size: number
}

interface MetricsData {
  isolatedRatio: number
  density: number
  connectivity: ConnectivityComponent[]
}

interface ApiResponse {
  code: string
  message: string | null
  data: MetricsData
  timestamp: number
}

interface FileCountResponse {
  code: string
  message: string | null
  data: string
  timestamp: number
}

const metricsData = ref<MetricsData>({
  isolatedRatio: 0,
  density: 0,
  connectivity: []
})

// API配置
const API_BASE_URL = import.meta.env.DEV ? '/api/graph/analysis/metrics' : 'http://localhost:8080/api/graph/analysis/metrics'
const FILE_COUNT_API_URL = import.meta.env.DEV ? '/api/file/num' : 'http://localhost:8080/api/file/num'
const FILE_STATS_API_URL = import.meta.env.DEV ? '/api/file/stats/weekly' : 'http://localhost:8080/api/file/stats/weekly'
const DIM_REDUCE_API_URL = import.meta.env.DEV ? '/api/dim/all' : 'http://localhost:8080/api/dim/all'
const DIM_REPLACE_API_URL = import.meta.env.DEV ? '/api/dim/replaceAll' : 'http://localhost:8080/api/dim/replaceAll'
const ANOMALY_API_URL = import.meta.env.DEV ? '/api/graph/analysis/anomalies' : 'http://localhost:8080/api/graph/analysis/anomalies'

// 获取文件统计数据
const fetchFileStats = async (): Promise<void> => {
  fileStatsLoading.value = true
  try {
    console.log('正在获取文件统计数据...')
    const response = await fetch(FILE_STATS_API_URL, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result: FileStatsResponse = await response.json()
    console.log('文件统计数据获取成功:', result)
    
    if (result.code === 'SUCCESS') {
      fileStatsData.value = result.data
      console.log('文件统计数据:', result.data)
    } else {
      throw new Error(result.message || '获取文件统计数据失败')
    }
  } catch (error) {
    console.error('获取文件统计数据失败:', error)
    
    // 使用模拟数据作为备用
    fileStatsData.value = [
      { count: 2, type: 'CSV', day: '2025-09-22T16:00:00.000+00:00' },
      { count: 1, type: 'MARKDOWN', day: '2025-09-22T16:00:00.000+00:00' },
      { count: 5, type: 'WORD', day: '2025-09-22T16:00:00.000+00:00' }
    ]
  } finally {
    fileStatsLoading.value = false
  }
}

// 获取异常检测数据
const fetchAnomalies = async (): Promise<void> => {
  anomalyLoading.value = true
  try {
    console.log('正在获取异常检测数据...')
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
    console.log('异常检测数据获取成功:', result)
    
    if (result.code === 'SUCCESS') {
      anomalyData.value = result.data
      console.log('异常检测结果:', result.data)
    } else {
      // 如果返回缓存错误，使用空数据而不是模拟数据
      if (result.code === 'CACHE_ERROR') {
        console.warn('异常检测数据尚未生成:', result.message)
        anomalyData.value = {
          selfLoops: [],
          isolatedNodes: [],
          duplicateRelations: [],
          invalidRelations: []
        }
      } else {
        throw new Error(result.message || '获取异常检测数据失败')
      }
    }
  } catch (error) {
    console.error('获取异常检测数据失败:', error)
    
    // 使用空数据作为备用
    anomalyData.value = {
      selfLoops: [],
      isolatedNodes: [],
      duplicateRelations: [],
      invalidRelations: []
    }
  } finally {
    anomalyLoading.value = false
  }
}

// 获取图谱分析数据
const fetchMetrics = async (): Promise<void> => {
  loading.value = true
  try {
    const response = await fetch(API_BASE_URL, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result: ApiResponse = await response.json()
    
    if (result.code === 'SUCCESS') {
      metricsData.value = result.data
    } else {
      // 如果返回缓存错误，使用默认数据
      if (result.code === 'CACHE_ERROR') {
        console.warn('图谱分析数据尚未生成:', result.message)
        metricsData.value = {
          isolatedRatio: 0,
          density: 0,
          connectivity: []
        }
      } else {
        throw new Error(result.message || '获取数据失败')
      }
    }
  } catch (error) {
    console.error('获取图谱分析数据失败:', error)
    // 使用默认数据
    metricsData.value = {
      isolatedRatio: 0,
      density: 0,
      connectivity: []
    }
  } finally {
    loading.value = false
  }
}

// 获取文件数量
const fetchFileCount = async (): Promise<void> => {
  fileLoading.value = true
  try {
    const response = await fetch(FILE_COUNT_API_URL, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result: FileCountResponse = await response.json()
    
    if (result.code === 'SUCCESS') {
      fileCount.value = result.data
    } else {
      throw new Error(result.message || '获取文件数量失败')
    }
  } catch (error) {
    console.error('获取文件数量失败:', error)
    fileCount.value = '0'
  } finally {
    fileLoading.value = false
  }
}

// 获取降维数据
const fetchDimReduceData = async (): Promise<void> => {
  dimReduceLoading.value = true
  try {
    console.log('正在获取降维数据...')
    const response = await fetch(DIM_REDUCE_API_URL, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result: DimReduceResponse = await response.json()
    console.log('降维数据获取成功:', result)
    
    if (result.code === 'SUCCESS') {
      dimReduceData.value = result.data
      console.log('降维数据:', result.data)
    } else {
      throw new Error(result.message || '获取降维数据失败')
    }
  } catch (error) {
    console.error('获取降维数据失败:', error)
    
    // 使用模拟数据作为备用
    dimReduceData.value = [
      { id: 1, name: '机器学习', x: 0.1, y: 0.2 },
      { id: 2, name: '深度学习', x: 0.3, y: 0.4 },
      { id: 3, name: '神经网络', x: 0.5, y: 0.6 },
      { id: 4, name: '自然语言处理', x: 0.7, y: 0.8 }
    ]
  } finally {
    dimReduceLoading.value = false
  }
}

// 降维数据重新生成
const replaceDimData = async (): Promise<boolean> => {
  dimReplaceLoading.value = true
  try {
    console.log('正在重新生成降维数据...')
    const response = await fetch(DIM_REPLACE_API_URL, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result: DimReplaceResponse = await response.json()
    console.log('降维数据重新生成完成:', result)
    
    if (result.code === 'SUCCESS') {
      console.log('降维数据重新生成成功:', result.data)
      return true
    } else {
      throw new Error(result.message || '重新生成降维数据失败')
    }
  } catch (error) {
    console.error('重新生成降维数据失败:', error)
    return false
  } finally {
    dimReplaceLoading.value = false
  }
}

// 处理降维数据重新生成的函数
const handleRestore = async (chart: echarts.ECharts) => {
  if (dimReplaceLoading.value) return
  
  chart.showLoading({
    color: '#1890ff',
    textColor: isDark.value ? '#ffffff' : '#333',
    maskColor: isDark.value ? 'rgba(38, 38, 38, 0.8)' : 'rgba(255, 255, 255, 0.8)'
  })
  
  const success = await replaceDimData()
  
  if (success) {
    await fetchDimReduceData()
    setTimeout(() => {
      initQueryChart()
    }, 100)
  } else {
    chart.hideLoading()
  }
}

// 处理文件统计数据，转换为图表需要的格式
const processFileStatsData = () => {
  // 生成最近7天的日期数组
  const generateLast7Days = () => {
    const days = []
    const today = new Date()
    
    for (let i = 6; i >= 0; i--) {
      const date = new Date(today)
      date.setDate(date.getDate() - i)
      // 格式化为中文简短日期
      const formatted = date.toLocaleDateString('zh-CN', { 
        month: 'short', 
        day: 'numeric' 
      })
      days.push({
        formatted: formatted,
        fullDate: date.toISOString().split('T')[0], // YYYY-MM-DD 格式用于匹配
        dateObj: new Date(date.getFullYear(), date.getMonth(), date.getDate()) // 用于日期比较
      })
    }
    return days
  }

  const last7Days = generateLast7Days()
  const categories = last7Days.map(day => day.formatted)

  // 如果没有数据，返回空数据的7天
  if (!fileStatsData.value || fileStatsData.value.length === 0) {
    return {
      categories: categories,
      series: []
    }
  }

  // 获取所有文件类型
  const fileTypes = [...new Set(fileStatsData.value.map(item => item.type))]
  
  // 类型映射
  const typeMapping: Record<string, string> = {
    'MARKDOWN': 'MD',
    'CSV': 'CSV',
    'JSON': 'JSON',
    'XML': 'XML',
    'PDF': 'PDF',
    'WORD': 'Word',
    'TXT': 'TXT',
    'DOCX': 'Word'
  }

  // 构建系列数据
  const series = fileTypes.map(type => {
    const displayName = typeMapping[type] || type
    
    // 为每个文件类型生成7天的数据
    const data = last7Days.map(dayInfo => {
      // 查找对应日期和类型的数据
      const item = fileStatsData.value.find(stat => {
        try {
          // 处理新的日期格式 "Sep 23, 2025, 12:00:00 AM"
          const statDate = new Date(stat.day)
          const statDateOnly = new Date(statDate.getFullYear(), statDate.getMonth(), statDate.getDate())
          
          // 比较日期是否相同（只比较年月日，忽略时分秒）
          return stat.type === type && 
                 statDateOnly.getTime() === dayInfo.dateObj.getTime()
        } catch (error) {
          console.error('日期解析错误:', stat.day, error)
          return false
        }
      })
      
      // 如果找到数据就返回count，否则返回0
      // 注意 count 现在可能是浮点数，需要取整
      return item ? Math.floor(item.count) : 0
    })

    return {
      name: displayName,
      type: 'line',
      smooth: true,
      data: data
    }
  })

  // 如果没有任何文件类型，返回一个默认的全0系列
  if (series.length === 0) {
    return {
      categories: categories,
      series: [{
        name: '暂无数据',
        type: 'line',
        smooth: true,
        data: new Array(7).fill(0)
      }]
    }
  }

  return {
    categories: categories,
    series: series
  }
}

// 初始化文件量趋势图表
const initFileChart = () => {
  if (!fileChartRef.value) {
    console.log('fileChartRef 不存在，跳过初始化')
    return
  }
  
  console.log('开始初始化文件统计图表...')
  
  // 销毁旧实例
  const existingChart = echarts.getInstanceByDom(fileChartRef.value)
  if (existingChart) {
    console.log('销毁现有图表实例')
    existingChart.dispose()
  }
  
  const myChart = echarts.init(fileChartRef.value, isDark.value ? 'dark' : 'light')
  console.log('创建新的图表实例:', myChart.id)
  
  // 显示加载状态
  if (fileStatsLoading.value) {
    myChart.showLoading({
      color: '#1890ff',
      textColor: isDark.value ? '#ffffff' : '#333',
      maskColor: isDark.value ? 'rgba(38, 38, 38, 0.8)' : 'rgba(255, 255, 255, 0.8)'
    })
  }
  
  // 处理数据
  const chartData = processFileStatsData()
  console.log('图表数据处理完成:', chartData)
  
  if (!fileStatsLoading.value) {
    myChart.hideLoading()
  }
  
  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: isDark.value ? '#434343' : '#ffffff',
      borderColor: isDark.value ? '#434343' : '#d9d9d9',
      textStyle: {
        color: isDark.value ? '#ffffff' : '#333'
      },
      formatter: (params: any) => {
        let result = `${params[0].axisValue}<br/>`
        params.forEach((param: any) => {
          const marker = `<span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:${param.color};"></span>`
          result += `${marker}${param.seriesName}: ${param.value} 个文件<br/>`
        })
        return result
      }
    },
    legend: {
      data: chartData.series.map(s => s.name),
      textStyle: {
        color: isDark.value ? '#ffffff' : '#333'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '5%',
      containLabel: true
    },
    toolbox: {
      feature: {
        restore: {
          title: '刷新数据',
          onclick: function() {
            console.log('=== 文件统计图表刷新按钮被点击 ===')
            
            // 显示加载状态
            this.showLoading && this.showLoading({
              color: '#1890ff',
              textColor: isDark.value ? '#ffffff' : '#333',
              maskColor: isDark.value ? 'rgba(38, 38, 38, 0.8)' : 'rgba(255, 255, 255, 0.8)'
            })
            
            // 重新获取数据
            fetchFileStats()
              .then(() => {
                console.log('文件统计数据重新获取完成')
                setTimeout(() => {
                  console.log('重新初始化文件图表')
                  initFileChart()
                }, 100)
              })
              .catch(error => {
                console.error('刷新数据失败:', error)
                this.hideLoading && this.hideLoading()
              })
          }
        },
        saveAsImage: {
          title: '保存为图片'
        }
      },
      iconStyle: {
        borderColor: isDark.value ? '#ffffff' : '#333'
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: chartData.categories,
      axisLine: {
        lineStyle: {
          color: isDark.value ? '#434343' : '#d9d9d9'
        }
      },
      axisLabel: {
        color: isDark.value ? '#b3b3b3' : '#666'
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      minInterval: 1,
      axisLine: {
        lineStyle: {
          color: isDark.value ? '#434343' : '#d9d9d9'
        }
      },
      axisLabel: {
        color: isDark.value ? '#b3b3b3' : '#666',
        formatter: (value: number) => {
          return Math.floor(value).toString()
        }
      },
      splitLine: {
        lineStyle: {
          color: isDark.value ? '#434343' : '#f0f0f0'
        }
      }
    },
    series: chartData.series
  }
  
  myChart.setOption(option)
  console.log('图表配置设置完成')
  
  // 添加窗口大小变化监听
  const handleResize = () => {
    if (myChart && !myChart.isDisposed()) {
      myChart.resize()
    }
  }
  
  window.addEventListener('resize', handleResize)
  
  // 清理函数
  const cleanup = () => {
    console.log('清理图表资源')
    window.removeEventListener('resize', handleResize)
    if (myChart && !myChart.isDisposed()) {
      myChart.dispose()
    }
  }
  
  // 将清理函数存储到图表实例上
  ;(myChart as any).cleanup = cleanup
  
  console.log('文件统计图表初始化完成')
}

// 初始化查询分布图表
const initQueryChart = () => {
  if (!queryChartRef.value) return
  
  // 销毁旧实例
  const existingChart = echarts.getInstanceByDom(queryChartRef.value)
  if (existingChart) {
    existingChart.dispose()
  }
  
  const myChart = echarts.init(queryChartRef.value, isDark.value ? 'dark' : 'light')
  
  // 显示加载状态
  myChart.showLoading({
    color: '#1890ff',
    textColor: isDark.value ? '#ffffff' : '#333',
    maskColor: isDark.value ? 'rgba(38, 38, 38, 0.8)' : 'rgba(255, 255, 255, 0.8)'
  })
  
  // 等待降维数据加载完成或使用现有数据
  const waitForData = () => {
    if (dimReduceLoading.value || dimReplaceLoading.value) {
      setTimeout(waitForData, 500)
      return
    }
    
    myChart.hideLoading()
    
    // 将降维数据转换为ECharts需要的格式
    const chartData = dimReduceData.value.map(item => ({
      name: item.name,
      value: [item.x, item.y],
      keyword: item.name,
      id: item.id
    }))
    
    // 计算数据范围用于自适应坐标轴
    const xValues = chartData.map(item => item.value[0])
    const yValues = chartData.map(item => item.value[1])
    const xMin = Math.min(...xValues)
    const xMax = Math.max(...xValues)
    const yMin = Math.min(...yValues)
    const yMax = Math.max(...yValues)
    
    // 添加一些边距
    const xPadding = (xMax - xMin) * 0.1
    const yPadding = (yMax - yMin) * 0.1
    
    const option: EChartsOption = {
      title: {
        subtext: `共 ${chartData.length} 个关键词`,
        textStyle: {
          color: isDark.value ? '#ffffff' : '#333',
          fontSize: 16
        },
        subtextStyle: {
          color: isDark.value ? '#b3b3b3' : '#666',
          fontSize: 12
        },
        top: 10,
        left: 10
      },
      tooltip: {
        formatter: (params: any) => {
          return `关键词: ${params.data.keyword}<br/>坐标: (${params.data.value[0].toFixed(3)}, ${params.data.value[1].toFixed(3)})`
        },
        backgroundColor: isDark.value ? '#434343' : '#ffffff',
        borderColor: isDark.value ? '#434343' : '#d9d9d9',
        textStyle: {
          color: isDark.value ? '#ffffff' : '#333'
        }
      },
      toolbox: {
        feature: {
          restore: {
            title: '重新生成',
            onclick: () => handleRestore(myChart)
          },
          saveAsImage: {
            title: '保存为图片'
          }
        },
        iconStyle: {
          borderColor: isDark.value ? '#ffffff' : '#333'
        }
      },
      xAxis: {
        type: 'value',
        min: xMin - xPadding,
        max: xMax + xPadding,
        axisLine: {
          lineStyle: {
            color: isDark.value ? '#434343' : '#d9d9d9'
          }
        },
        axisLabel: {
          color: isDark.value ? '#b3b3b3' : '#666'
        },
        splitLine: {
          lineStyle: {
            color: isDark.value ? '#434343' : '#f0f0f0'
          }
        }
      },
      yAxis: {
        type: 'value',
        min: yMin - yPadding,
        max: yMax + yPadding,
        axisLine: {
          lineStyle: {
            color: isDark.value ? '#434343' : '#d9d9d9'
          }
        },
        axisLabel: {
          color: isDark.value ? '#b3b3b3' : '#666'
        },
        splitLine: {
          lineStyle: {
            color: isDark.value ? '#434343' : '#f0f0f0'
          }
        }
      },
      series: [
        {
          name: '关键词分布',
          type: 'scatter',
          data: chartData,
          symbolSize: (data: any) => {
            const length = data.keyword?.length || 3
            return Math.min(Math.max(length / 2 + 4, 4), 12)
          },
          itemStyle: {
            color: '#1890ff',
            opacity: 0.7
          },
          emphasis: {
            itemStyle: {
              color: '#ff4d4f',
              opacity: 1
            },
            label: {
              show: true,
              formatter: '{b}',
              position: 'top',
              // 修复：直接在 label 中配置文本样式，不使用 textStyle
              color: isDark.value ? '#ffffff' : '#333',
              fontSize: 12
            }
          }
        }
      ]
    }
    
    myChart.setOption(option)
    
    // 添加点击事件
    myChart.off('click')
    myChart.on('click', (params: any) => {
      console.log('点击了关键词:', params.data.keyword, '坐标:', params.data.value)
    })
  }
  
  waitForData()
}

// 格式化函数
const formatDensity = (density: number): string => {
  return (density * 100).toFixed(2) + '%'
}

const formatIsolatedRatio = (ratio: number): string => {
  return (ratio * 100).toFixed(2) + '%'
}

const getConnectivityCount = (connectivity: ConnectivityComponent[]): string => {
  return connectivity.length.toString()
}

// 异常检测相关函数
const getTotalAnomaliesText = (): string => {
  const total = getTotalAnomalies()
  return total > 0 ? `${total}个异常` : '正常'
}

const getTotalAnomalies = (): number => {
  const data = anomalyData.value
  return data.selfLoops.length + data.isolatedNodes.length + 
         data.duplicateRelations.length + data.invalidRelations.length
}

const getAnomalyColor = (): string => {
  const total = getTotalAnomalies()
  if (total === 0) {
    return '#52c41a' // 绿色表示正常
  } else if (total < 5) {
    return '#faad14' // 黄色表示轻微异常
  } else {
    return '#ff4d4f' // 红色表示严重异常
  }
}

// 计算属性：获取各种样式
const getStatItemStyle = () => ({
  backgroundColor: isDark.value ? '#2a2a2a' : '#ffffff',
  borderColor: isDark.value ? '#404040' : '#e2e8f0',
  border: `1px solid ${isDark.value ? '#404040' : '#e2e8f0'}`
})

const getIconStyle = (type: string) => {
  const baseStyle = {
    borderRadius: '50%',
    width: '48px',
    height: '48px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center'
  }

  if (isDark.value) {
    const darkColors: Record<string, string> = {
      density: '#1a3d1a',
      connectivity: '#1a2d4d',
      anomaly: '#4d1a1a',
      isolated: '#4d2d1a',
      file: '#2d4d1a'
    }
    return { ...baseStyle, backgroundColor: darkColors[type] || '#2a2a2a' }
  } else {
    const lightColors: Record<string, string> = {
      density: '#e8f5e8',
      connectivity: '#e6f7ff',
      anomaly: '#fff1f0',
      isolated: '#fff2e8',
      file: '#f6ffed'
    }
    return { ...baseStyle, backgroundColor: lightColors[type] || '#f5f5f5' }
  }
}

const getChartSectionStyle = () => ({
  backgroundColor: isDark.value ? '#2a2a2a' : '#ffffff',
  borderColor: isDark.value ? '#404040' : '#e2e8f0',
  padding: '20px',
  borderRadius: '8px',
  border: `1px solid ${isDark.value ? '#404040' : '#e2e8f0'}`
})

const getChartTitleStyle = () => ({
  color: isDark.value ? '#ffffff' : '#333333',
  fontSize: '16px',
  fontWeight: '600',
  marginBottom: '16px'
})

const getActivitySectionStyle = () => ({
  backgroundColor: isDark.value ? '#2a2a2a' : '#ffffff',
  borderColor: isDark.value ? '#404040' : '#e2e8f0',
  padding: '20px',
  borderRadius: '8px',
  border: `1px solid ${isDark.value ? '#404040' : '#e2e8f0'}`
})

const getTableHeaderStyle = () => ({
  borderBottom: `1px solid ${isDark.value ? '#404040' : '#e2e8f0'}`,
  color: isDark.value ? '#ffffff' : '#333333'
})

// 监听主题变化
watch(isDarkMode, () => {
  console.log('主题变化，重新初始化图表')
  reinitCharts()
})

// 监听文件统计数据变化，重新初始化文件图表
watch(fileStatsData, () => {
  console.log('文件统计数据已更新，重新初始化文件图表')
  // 增加延迟，确保数据更新完成
  setTimeout(() => {
    if (fileChartRef.value) {
      initFileChart()
    }
  }, 200)
}, { deep: true })

// 优化重新初始化图表的逻辑
const reinitCharts = () => {
  console.log('重新初始化所有图表')
  setTimeout(() => {
    if (fileChartRef.value) {
      initFileChart()
    }
    if (queryChartRef.value) {
      initQueryChart()
    }
  }, 200)
}

// 组件挂载时
onMounted(async () => {
  console.log('Dashboard组件已挂载，开始初始化...')
  
  // 先初始化图表
  setTimeout(() => {
    initFileChart()
    initQueryChart()
  }, 100)
  
  // Dashboard打开时自动获取所有数据
  console.log('Dashboard打开，开始获取最新数据...')
  try {
    await Promise.all([
      fetchMetrics(),
      fetchFileCount(),
      fetchFileStats(), // 新增：获取文件统计数据
      fetchDimReduceData(),
      fetchAnomalies()
    ])
    console.log('Dashboard数据获取完成')
  } catch (error) {
    console.error('Dashboard数据获取失败:', error)
  }
})

// 组件卸载时清理
onUnmounted(() => {
  console.log('Dashboard组件正在卸载，清理资源...')
  
  // 销毁图表实例
  if (fileChartRef.value) {
    const fileChart = echarts.getInstanceByDom(fileChartRef.value)
    if (fileChart && (fileChart as any).cleanup) {
      ;(fileChart as any).cleanup()
    } else if (fileChart) {
      fileChart.dispose()
    }
  }
  
  if (queryChartRef.value) {
    const queryChart = echarts.getInstanceByDom(queryChartRef.value)
    if (queryChart) {
      queryChart.dispose()
    }
  }
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  padding: 20px;
  transition: background-color 0.3s ease;
}

/* 统计卡片容器 - 调整为5列布局 */
.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

/* 统计卡片样式 */
.stat-item {
  padding: 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.stat-icon {
  font-size: 24px;
  margin-right: 16px;
  transition: background-color 0.3s ease;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  margin-bottom: 4px;
  transition: color 0.3s ease;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  transition: color 0.3s ease;
}

/* 图表区域 */
.charts-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.chart-section {
  transition: all 0.3s ease;
}

/* ECharts 容器样式 */
.chart-container {
  width: 100%;
  height: 350px;
  transition: background-color 0.3s ease;
}

/* 活动区域 */
.activity-section {
  transition: all 0.3s ease;
}

.activity-table {
  width: 100%;
}

.table-header {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
  padding: 12px 0;
  font-weight: bold;
  transition: all 0.3s ease;
}

/* 移除所有 Ant Design 组件的默认样式 */
:deep(.ant-card) {
  border: none !important;
  box-shadow: none !important;
}

:deep(.ant-card .ant-card-body) {
  padding: 0 !important;
}
</style>