package top.zfmx.neokgbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRef {
    private Long id;
    private Long documentId;
    private Long keywordId;
    private Long refIndex;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
