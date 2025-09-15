<template>
  <div class="graph-visualization">
    <!-- 工具栏 -->
    <div class="graph-toolbar">
      <div class="left-tools">
        <a-input
          v-model:value="searchText"
          placeholder="搜索节点..."
          style="width: 200px;"
          @pressEnter="searchNodes"
        >
          <template #prefix>
            <search-outlined />
          </template>
        </a-input>
        <a-button-group>
          <a-button @click="fitView">
            <fullscreen-outlined />
          </a-button>
          <a-button @click="zoomIn">
            <plus-outlined />
          </a-button>
          <a-button @click="zoomOut">
            <minus-outlined />
          </a-button>
        </a-button-group>
      </div>
      <div class="right-tools">
        <a-button type="primary" @click="queryGraph">
          Query graph
        </a-button>
      </div>
    </div>
    
    <!-- 图谱画布 -->
    <div class="graph-canvas" ref="canvasRef">
      <!-- Cytoscape 将在这里渲染 -->
    </div>
    
    <!-- 节点详情面板 -->
    <div v-if="selectedNode" class="node-detail-panel">
      <div class="panel-header">
        <h4>{{ selectedNode.label }}</h4>
        <a-button size="small" @click="closePanel">
          <close-outlined />
        </a-button>
      </div>
      <div class="panel-content">
        <a-descriptions size="small" :column="1">
          <a-descriptions-item label="类型">
            {{ selectedNode.type }}
          </a-descriptions-item>
          <a-descriptions-item label="ID">
            {{ selectedNode.id }}
          </a-descriptions-item>
          <a-descriptions-item label="连接数">
            {{ selectedNode.connections || 0 }}
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import cytoscape from 'cytoscape'
import {
  SearchOutlined,
  FullscreenOutlined,
  PlusOutlined,
  MinusOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'

const props = defineProps({
  nodes: {
    type: Array,
    default: () => []
  },
  edges: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['nodeSelect', 'queryGraph'])

const canvasRef = ref(null)
const searchText = ref('')
const selectedNode = ref(null)
let cy = null

onMounted(() => {
  initGraph()
})

onUnmounted(() => {
  if (cy) {
    cy.destroy()
  }
})

const initGraph = () => {
  if (!canvasRef.value) return

  cy = cytoscape({
    container: canvasRef.value,
    elements: [
      ...props.nodes.map(node => ({
        data: { 
          id: node.id, 
          label: node.label, 
          type: node.type,
          ...node 
        }
      })),
      ...props.edges.map(edge => ({
        data: { 
          id: edge.id, 
          source: edge.source, 
          target: edge.target,
          label: edge.relation,
          ...edge 
        }
      }))
    ],
    style: [
      {
        selector: 'node',
        style: {
          'background-color': (ele) => getNodeColor(ele.data('type')),
          'label': 'data(label)',
          'width': 20,
          'height': 20,
          'font-size': 10,
          'text-valign': 'center',
          'text-halign': 'center',
          'text-outline-width': 1,
          'text-outline-color': '#fff',
          'overlay-padding': 4
        }
      },
      {
        selector: 'edge',
        style: {
          'width': 1,
          'line-color': '#ccc',
          'target-arrow-color': '#ccc',
          'target-arrow-shape': 'triangle',
          'curve-style': 'bezier',
          'label': 'data(label)',
          'font-size': 8,
          'text-rotation': 'autorotate'
        }
      },
      {
        selector: 'node:selected',
        style: {
          'border-width': 2,
          'border-color': '#1890ff',
          'background-color': '#e6f7ff'
        }
      },
      {
        selector: 'edge:selected',
        style: {
          'line-color': '#1890ff',
          'target-arrow-color': '#1890ff',
          'width': 2
        }
      }
    ],
    layout: {
      name: 'cose',
      animate: true,
      animationDuration: 1000,
      nodeRepulsion: 400000,
      nodeOverlap: 10,
      idealEdgeLength: 10,
      edgeElasticity: 100,
      nestingFactor: 5,
      gravity: 80,
      numIter: 1000,
      initialTemp: 200,
      coolingFactor: 0.95,
      minTemp: 1.0
    },
    minZoom: 0.1,
    maxZoom: 3
  })

  // 事件监听
  cy.on('tap', 'node', (evt) => {
    const node = evt.target.data()
    selectedNode.value = node
    emit('nodeSelect', node)
  })

  cy.on('tap', (evt) => {
    if (evt.target === cy) {
      selectedNode.value = null
    }
  })
}

const getNodeColor = (type) => {
  const colorMap = {
    'BusinessSegments': '#52c41a',
    'CorporateStructure': '#1890ff',
    'FinancialPerformance': '#722ed1',
    'FutureOutlook': '#fa8c16',
    'OperationalInfrastructure': '#f5222d',
    'ProductsAndServices': '#13c2c2',
    'RevenueStreams': '#eb2f96',
    'RiskFactors': '#faad14'
  }
  return colorMap[type] || '#666'
}

const searchNodes = () => {
  if (!cy || !searchText.value) return
  
  cy.elements().removeClass('highlighted')
  cy.elements().addClass('dimmed')
  
  const searchResults = cy.nodes().filter(node => 
    node.data('label').toLowerCase().includes(searchText.value.toLowerCase())
  )
  
  searchResults.removeClass('dimmed').addClass('highlighted')
  
  if (searchResults.length > 0) {
    cy.center(searchResults)
  }
}

const fitView = () => {
  if (cy) cy.fit()
}

const zoomIn = () => {
  if (cy) cy.zoom(cy.zoom() * 1.2)
}

const zoomOut = () => {
  if (cy) cy.zoom(cy.zoom() / 1.2)
}

const closePanel = () => {
  selectedNode.value = null
}

const queryGraph = () => {
  emit('queryGraph')
}
</script>

<style scoped>
.graph-visualization {
  position: relative;
  height: 100%;
  background: #f9f9f9;
}

.graph-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.left-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.graph-canvas {
  height: calc(100% - 64px);
  width: 100%;
}

.node-detail-panel {
  position: absolute;
  top: 80px;
  right: 16px;
  width: 300px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
}

.panel-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}

.panel-content {
  padding: 16px;
}

:deep(.dimmed) {
  opacity: 0.3;
}

:deep(.highlighted) {
  opacity: 1;
}
</style>