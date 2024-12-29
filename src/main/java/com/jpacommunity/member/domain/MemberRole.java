package com.jpacommunity.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberRole {
    USER("일반회원"), ADMIN("관리자");
    private String value;
}
