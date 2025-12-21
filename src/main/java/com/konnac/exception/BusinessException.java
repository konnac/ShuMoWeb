package com.konnac.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 */
@Getter
public class BusinessException extends RuntimeException {

    // 错误码
    private final Integer code;

    // 错误信息
    private final String message;

    // 错误详情（可选）
    private final Object data;

    /**
     * 构造方法1：只有错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500; // 默认错误码
        this.message = message;
        this.data = null;
    }

    /**
     * 构造方法2：错误信息和错误码
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = null;
    }

    /**
     * 构造方法3：错误码、错误信息和错误详情
     */
    public BusinessException(Integer code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造方法4：错误信息和原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
        this.data = null;
    }

    @Override
    public String toString() {
        return "BusinessException{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}