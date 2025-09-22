package top.zfmx.neokgbackend.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 路径查询服务：支持 Dijkstra 和 BFS
 *
 * @author li ma
 * @version 0.0.2
 **/
@Service
public interface PathService {

    @SuppressWarnings("unchecked")
    Map<String, Object> getPath(String startType, Long startId, String endType, Long endId);
}

