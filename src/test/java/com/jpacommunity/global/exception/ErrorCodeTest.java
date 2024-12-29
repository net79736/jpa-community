package com.jpacommunity.global.exception;

import com.jpacommunity.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class ErrorCodeTest {

    // END 는 자동 완성 후 커서 위치
    @Test
    public void print_test() throws Exception {
        // given
        String name = ErrorCode.INTERNAL_SERVER_ERROR.name();
        System.out.println(name);

        // when

        // then
    }

}