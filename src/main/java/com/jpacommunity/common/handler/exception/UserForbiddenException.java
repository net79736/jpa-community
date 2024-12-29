package com.jpacommunity.common.handler.exception;


// 인증은 되었으나 권한이 없음
public class UserForbiddenException extends RuntimeException {
    public UserForbiddenException(String message) {
        super(message);
    }
}
