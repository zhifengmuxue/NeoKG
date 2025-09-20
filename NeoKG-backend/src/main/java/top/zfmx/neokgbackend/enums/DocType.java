package top.zfmx.neokgbackend.enums;

/**
 * @author li ma
 * @version 0.0.1
 **/
public enum DocType {
    PDF,
    WORD,
    MARKDOWN,
    CSV;

    public static DocType fromFilename(String filename) {
        if (filename.endsWith(".csv")) {
            return CSV;
        } else if (filename.endsWith(".md") || filename.endsWith(".markdown")) {
            return MARKDOWN;
        } else if (filename.endsWith(".doc") || filename.endsWith(".docx")) {
            return WORD;
        } else if (filename.endsWith(".pdf")) {
            return PDF;
        } else {
            throw new IllegalArgumentException("不支持的文件类型: " + filename);
        }
    }
}