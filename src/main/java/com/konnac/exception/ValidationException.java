package com.konnac.exception;

public class ValidationException extends BusinessException{
    public ValidationException(String message) {
        super(400, message);
    }

    public ValidationException(String message, Object data) {
        super(400, message, data);
    }
}
