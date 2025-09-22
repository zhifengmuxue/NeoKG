package top.zfmx.neokgbackend.service;

import org.apache.tika.exception.TikaException;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.pojo.dto.ExtractedGraph;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;

import java.io.IOException;
import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface DataImportMetaService {
    ExtractedGraph parseFile(MultipartFile file,
                             List<EntityType> entityTypes,
                             List<RelationType> relationTypes) throws IOException, TikaException;

    ExtractedGraph parseCsv(MultipartFile file, List<EntityType> entityTypes, List<RelationType> relationTypes) throws IOException;
    ExtractedGraph parseMarkdown(MultipartFile file, List<EntityType> entityTypes, List<RelationType> relationTypes) throws IOException;
    ExtractedGraph parseWordOrPdf(MultipartFile file, List<EntityType> entityTypes, List<RelationType> relationTypes) throws IOException, TikaException;

}
