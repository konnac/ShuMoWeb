package com.konnac.exception;

import com.konnac.pojo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURL(), e);
        return ResponseEntity.status(e.getCode()).body(Result.error(e.getCode(), e.getMessage()));
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURL(), e);
        return Result.error(500, "系统异常,请联系管理员");
    }

}
