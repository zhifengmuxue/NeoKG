// 1️⃣ 删除已存在的图（如果存在）
CALL gds.graph.exists('myGraph') YIELD exists
WITH exists
CALL apoc.do.when(
    exists,
    'CALL gds.graph.drop("myGraph") YIELD graphName RETURN graphName',
    'RETURN null AS graphName',
    {}
) YIELD value
RETURN value;

// 2️⃣ 给关系设置默认权重（Delta Stepping 需要权重）
MATCH ()-[r:HAS_KEYWORD]->()
SET r.weight = 1.0
RETURN count(r) AS updatedRelationships;

// 3️⃣ 创建 GDS 图，仅保留关系权重（不投影字符串属性）
CALL gds.graph.project(
  'myGraph',
  ['DocumentNode', 'KeywordNode'], 
  { HAS_KEYWORD: { type: 'HAS_KEYWORD', orientation: 'UNDIRECTED', properties: ['weight'] } }
)
YIELD graphName, nodeCount, relationshipCount;

