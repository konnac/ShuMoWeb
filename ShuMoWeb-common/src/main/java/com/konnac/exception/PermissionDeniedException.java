package com.konnac.exception;

public class PermissionDeniedException extends BusinessException{
    public PermissionDeniedException(String message) {
        super(403, message);
    }

    public PermissionDeniedException(String message, Object data) {
        super(403, message, data);
    }
}
