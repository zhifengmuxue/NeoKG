<template>
  <div class="dashboard" :style="{ backgroundColor: isDark ? '#1f1f1f' : '#ffffff' }">
    <!-- 统计卡片区域 -->
    <div class="stats-container">
      <div class="stat-item" :style="getStatItemStyle()">
        <div class="stat-icon density-icon">🕸️</div>
        <div class="stat-content">
          <div class="stat-label" :style="{ color: isDark ? '#b3b3b3' : '#666' }">图谱密度</div>
          <div class="stat-value" :style="{ color: isDark ? '#ffffff' : '#333' }">
            {{ loading ? '加载中...' : formatDensity(metricsData.density) }}
          </div>
        </div>
      </div>
      
      <div class="stat-item" :style="getStatItemStyle()">
        <div class="stat-icon connectivity-icon">🔗</div>
        <div class="stat-content">
          <div class="stat-label" :style="{ color: isDark ? '#b3b3b3' : '#666' }">连通组件</div>
          <div class="stat-value" :style="{ color: isDark ? '#ffffff' : '#333' }">
            {{ loading ? '加载中...' : getConnectivityCount(metricsData.connectivity) }}
          </div>
        </div>
      </div>
      
      <div class="stat-item" :style="getStatItemStyle()">
        <div class="stat-icon isolated-icon">⚪</div>
        <div class="stat-content">
          <div class="stat-label" :style="{ color: isDark ? '#b3b3b3' : '#666' }">孤立结点比例</div>
          <div class="stat-value" :style="{ color: isDark ? '#ffffff' : '#333' }">
            {{ loading ? '加载中...' : formatIsolatedRatio(metricsData.isolatedRatio) }}
          </div>
        </div>
      </div>
      
      <div class="stat-item" :style="getStatItemStyle()">
        <div class="stat-icon file-icon">📁</div>
        <div class="stat-content">
          <div class="stat-label" :style="{ color: isDark ? '#b3b3b3' : '#666' }">文件数量</div>
          <div class="stat-value" :style="{ color: isDark ? '#ffffff' : '#333' }">
            {{ fileLoading ? '加载中...' : fileCount }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 其他内容保持不变 -->
    <!-- 图表区域 -->
    <div class="charts-container">
      <div class="chart-section" :style="getChartSectionStyle()">
        <h3 :style="getChartTitleStyle()">文件量趋势</h3>
        <!-- ECharts 图表容器 -->
        <div ref="fileChartRef" class="chart-container"></div>
      </div>
      
      <div class="chart-section" :style="getChartSectionStyle()">
        <h3 :style="getChartTitleStyle()">查询分布</h3>
        <!-- ECharts 图表容器 -->
        <div ref="queryChartRef" class="chart-container"></div>
      </div>
    </div>
    
    <!-- 最近活动 -->
    <div class="activity-section" :style="getActivitySectionStyle()">
      <h3 :style="getChartTitleStyle()">最近活动</h3>
      <div class="activity-table">
        <div class="table-header" :style="getTableHeaderStyle()">
          <span>操作</span>
          <span>时间</span>
          <span>状态</span>
        </div>
        <!-- 表格内容 -->
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import * as echarts from 'echarts'

type EChartsOption = echarts.EChartsOption

// 图表实例引用
const fileChartRef = ref<HTMLDivElement>()
const queryChartRef = ref<HTMLDivElement>()

// 主题状态和数据状态
const isDark = ref(false)
const loading = ref(false)
const fileLoading = ref(false) // 新增：文件数量加载状态
const fileCount = ref<string>('0') // 新增：文件数量

// 新增：图谱分析数据类型
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

// 新增：文件数量API响应类型
interface FileCountResponse {
  code: string
  message: string | null
  data: string
  timestamp: number
}

// 新增：图谱分析数据状态
const metricsData = ref<MetricsData>({
  isolatedRatio: 0,
  density: 0,
  connectivity: []
})

// 新增：API配置
const API_BASE_URL = import.meta.env.DEV ? '/api/graph/analysis/metrics' : 'http://localhost:8080/api/graph/analysis/metrics'
const FILE_COUNT_API_URL = import.meta.env.DEV ? '/api/file/num' : 'http://localhost:8080/api/file/num'

// 新增：获取文件数量
const fetchFileCount = async (): Promise<void> => {
  fileLoading.value = true
  try {
    console.log('正在获取文件数量...')
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
    console.log('文件数量获取成功:', result)
    
    if (result.code === 'SUCCESS') {
      fileCount.value = result.data
    } else {
      throw new Error(result.message || '获取文件数量失败')
    }
  } catch (error) {
    console.error('获取文件数量失败:', error)
    // 使用默认值
    fileCount.value = '2,856'
  } finally {
    fileLoading.value = false
  }
}

// 新增：获取图谱分析数据
const fetchMetrics = async (): Promise<void> => {
  loading.value = true
  try {
    console.log('正在获取图谱分析数据...')
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
    console.log('图谱分析数据获取成功:', result)
    
    if (result.code === 'SUCCESS') {
      metricsData.value = result.data
    } else {
      throw new Error(result.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取图谱分析数据失败:', error)
    
    // 使用模拟数据
    metricsData.value = {
      isolatedRatio: 0.0,
      density: 0.08695652173913043,
      connectivity: [
        { componentId: 2, size: 21 },
        { componentId: 0, size: 2 }
      ]
    }
  } finally {
    loading.value = false
  }
}

// 新增：格式化函数
const formatDensity = (density: number): string => {
  return (density * 100).toFixed(1) + '%'
}

const getConnectivityCount = (connectivity: ConnectivityComponent[]): string => {
  if (!connectivity || connectivity.length === 0) return '0个'
  return `${connectivity.length}个`
}

const formatIsolatedRatio = (ratio: number): string => {
  return (ratio * 100).toFixed(1) + '%'
}

// 检测主题的函数
const detectTheme = () => {
  const bodyBg = getComputedStyle(document.body).backgroundColor
  const htmlBg = getComputedStyle(document.documentElement).backgroundColor
  
  // 检测是否为深色主题
  const isDarkTheme = bodyBg === 'rgb(20, 20, 20)' || 
                     htmlBg === 'rgb(20, 20, 20)' ||
                     bodyBg === '#141414' ||
                     htmlBg === '#141414'
  
  isDark.value = isDarkTheme
  console.log('Theme detected:', isDarkTheme, 'bodyBg:', bodyBg, 'htmlBg:', htmlBg) // 调试用
}

// 监听主题变化
watch(isDark, () => {
  console.log('Theme changed to:', isDark.value) // 调试用
  reinitCharts()
})

// 样式计算函数
const getStatItemStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff',
  borderBottom: `1px solid ${isDark.value ? '#434343' : '#f5f5f5'}`
})

const getChartSectionStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff',
  padding: '20px',
  border: 'none',
  boxShadow: 'none',
  borderRadius: 0
})

const getChartTitleStyle = () => ({
  margin: '0 0 16px 0',
  fontSize: '16px',
  color: isDark.value ? '#ffffff' : '#333',
  borderBottom: `1px solid ${isDark.value ? '#434343' : '#f5f5f5'}`,
  paddingBottom: '10px'
})

const getActivitySectionStyle = () => ({
  backgroundColor: isDark.value ? '#262626' : '#ffffff',
  padding: '20px',
  border: 'none',
  boxShadow: 'none',
  borderRadius: 0
})

const getTableHeaderStyle = () => ({
  borderBottom: `1px solid ${isDark.value ? '#434343' : '#f5f5f5'}`,
  color: isDark.value ? '#ffffff' : '#333'
})

// 初始化文件量趋势图表 - 添加主题支持
const initFileChart = () => {
  if (!fileChartRef.value) return
  
  // 销毁旧实例
  const existingChart = echarts.getInstanceByDom(fileChartRef.value)
  if (existingChart) {
    existingChart.dispose()
  }
  
  const myChart = echarts.init(fileChartRef.value, isDark.value ? 'dark' : 'light')
  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: isDark.value ? '#434343' : '#ffffff',
      borderColor: isDark.value ? '#434343' : '#d9d9d9',
      textStyle: {
        color: isDark.value ? '#ffffff' : '#333'
      }
    },
    legend: {
      data: ['MD', 'CSV', 'JSON', 'XML', 'PDF', 'Word'],
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
        saveAsImage: {}
      },
      iconStyle: {
        borderColor: isDark.value ? '#ffffff' : '#333'
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
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
        name: 'MD',
        type: 'line',
        smooth: true,
        data: [120, 132, 101, 134, 90, 230, 210]
      },
      {
        name: 'CSV',
        type: 'line',
        smooth: true,
        data: [220, 182, 191, 234, 290, 330, 310]
      },
      {
        name: 'JSON',
        type: 'line',
        smooth: true,
        data: [150, 232, 201, 154, 190, 330, 410]
      },
      {
        name: 'XML',
        type: 'line',
        smooth: true,
        data: [320, 332, 301, 334, 390, 330, 320]
      },
      {
        name: 'PDF',
        type: 'line',
        smooth: true,
        data: [820, 932, 901, 934, 1290, 1330, 1320]
      },
      {
        name: 'Word',
        type: 'line',
        smooth: true,
        data: [420, 532, 501, 534, 690, 730, 620]
      }
    ]
  }
  
  myChart.setOption(option)
  
  // 响应式调整
  const resizeHandler = () => myChart.resize()
  window.removeEventListener('resize', resizeHandler)
  window.addEventListener('resize', resizeHandler)
}

// 初始化查询分布图表 - 添加主题支持
const initQueryChart = () => {
  if (!queryChartRef.value) return
  
  // 销毁旧实例
  const existingChart = echarts.getInstanceByDom(queryChartRef.value)
  if (existingChart) {
    existingChart.dispose()
  }
  
  const myChart = echarts.init(queryChartRef.value, isDark.value ? 'dark' : 'light')
  
  // 模拟数据 - 替代远程数据加载
  const generateMockData = () => {
    const data = []
    const pointCount = 10000
    
    for (let i = 0; i < pointCount; i++) {
      const x = Math.random() * 100
      const y = Math.random() * 100
      data.push([x, y])
    }
    
    return data
  }
  
  myChart.showLoading({
    color: '#1890ff',
    textColor: isDark.value ? '#ffffff' : '#333',
    maskColor: isDark.value ? 'rgba(38, 38, 38, 0.8)' : 'rgba(255, 255, 255, 0.8)'
  })
  
  // 模拟异步加载
  setTimeout(() => {
    myChart.hideLoading()
    
    const mockData = generateMockData()
    
    const option: EChartsOption = {
      tooltip: {
        backgroundColor: isDark.value ? '#434343' : '#ffffff',
        borderColor: isDark.value ? '#434343' : '#d9d9d9',
        textStyle: {
          color: isDark.value ? '#ffffff' : '#333'
        }
      },
      toolbox: {
        right: 20,
        feature: {
          dataZoom: {}
        },
        iconStyle: {
          borderColor: isDark.value ? '#ffffff' : '#333'
        }
      },
      grid: {
        right: 70,
        bottom: 70
      },
      xAxis: [{
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
      }],
      yAxis: [{
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
      }],
      dataZoom: [
        {
          type: 'inside'
        },
        {
          type: 'slider',
          showDataShadow: false,
          handleIcon:
            'path://M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
          handleSize: '80%',
          textStyle: {
            color: isDark.value ? '#ffffff' : '#333'
          },
          backgroundColor: isDark.value ? '#434343' : '#f5f5f5',
          fillerColor: isDark.value ? '#666' : '#e6e6e6',
          borderColor: isDark.value ? '#666' : '#d9d9d9'
        },
        {
          type: 'inside',
          orient: 'vertical'
        },
        {
          type: 'slider',
          orient: 'vertical',
          showDataShadow: false,
          handleIcon:
            'path://M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
          handleSize: '80%',
          textStyle: {
            color: isDark.value ? '#ffffff' : '#333'
          },
          backgroundColor: isDark.value ? '#434343' : '#f5f5f5',
          fillerColor: isDark.value ? '#666' : '#e6e6e6',
          borderColor: isDark.value ? '#666' : '#d9d9d9'
        }
      ],
      animation: false,
      series: [
        {
          type: 'scatter',
          data: mockData,
          dimensions: ['x', 'y'],
          symbolSize: 3,
          itemStyle: {
            opacity: 0.4,
            color: isDark.value ? '#40a9ff' : '#1890ff'
          },
          blendMode: 'source-over',
          large: true,
          largeThreshold: 500
        }
      ]
    }
    
    myChart.setOption(option)
  }, 500)
  
  // 响应式调整
  const resizeHandler = () => myChart.resize()
  window.removeEventListener('resize', resizeHandler)
  window.addEventListener('resize', resizeHandler)
}

// 监听主题变化，重新初始化图表
const reinitCharts = () => {
  setTimeout(() => {
    initFileChart()
    initQueryChart()
  }, 100)
}

// 使用 MutationObserver 监听主题变化
onMounted(() => {
  // 初始检测主题
  detectTheme()
  
  // 获取图谱分析数据和文件数量
  fetchMetrics()
  fetchFileCount() // 新增：获取文件数量
  
  // 延迟初始化，确保DOM已渲染
  setTimeout(() => {
    initFileChart()
    initQueryChart()
  }, 100)
  
  // 监听 body 和 html 样式变化来检测主题切换
  const observer = new MutationObserver(() => {
    detectTheme()
  })
  
  observer.observe(document.body, {
    attributes: true,
    attributeFilter: ['style']
  })
  
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['style']
  })
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  padding: 20px;
  transition: background-color 0.3s ease;
}

/* 统计卡片容器 */
.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

/* 统计卡片样式 - 去除边框和阴影 */
.stat-item {
  padding: 20px;
  border: none;
  box-shadow: none;
  border-radius: 0;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.stat-icon {
  font-size: 24px;
  margin-right: 16px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.density-icon {
  background-color: #e8f5e8;
}

.connectivity-icon {
  background-color: #e6f7ff;
}

.isolated-icon {
  background-color: #fff2e8;
}

.file-icon {
  background-color: #f6ffed;
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