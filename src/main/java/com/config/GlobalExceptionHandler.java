package com.config;

import com.entity.EIException;
import com.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EIException.class)
    public R handleEIException(EIException ex) {
        logger.warn("业务异常: {}", ex.getMsg(), ex);
        return R.error(ex.getCode(), StringUtils.defaultIfBlank(ex.getMsg(), "业务处理失败"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("参数异常: {}", ex.getMessage(), ex);
        return R.error(400, StringUtils.defaultIfBlank(ex.getMessage(), "请求参数不合法"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        logger.warn("参数校验失败: {}", ex.getMessage(), ex);
        return R.error(400, "参数校验失败，请检查输入");
    }

    @ExceptionHandler(MultipartException.class)
    public R handleMultipart(MultipartException ex) {
        logger.warn("文件上传异常: {}", ex.getMessage(), ex);
        return R.error(400, "文件上传失败，请检查文件格式或大小");
    }

    @ExceptionHandler(Exception.class)
    public R handleException(Exception ex) {
        logger.error("系统异常", ex);
        return R.error(500, "系统繁忙，请稍后重试");
    }
}
