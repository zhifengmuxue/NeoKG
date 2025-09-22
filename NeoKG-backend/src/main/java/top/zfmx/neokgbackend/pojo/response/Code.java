package top.zfmx.neokgbackend.pojo.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码
 *
 * @author lima
 * @version 0.0.1
 **/
@Getter
@AllArgsConstructor
public enum Code {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FAILED(402, "请求失败"),
    INTERNAL_ERROR(500, "系统异常"),
    FORBIDDEN(403, "禁止访问"),
    CACHE_NULL(1001, "数据缓存尚未生成"),;

    private final int code;
    private final String message;
}

