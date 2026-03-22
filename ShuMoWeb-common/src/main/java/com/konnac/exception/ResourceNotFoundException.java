package com.konnac.exception;

public class ResourceNotFoundException extends BusinessException{
    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    public ResourceNotFoundException(String message, Object data) {
        super(404, message, data);
    }
}
