<template>
  <div class="query-page">
    <div class="main-content">
      <!-- Left Sidebar - Filters -->
      <div class="sidebar">
        <div class="filters-section">
          <h3>Filters</h3>
          <div class="filter-controls">
            <button class="btn-icon"><i class="icon-grid"></i></button>
            <button class="btn-icon"><i class="icon-plus"></i></button>
            <button class="btn-icon"><i class="icon-search"></i></button>
            <button class="btn-icon"><i class="icon-filter"></i></button>
            <button class="btn-icon"><i class="icon-settings"></i></button>
          </div>
        </div>

        <!-- Entity Types -->
        <div class="filter-group">
          <div class="filter-header">
            <i class="icon-entity"></i>
            <span>Entity types</span>
            <span class="count">6/8</span>
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
            <i class="icon-relation"></i>
            <span>Relations</span>
            <span class="count">5/5</span>
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
      <div class="graph-container">
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

<script lang="ts">
import { defineComponent } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

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

export default defineComponent({
  name: 'QueryPage',
  data() {
    return {
      hideUnselectedNodes: false,
      autoFitView: false,
      lockSelection: false,
      myChart: null as echarts.ECharts | null,
      chartLoaded: false,
      loadingMessage: '正在加载图表...',
      entityTypes: [
        { name: 'BusinessSegments', selected: true, color: '#4F46E5' },
        { name: 'CorporateStructure', selected: true, color: '#06B6D4' },
        { name: 'FinancialPerformance', selected: true, color: '#10B981' },
        { name: 'FutureOutlook', selected: true, color: '#F59E0B' },
        { name: 'OperationalInfrastructure', selected: false, color: '#EF4444' },
        { name: 'ProductsAndServices', selected: true, color: '#8B5CF6' },
        { name: 'RevenueStreams', selected: true, color: '#EC4899' },
        { name: 'RiskFactors', selected: true, color: '#6B7280' }
      ],
      relations: [
        { name: 'comprises', selected: true },
        { name: 'contributes_to', selected: true },
        { name: 'depends_on', selected: true },
        { name: 'drives', selected: true },
        { name: 'influences', selected: true }
      ]
    }
  },
  mounted() {
    console.log('组件已挂载，开始初始化图表')
    this.$nextTick(() => {
      this.initChart()
    })
    window.addEventListener('resize', this.handleResize)
  },
  beforeUnmount() {
    if (this.myChart) {
      this.myChart.dispose()
    }
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    async initChart() {
      try {
        console.log('开始初始化图表')
        const chartDom = this.$refs.graphCanvas as HTMLElement
        
        if (!chartDom) {
          console.error('图表容器未找到')
          this.loadingMessage = '图表容器未找到'
          return
        }

        console.log('图表容器尺寸:', chartDom.offsetWidth, 'x', chartDom.offsetHeight)
        
        this.myChart = echarts.init(chartDom)
        console.log('ECharts实例已创建')
        
        // 显示加载动画
        this.myChart.showLoading()
        this.loadingMessage = '正在加载数据...'

        const ROOT_PATH = 'https://echarts.apache.org/examples'
        
        try {
          console.log('开始请求数据')
          const response = await axios.get(`${ROOT_PATH}/data/asset/data/npmdepgraph.min10.json`)
          const json = response.data
          console.log('数据加载成功:', json)
          
          // 隐藏加载动画
          this.myChart.hideLoading()

          const option: EChartsOption = {
            title: {
              text: 'NPM Dependencies'
            },
            animationDurationUpdate: 1500,
            animationEasingUpdate: 'quinticInOut',
            series: [
              {
                type: 'graph',
                layout: 'none',
                // progressiveThreshold: 700,
                data: json.nodes.map(function (node: RawNode) {
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
                edges: json.edges.map(function (edge: RawEdge) {
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
                  opacity: 0.7
                }
              }
            ],
            thumbnail: {
              width: '20%',
              height: '20%',
              windowStyle: {
                color: 'rgba(140, 212, 250, 0.5)',
                borderColor: 'rgba(30, 64, 175, 0.7)',
                opacity: 1
              }
            }
          }

          console.log('设置图表配置')
          this.myChart.setOption(option, true)
          this.chartLoaded = true
          console.log('图表配置完成')
          
        } catch (networkError) {
          console.error('网络请求失败，使用备用数据:', networkError)
          this.loadFallbackData()
        }
        
      } catch (error) {
        console.error('初始化图表失败:', error)
        this.loadingMessage = '图表初始化失败'
      }
    },

    loadFallbackData() {
      console.log('加载备用数据')
      this.myChart?.hideLoading()
      
      // 创建简单的NPM依赖图测试数据
      const option: EChartsOption = {
        title: {
          text: 'NPM Dependencies (备用数据)'
        },
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
              opacity: 0.7
            }
          }
        ]
      }

      this.myChart?.setOption(option, true)
      this.chartLoaded = true
      console.log('备用数据加载完成')
    },

    handleResize() {
      if (this.myChart) {
        this.myChart.resize()
      }
    }
  }
})
</script>

<style scoped>
.query-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8fafc;
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
}

.filter-controls {
  display: flex;
  gap: 0.25rem;
}

.btn-icon {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: none;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  color: #64748b;
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
}

.count {
  margin-left: auto;
  color: #64748b;
  font-size: 0.875rem;
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

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.option-item label {
  margin-left: 0.5rem;
  cursor: pointer;
}

.graph-container {
  flex: 1;
  position: relative;
  min-height: 400px;
}

.graph-canvas {
  width: 100%;
  height: 100%;
  background: white;
  min-height: 400px;
}

.loading-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748b;
  font-size: 1rem;
}
</style>