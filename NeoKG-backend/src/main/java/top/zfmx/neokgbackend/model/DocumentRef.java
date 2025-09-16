package top.zfmx.neokgbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRef {
    private String documentId;
    private long index;
}
