package top.zfmx.neokgbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* 
* @author li ma
* @version 0.0.1
**/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    private long id;
    private String title;
    private String content;
    private List<Keyword> keywords;
}
