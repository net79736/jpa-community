package com.jpacommunity.common.handler.old;

// 예: 이메일, 사용자 이름, 고유 키 값 등이 중복된 경우.
// 데이터베이스의 고유 제약 조건을 위반했을 때.
// 외래 키 제약 조건 검사
public class DataIntegrityViolationException extends RuntimeException {
    public DataIntegrityViolationException(String resource, String field, String value) {
        super(resource + "에서 " + field + " '" + value + "'가 이미 존재합니다.");
    }
}
