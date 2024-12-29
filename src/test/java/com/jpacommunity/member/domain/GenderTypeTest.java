package com.jpacommunity.member.domain;

import com.jpacommunity.member.domain.GenderType;
import org.junit.jupiter.api.Test;

class GenderTypeTest {

    // END 는 자동 완성 후 커서 위치
    @Test
    public void gender_test() throws Exception {
        // given
        String gender = "MALE";

        GenderType genderType = GenderType.fromValue(gender);

        System.out.println(genderType);
        System.out.println(genderType.getValue());
        System.out.println(genderType.name());

        GenderType[] values = GenderType.values();

        for (GenderType value : values) {
            System.out.println(value);
        }
        // when

        // then
    }

    public void gender_test_2() throws Exception {
        String gender = "F";
    }

}