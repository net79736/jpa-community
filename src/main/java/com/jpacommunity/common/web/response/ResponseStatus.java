package com.jpacommunity.common.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseStatus {
    SUCCESS(1), FAIL(-1);
    private Integer value;
}