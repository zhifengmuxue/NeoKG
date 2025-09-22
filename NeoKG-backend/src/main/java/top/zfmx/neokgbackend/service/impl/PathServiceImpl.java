package top.zfmx.neokgbackend.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.pojo.dto.GraphEdge;
import top.zfmx.neokgbackend.pojo.dto.GraphNode;
import top.zfmx.neokgbackend.repository.GraphRepository;
import top.zfmx.neokgbackend.service.PathService;

import java.util.List;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class PathServiceImpl implements PathService {

    @Resource
    private GraphRepository graphRepository;

    /**
     * 查询图中两节点之间的路径（支持 BFS ）
     *
     * @param startType 起点类型 DOCUMENT / KEYWORD
     * @param startId   起点 ID（docId 或 keywordId）
     * @param endType   终点类型 DOCUMENT / KEYWORD
     * @param endId     终点 ID（docId 或 keywordId）
     * @return 返回前端渲染的图数据 {nodes: [...], edges: [...]}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getPath(String startType, Long startId, String endType, Long endId) {

        // 调用 Repository 查询路径
        Map<String, Object> row = graphRepository.findPath(startType, startId, endType, endId);
        if (row.isEmpty()) {
            // 如果路径不存在，返回空图
            return Map.of("nodes", List.of(), "edges", List.of());
        }

        // 取出路径节点和路径关系
        List<Map<String, Object>> pathNodes = (List<Map<String, Object>>) row.get("pathNodes");
        List<Map<String, Object>> pathRels = (List<Map<String, Object>>) row.get("pathRels");

        // 构建 GraphNode 列表
        List<GraphNode> graphNodes = pathNodes.stream().map(n -> {
            String type = (String) n.get("type");  // DOCUMENT 或 KEYWORD
            long id = ((Number) n.get("id")).longValue();  // docId 或 keywordId
            String label = type.equals("DOCUMENT") ? "doc-" + id : "kw-" + id; // 唯一 id
            String name = (String) n.get("name"); // 节点显示名称
            String color = type.equals("DOCUMENT") ? "#5470C6" : "#91CC75"; // 文档蓝色，关键词绿色
            return new GraphNode(label, name, 20.0, color);
        }).toList();

        // 构建 GraphEdge 列表
        List<GraphEdge> graphEdges = pathRels.stream().map(r -> {
            // r 包含 start 和 end 的 Neo4j 内部节点 id
            String source = String.valueOf(r.get("start"));
            String target = String.valueOf(r.get("end"));
            return new GraphEdge(source, target);
        }).toList();

        // 返回给前端的图数据
        return Map.of(
                "nodes", graphNodes,
                "edges", graphEdges
        );
    }
}
