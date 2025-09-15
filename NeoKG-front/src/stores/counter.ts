import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { Node, Edge, Chunk } from '@/types/graph'

export const useGraphStore = defineStore('graph', () => {
  // 状态
  const nodes = ref<Node[]>([])
  const edges = ref<Edge[]>([])
  const chunks = ref<Chunk[]>([])
  const selectedNodeId = ref<string | null>(null)
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)
  
  // 计算属性
  const selectedNode = computed((): Node | null => {
    if (!selectedNodeId.value) return null
    return nodes.value.find(node => node.id === selectedNodeId.value) || null
  })
  
  const entityTypes = computed((): string[] => {
    const types = new Set<string>()
    nodes.value.forEach(node => types.add(node.type))
    return Array.from(types)
  })
  
  const relations = computed((): string[] => {
    const rels = new Set<string>()
    edges.value.forEach(edge => rels.add(edge.relation))
    return Array.from(rels)
  })
  
  // 动作
  const selectNode = (nodeId: string): void => {
    selectedNodeId.value = nodeId
  }
  
  const clearSelection = (): void => {
    selectedNodeId.value = null
  }
  
  const loadGraphData = async (): Promise<void> => {
    loading.value = true
    error.value = null
    
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // 模拟数据
      nodes.value = [
        {
          id: '1',
          label: 'Business Operations',
          type: 'BusinessSegments',
          connections: 5
        },
        {
          id: '2',
          label: 'Technology Infrastructure',
          type: 'OperationalInfrastructure',
          connections: 8
        },
        {
          id: '3',
          label: 'Cloud Services',
          type: 'ProductsAndServices',
          connections: 12
        }
      ]
      
      edges.value = [
        {
          id: 'e1',
          source: '1',
          target: '2',
          relation: 'depends_on'
        },
        {
          id: 'e2',
          source: '3',
          target: '2',
          relation: 'comprises'
        }
      ]
      
      chunks.value = [
        {
          id: 'c1',
          type: 'Document',
          source: 'business_report.pdf',
          content: 'Business operations content...',
          created: '2024-01-15'
        }
      ]
      
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      throw err
    } finally {
      loading.value = false
    }
  }
  
  return {
    // 状态
    nodes,
    edges,
    chunks,
    selectedNodeId,
    loading,
    error,
    
    // 计算属性
    selectedNode,
    entityTypes,
    relations,
    
    // 动作
    selectNode,
    clearSelection,
    loadGraphData
  }
})