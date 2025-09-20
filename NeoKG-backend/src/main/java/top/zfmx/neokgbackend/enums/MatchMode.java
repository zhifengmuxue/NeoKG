package top.zfmx.neokgbackend.enums;

/**
 * @author li ma
 * @version 0.0.1
 **/
public enum MatchMode {
    STRING, SEMANTIC, BOTH;

    /**
     * 将字符串转换为 MatchMode，忽略大小写
     * @param value 字符串值
     * @return MatchMode 枚举
     * @throws IllegalArgumentException 如果字符串无效
     */
    public static MatchMode fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("匹配模式不能为空");
        }
        try {
            return MatchMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "无效的匹配模式: " + value + "，可选值: STRING, SEMANTIC, BOTH"
            );
        }
    }
}
