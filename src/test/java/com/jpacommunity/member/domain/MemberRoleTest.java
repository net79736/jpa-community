package com.jpacommunity.member.domain;

import com.jpacommunity.common.web.response.ResponseStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MemberRoleTest {

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("ResponseStatus.SUCCESS.name() 출력 테스트 ")
    public void response_status_print_test() throws Exception {
        String name = ResponseStatus.SUCCESS.name();
        Assertions.assertThat(name).isEqualTo("SUCCESS");
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("이메일 정규식을 체크한다.")
    public void nickname_validateor_test() throws Exception {
        // 정규식
        String nicknameRegex = "^[a-zA-Z가-힣0-9_\\-]{1,20}$";

        // 테스트 이메일들
        String[] testNickname = {
                "bluehi090",
                "bluehi090...",
                "bluehi090---",
                "blue___hi",
                "김치종욱kokoko",
                "김치종욱kokoko9999김치종욱kokoko9999김치종욱kokoko9999",
        };

        // 정규식 패턴 생성
        Pattern pattern = Pattern.compile(nicknameRegex);

        // 이메일 검증
        for (String nickname : testNickname) {
            Matcher matcher = pattern.matcher(nickname);
            if (matcher.matches()) {
                System.out.println(nickname + " is valid.");
            } else {
                System.out.println(nickname + " is invalid.");
            }
        }
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("이메일 정규식을 체크한다.")
    public void email_validateor_test() throws Exception {
        // 정규식
        String emailRegex = "^[0-9a-zA-Z]+@[0-9a-zA-Z]+(\\.[a-zA-Z]{2,3}){1,2}$";

        // 테스트 이메일들
        String[] testEmails = {
                "example@test.com",
                "example@domain.co.kr",
                "example@domain.or",
                "user.name+test@domain.co.kr",
                "user.name+test@domain.co",
                "invalid-email",
                "user@domaincom"
        };

        // 정규식 패턴 생성
        Pattern pattern = Pattern.compile(emailRegex);

        // 이메일 검증
        for (String email : testEmails) {
            Matcher matcher = pattern.matcher(email);
            if (matcher.matches()) {
                System.out.println(email + " is valid.");
            } else {
                System.out.println(email + " is invalid.");
            }
        }
    }


    @Test
    @DisplayName("이메일 정규식을 체크한다.")
    public void password_validator_test() throws Exception {
        // 정규식
        String passwordRegex = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,15}$";

        // 테스트 이메일들
        String[] testPW = {
                "rlacl",
                "rlacl@",
                "rlaclJIK",
                "rlaclJIK@",
                "kfejisk12322",
                "kfejiSDS12322",
                "kfejiSDS12322@",
                "kfejiSDS12322!%",
        };

        // 정규식 패턴 생성
        Pattern pattern = Pattern.compile(passwordRegex);

        // 이메일 검증
        for (String pw : testPW) {
            Matcher matcher = pattern.matcher(pw);
            if (matcher.matches()) {
                System.out.println(pw + " is valid.");
            } else {
                System.out.println(pw + " is invalid.");
            }
        }
    }
}