package com.jpacommunity.common.handler.old;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String operation, String limit) {
        super(operation + " 작업의 제한 횟수(" + limit + ")를 초과했습니다. 잠시 후 다시 시도해주세요.");
    }
}
