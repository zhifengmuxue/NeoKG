export interface Node {
  id: string
  label: string
  type: string
  connections?: number
  properties?: Record<string, any>
}

export interface Edge {
  id: string
  source: string
  target: string
  relation: string
  properties?: Record<string, any>
}

export interface Triple extends Edge {}

export interface Chunk {
  id: string
  type: string
  source: string
  content: string
  created: string
  properties?: Record<string, any>
}

export interface FilterType {
  name: string
  color?: string
  count?: number
  checked: boolean
}

export interface GraphFilters {
  entityTypes: FilterType[]
  relations: FilterType[]
  options: {
    hideUnselected: boolean
    autoFitView: boolean
    lockSelection: boolean
  }
}