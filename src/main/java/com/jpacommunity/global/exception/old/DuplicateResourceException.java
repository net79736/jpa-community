package com.jpacommunity.global.exception.old;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String resource, String field, String value) {
        super(String.format("리소스 '%s'의 '%s' 필드 값 '%s'가 데이터 무결성 제약 조건을 위반했습니다.", resource, field, value));
    }
}
