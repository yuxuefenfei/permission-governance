package com.example.permissionhub.exception;

import lombok.Getter;

/** 可预期的业务错误；不泄漏内部异常和 SQL 信息。 */
@Getter
public class BusinessException extends RuntimeException {
    private final int status;
    public BusinessException(int status, String message) {
        super(message);
        this.status = status;
    }
}
