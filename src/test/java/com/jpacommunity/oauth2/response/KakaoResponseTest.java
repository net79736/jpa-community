package com.jpacommunity.oauth2.response;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KakaoResponseTest {

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("+82 형태의 전화번호 정규표현식 돌린 후 테스트")
    public void tel_replace_test() throws Exception {
        // given
        String replace = StringUtils.defaultString("+82 010-1234-5678", "")
                .replaceAll("^\\+\\d{1,3}\\s{1,}", "") // 국가 코드 제거 (예: +82, +1 등)
                .replace("-", "")                  // 하이픈 제거
                .replace(" ", "");                 // 공백 제거
        // when
        // then
        Assertions.assertThat(replace).isEqualTo("01012345678");
    }

}