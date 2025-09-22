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
      
      <!-- 新增：异常检测卡片 -->
      <div class="stat-item" :style="getStatItemStyle()">
        <div class="stat-icon anomaly-icon">⚠️</div>
        <div class="stat-content">
          <div class="stat-label" :style="{ color: isDark ? '#b3b3b3' : '#666' }">异常检测</div>
          <div class="stat-value" :style="{ color: anomalyLoading ? (isDark ? '#ffffff' : '#333') : getAnomalyColor() }">
            {{ anomalyLoading ? '检测中...' : getTotalAnomaliesText() }}
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
        <h3 :style="getChartTitleStyle()">关键词降维分布图</h3>
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
const fileLoading = ref(false)
const fileCount = ref<string>('0')

// 新增：降维数据加载状态
const dimReduceLoading = ref(false)
// 新增：降维重新生成状态
const dimReplaceLoading = ref(false)
// 新增：异常检测加载状态
const anomalyLoading = ref(false)

// 修改：降维数据类型定义 - 适配实际API响应
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

// 新增：降维替换接口响应类型
interface DimReplaceResponse {
  code: string
  message: string | null
  data: string
  timestamp: number
}

// 新增：异常检测数据类型
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

// 新增：降维数据状态
const dimReduceData = ref<Vec2D[]>([])
// 新增：异常检测数据状态
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
// 新增：降维API配置
const DIM_REDUCE_API_URL = import.meta.env.DEV ? '/api/dim/all' : 'http://localhost:8080/api/dim/all'
const DIM_REPLACE_API_URL = import.meta.env.DEV ? '/api/dim/replaceAll' : 'http://localhost:8080/api/dim/replaceAll'
// 新增：异常检测API配置
const ANOMALY_API_URL = import.meta.env.DEV ? '/api/graph/analysis/anomalies' : 'http://localhost:8080/api/graph/analysis/anomalies'

// 新增：获取异常检测数据
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
      throw new Error(result.message || '获取异常检测数据失败')
    }
  } catch (error) {
    console.error('获取异常检测数据失败:', error)
    
    // 使用模拟数据作为备用
    anomalyData.value = {
      selfLoops: [1, 2], // 模拟2个自环
      isolatedNodes: [],
      duplicateRelations: [1],
      invalidRelations: []
    }
  } finally {
    anomalyLoading.value = false
  }
}

// 新增：计算异常总数
const getTotalAnomalies = (): number => {
  const { selfLoops, isolatedNodes, duplicateRelations, invalidRelations } = anomalyData.value
  return (selfLoops?.length || 0) + 
         (isolatedNodes?.length || 0) + 
         (duplicateRelations?.length || 0) + 
         (invalidRelations?.length || 0)
}

// 新增：获取异常总数文本
const getTotalAnomaliesText = (): string => {
  const total = getTotalAnomalies()
  return total === 0 ? '正常' : `${total}个异常`
}

// 新增：获取异常状态颜色
const getAnomalyColor = (): string => {
  return isDark.value ? '#ffffff' : '#333'
}

// 修改：降维数据替换接口
const replaceDimData = async (): Promise<boolean> => {
  dimReplaceLoading.value = true
  try {
    console.log('正在重新生成降维数据...')
    const response = await fetch(DIM_REPLACE_API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result: DimReplaceResponse = await response.json()
    console.log('降维数据重新生成成功:', result)
    
    if (result.code === 'SUCCESS') {
      console.log('降维替换结果:', result.data)
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

// 修改：获取降维数据
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
      console.log('降维数据点数量:', result.data.length)
    } else {
      throw new Error(result.message || '获取降维数据失败')
    }
  } catch (error) {
    console.error('获取降维数据失败:', error)
    
    // 使用模拟数据作为备用 - 更新数据结构
    const generateMockDimData = () => {
      const data: Vec2D[] = []
      const keywords = ['AI', '机器学习', '深度学习', '神经网络', '数据挖掘', '知识图谱', '自然语言处理', '计算机视觉', '推荐系统', '模式识别']
      
      for (let i = 0; i < 100; i++) {
        data.push({
          id: 1000000000000000000 + i,
          name: keywords[i % keywords.length] + '_' + i,
          x: Math.random() * 2 - 1, // 范围 -1 到 1，更接近真实数据
          y: Math.random() * 2 - 1
        })
      }
      return data
    }
    
    dimReduceData.value = generateMockDimData()
  } finally {
    dimReduceLoading.value = false
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
      throw new Error(result.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取图谱分析数据失败:', error)
    // 使用模拟数据
    metricsData.value = {
      isolatedRatio: 0.15,
      density: 0.35,
      connectivity: [
        { componentId: 1, size: 150 },
        { componentId: 2, size: 80 },
        { componentId: 3, size: 45 }
      ]
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
    fileCount.value = '1,234'
  } finally {
    fileLoading.value = false
  }
}

// 格式化函数
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

// 新增：处理还原操作
const handleRestore = async (chartInstance: echarts.ECharts) => {
  try {
    // 显示加载状态
    chartInstance.showLoading({
      color: '#1890ff',
      text: '重新生成降维数据中...',
      textColor: isDark.value ? '#ffffff' : '#333',
      maskColor: isDark.value ? 'rgba(38, 38, 38, 0.8)' : 'rgba(255, 255, 255, 0.8)'
    })
    
    // 1. 先调用替换接口重新生成降维数据
    const replaceSuccess = await replaceDimData()
    
    if (replaceSuccess) {
      // 2. 如果重新生成成功，获取新的降维数据
      await fetchDimReduceData()
      
      // 3. 数据更新后，图表会通过 watch 自动重新渲染
      console.log('降维数据已更新，图表将自动刷新')
    } else {
      // 如果重新生成失败，至少还原图表视图
      chartInstance.dispatchAction({
        type: 'restore'
      })
    }
    
    chartInstance.hideLoading()
  } catch (error) {
    console.error('还原操作失败:', error)
    chartInstance.hideLoading()
    
    // 出错时至少还原图表视图
    chartInstance.dispatchAction({
      type: 'restore'
    })
  }
}

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
  
  // 显示加载状态
  myChart.showLoading({
    color: '#1890ff',
    textColor: isDark.value ? '#ffffff' : '#333',
    maskColor: isDark.value ? 'rgba(38, 38, 38, 0.8)' : 'rgba(255, 255, 255, 0.8)'
  })
  
  // 等待降维数据加载完成或使用现有数据
  const waitForData = () => {
    if (dimReduceLoading.value || dimReplaceLoading.value) {
      // 如果数据还在加载，等待一段时间后重试
      setTimeout(waitForData, 500)
      return
    }
    
    myChart.hideLoading()
    
    // 将降维数据转换为ECharts需要的格式 - 适配新的数据结构
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
        trigger: 'item',
        formatter: (params: any) => {
          const data = params.data
          return `
            <div style="padding: 8px;">
              <div style="font-weight: bold; margin-bottom: 4px;">关键词: ${data.keyword}</div>
              <div>ID: ${data.id}</div>
              <div>坐标: (${data.value[0].toFixed(4)}, ${data.value[1].toFixed(4)})</div>
            </div>
          `
        },
        backgroundColor: isDark.value ? '#434343' : '#ffffff',
        borderColor: isDark.value ? '#434343' : '#d9d9d9',
        textStyle: {
          color: isDark.value ? '#ffffff' : '#333'
        },
        extraCssText: 'box-shadow: 0 2px 8px rgba(0,0,0,0.15); border-radius: 4px;'
      },
      toolbox: {
        right: 20,
        feature: {
          myRestore: {
            show: true,
            title: '重新生成数据',
           icon: 'path://M4 12a8 8 0 0 1 8-8V2.5L16 6l-4 3.5V8a6 6 0 1 0 6 6h1.5a7.5 7.5 0 1 1-7.5-7.5z',
            onclick: () => {
              handleRestore(myChart)
            }
          },
          saveAsImage: {
            title: '保存为图片',
            pixelRatio: 2
          }
        },
        iconStyle: {
          borderColor: isDark.value ? '#ffffff' : '#333'
        }
      },
      grid: {
        left: '8%',
        right: '12%',
        bottom: '20%',
        top: '20%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        name: 'X坐标',
        nameLocation: 'middle',
        nameGap: 25,
        nameTextStyle: {
          color: isDark.value ? '#b3b3b3' : '#666',
          fontSize: 12
        },
        min: xMin - xPadding,
        max: xMax + xPadding,
        axisLine: {
          lineStyle: {
            color: isDark.value ? '#434343' : '#d9d9d9'
          }
        },
        axisLabel: {
          color: isDark.value ? '#b3b3b3' : '#666',
          formatter: (value: number) => value.toFixed(2)
        },
        splitLine: {
          lineStyle: {
            color: isDark.value ? '#434343' : '#f0f0f0',
            type: 'dashed'
          }
        }
      },
      yAxis: {
        type: 'value',
        name: 'Y坐标',
        nameLocation: 'middle',
        nameGap: 35,
        nameTextStyle: {
          color: isDark.value ? '#b3b3b3' : '#666',
          fontSize: 12
        },
        min: yMin - yPadding,
        max: yMax + yPadding,
        axisLine: {
          lineStyle: {
            color: isDark.value ? '#434343' : '#d9d9d9'
          }
        },
        axisLabel: {
          color: isDark.value ? '#b3b3b3' : '#666',
          formatter: (value: number) => value.toFixed(2)
        },
        splitLine: {
          lineStyle: {
            color: isDark.value ? '#434343' : '#f0f0f0',
            type: 'dashed'
          }
        }
      },
      dataZoom: [
        {
          type: 'inside',
          xAxisIndex: 0
        },
        {
          type: 'inside',
          yAxisIndex: 0
        },
        {
          type: 'slider',
          xAxisIndex: 0,
          bottom: 60,
          height: 20,
          textStyle: {
            color: isDark.value ? '#ffffff' : '#333'
          },
          backgroundColor: isDark.value ? '#434343' : '#f5f5f5',
          fillerColor: isDark.value ? '#666' : '#e6e6e6',
          borderColor: isDark.value ? '#666' : '#d9d9d9'
        },
        {
          type: 'slider',
          yAxisIndex: 0,
          right: 30,
          width: 20,
          textStyle: {
            color: isDark.value ? '#ffffff' : '#333'
          },
          backgroundColor: isDark.value ? '#434343' : '#f5f5f5',
          fillerColor: isDark.value ? '#666' : '#e6e6e6',
          borderColor: isDark.value ? '#666' : '#d9d9d9'
        }
      ],
      series: [
        {
          name: '关键词分布',
          type: 'scatter',
          data: chartData,
          symbolSize: (data: any) => {
            // 根据关键词长度调整点的大小，范围在4-12之间
            const length = data.keyword?.length || 3
            return Math.min(Math.max(length / 2 + 4, 4), 12)
          },
          itemStyle: {
            opacity: 0.8,
            color: (params: any) => {
              // 根据数据索引生成不同颜色
              const colors = [
                '#1890ff', '#52c41a', '#faad14', '#f5222d', 
                '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16',
                '#a0d911', '#096dd9', '#36cfc9', '#ff85c0'
              ]
              return colors[params.dataIndex % colors.length]
            },
            borderColor: isDark.value ? '#ffffff' : '#333',
            borderWidth: 0.5
          },
          emphasis: {
            scale: 1.8,
            itemStyle: {
              opacity: 1,
              borderWidth: 2,
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.3)'
            }
          },
          large: chartData.length > 1000,
          largeThreshold: 1000,
          animation: true,
          animationDuration: 1000,
          animationEasing: 'cubicOut'
        }
      ]
    }
    
    myChart.setOption(option)
    
    // 添加点击事件
    myChart.on('click', (params: any) => {
      console.log('点击了关键词:', params.data.keyword, '坐标:', params.data.value)
    })
  }
  
  // 开始等待数据
  waitForData()
  
  // 响应式调整
  const resizeHandler = () => myChart.resize()
  window.removeEventListener('resize', resizeHandler)
  window.addEventListener('resize', resizeHandler)
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

// 监听主题变化，重新初始化图表
const reinitCharts = () => {
  setTimeout(() => {
    initFileChart()
    initQueryChart()
  }, 100)
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

// 监听降维数据变化，重新初始化查询图表
watch(dimReduceData, () => {
  setTimeout(() => {
    initQueryChart()
  }, 100)
}, { deep: true })

// 使用 MutationObserver 监听主题变化
onMounted(() => {
  // 初始检测主题
  detectTheme()
  
  // 获取所有数据
  fetchMetrics()
  fetchFileCount()
  fetchDimReduceData()
  fetchAnomalies() // 新增：获取异常检测数据
  
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

/* 统计卡片容器 - 调整为5列布局 */
.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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

/* 新增：异常检测图标样式 */
.anomaly-icon {
  background-color: #fff1f0;
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