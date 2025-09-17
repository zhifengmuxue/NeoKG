package top.zfmx.neokgbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Keyword {
    private String tag;
    private String description;
    private String vector;
    private List<String> alias;
    private DocumentRef ref;
}
