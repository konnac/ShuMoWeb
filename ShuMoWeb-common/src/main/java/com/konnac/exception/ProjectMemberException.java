package com.konnac.exception;

public class ProjectMemberException extends BusinessException{
    public ProjectMemberException(String message) {
        super(400, message);
    }

    public ProjectMemberException(Integer code, String message) {
        super(code, message);
    }

    public ProjectMemberException(String message, Object data) {
        super(400, message, data);
    }
}
