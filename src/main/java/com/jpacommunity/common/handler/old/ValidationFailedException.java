package com.jpacommunity.common.handler.old;


import lombok.Getter;

import java.util.Map;

// DML (Data Manipulation Language) 에러 처리
@Getter
public class ValidationFailedException extends RuntimeException {
    private Map<String, String> errorMap;

    public ValidationFailedException(String message, Map<String, String> errorMap) {
        super(message);
        this.errorMap = errorMap;
    }
}
