package com.jpacommunity.member.domain.validator;

import java.util.regex.Pattern;

public class MemberValidator {
    private static final String TEL_PATTERN = "^010[0-9]{8}$";

    public static String validateTel(String tel) {
        if (tel != null && Pattern.matches(TEL_PATTERN, tel)) {
            return tel; // 유효한 값 반환
        }
        return null; // 유효하지 않으면 null 반환
    }
}
