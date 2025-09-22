package top.zfmx.neokgbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface DimReduceService {
    void reduceAndReplaceAll() throws JsonProcessingException;
}
