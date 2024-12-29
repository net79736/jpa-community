package com.jpacommunity.common.handler.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class JpaCommunityException extends RuntimeException {

    private final ErrorCode errorCode;
    private Map<String, String> errorMap;

    /**
     * @param errorCode ErrorCode에 정의된 메시지 반환
     */
    public JpaCommunityException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }

    /**
     * @param errorCode ErrorCode 에 정의된 메시지 반환
     * @param errorMap 필드에 대한 에러를 담은 Map
     */
    public JpaCommunityException(ErrorCode errorCode, Map<String, String> errorMap) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
        this.errorMap = errorMap;
    }

    /**
     * @param errorCode ErrorCode에 정의된 메시지 반환
     * @param message 정의되지 않은 예외 처리
     */
    public JpaCommunityException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
