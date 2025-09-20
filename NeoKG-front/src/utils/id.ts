/**
 * 安全处理雪花算法ID，确保不丢失精度
 */
export const safeStringifyId = (id: any): string => {
  // 如果已经是字符串，直接返回
  if (typeof id === 'string') {
    return id
  }
  
  // 如果是数字且超过安全整数范围，使用BigInt处理
  if (typeof id === 'number') {
    if (id > Number.MAX_SAFE_INTEGER || id < Number.MIN_SAFE_INTEGER) {
      console.warn(`ID ${id} 超过JavaScript安全整数范围，可能存在精度丢失`)
      return BigInt(Math.floor(id)).toString()
    }
    return id.toString()
  }
  
  // 其他情况，强制转换为字符串
  return String(id)
}

/**
 * 从JSON中安全解析包含大整数的数据
 */
export const safeJsonParse = (jsonString: string) => {
  // 使用正则表达式匹配可能的大整数ID
  const bigIntRegex = /"(id|source|target)"\s*:\s*(\d{15,})/g
  
  // 将大整数用引号包裹，转换为字符串
  const safeJsonString = jsonString.replace(bigIntRegex, (match, key, value) => {
    return `"${key}":"${value}"`
  })
  
  return JSON.parse(safeJsonString)
}

/**
 * 验证雪花算法ID格式
 */
export const isValidSnowflakeId = (id: string): boolean => {
  const snowflakePattern = /^\d{17,19}$/
  return snowflakePattern.test(id)
}