package top.zfmx.neokgbackend.result;

import lombok.Data;

import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
public class FileParseResult {
    /* 正文 */
    private String content;
    /* mine类型 */
    private String mimeType;
    /* 元数据 */
    private Map<String, String> metadata;
}
