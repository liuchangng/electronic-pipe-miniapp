package com.score.config;

import com.score.common.R;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 全局异常处理
 *
 * @author Score Team
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 静态资源未找到（如 /uploads/xxx.jpg）— 返回标准404，不包装为JSON业务错误
     * 必须在通用 Exception handler 之前处理，否则会被吞掉
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResourceFound(NoResourceFoundException e, HttpServletResponse response) {
        // 不写入body，让容器返回空404
    }

    @ExceptionHandler(Exception.class)
    public R handleException(Exception e) {
        e.printStackTrace();
        return R.fail("服务器错误: " + e.getMessage());
    }

    @ExceptionHandler(cn.dev33.satoken.exception.NotLoginException.class)
    public R handleNotLoginException(cn.dev33.satoken.exception.NotLoginException e) {
        return R.fail("未登录: " + e.getMessage());
    }
}