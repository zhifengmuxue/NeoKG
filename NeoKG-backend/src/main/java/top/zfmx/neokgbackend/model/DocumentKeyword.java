package top.zfmx.neokgbackend.model;

import lombok.Data;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
public class DocumentKeyword {
    private long id;
    private long documentId;
    private long keywordId;
}
