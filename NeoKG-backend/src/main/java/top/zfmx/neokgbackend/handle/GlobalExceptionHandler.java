package top.zfmx.neokgbackend.handle;


import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.xml.sax.SAXException;
import top.zfmx.neokgbackend.exception.BaseException;
import top.zfmx.neokgbackend.response.Result;

import java.io.IOException;


/**
 * 全局异常捕获
 *
 * @author lima
 * @version 0.0.1
 **/
@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IOException.class)
    public Result<String> handleIOException(IOException e) {
        return Result.error("文件读写异常: " + e.getMessage());
    }

    @ExceptionHandler(TikaException.class)
    public Result<String> handleTikaException(TikaException e) {
        return Result.error("文件解析失败: " + e.getMessage());
    }

    @ExceptionHandler(SAXException.class)
    public Result<String> handleSAXException(SAXException e) {
        return Result.error("XML 解析异常: " + e.getMessage());
    }

    // 捕获所有未处理的异常
    @ExceptionHandler(Exception.class)
    public Result<String> handleGeneralException(Exception e) {
        log.info(e.getMessage());
        return Result.error("系统异常: " + e.getMessage());
    }
}
