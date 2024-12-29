package com.jpacommunity.global.exception.old;


// 그 외 API 익셉션 처리
public class GeneralApiException extends RuntimeException {
    public GeneralApiException(String message) {
        super(message);
    }
}
