package com.jpacommunity.common.handler.exception;

// 인증되지 않은 사용자
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("인증되지 않은 사용자입니다. 로그인 후 다시 시도해 주세요.");
    }

    public UnauthorizedException(String resource) {
        super("해당 " + resource + "에 접근할 권한이 없습니다. 로그인 후 다시 시도해 주세요.");
    }
}
