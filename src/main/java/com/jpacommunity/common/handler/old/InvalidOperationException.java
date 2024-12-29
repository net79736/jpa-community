package com.jpacommunity.common.handler.old;

public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String operation, String reason) {
        super("작업 '" + operation + "'을(를) 수행할 수 없습니다. 이유: " + reason);
    }
}
