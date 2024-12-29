package com.jpacommunity.member.dto.create;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MemberCreateRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9a-zA-Z]+@[0-9a-zA-Z]+(\\.[a-zA-Z]{2,3}){1,2}$", message = "이메일 형식으로 작성해주세요")
    private String email; // 계정

    @NotBlank
    // @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,15}$", message = "비밀번호는 영문+숫자 조합 4 ~ 10자 이내로 입력해주세요")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{4,15}$", message = "비밀번호는 영문, 숫자 조합 4 ~ 15자 이내로 입력해주세요")
    private String password; // 비밀번호

    @NotBlank
    @Pattern(regexp = "^010[0-9]{8}$", message = "연락처는 '010'으로 시작하고 뒤에 8자리 숫자로 작성해주세요.")
    private String tel; // 전화번호

    @Pattern(regexp = "^[a-zA-Z가-힣]{1,20}$", message = "한글/영문 1~20자 이내로 작성해주세요")
    private String name; // 이름

    // _-. 를 포함하는 닉네임 생성 가능
    @Pattern(regexp = "^[a-zA-Z가-힣0-9_\\-]{1,20}$", message = "한글/영문/_- 1~20자 이내로 작성해주세요")
    private String nickname;

    // YYYY-MM-dd 형식
    @Column(name = "birth_date")
    private LocalDate birthdate;

    @Pattern(regexp = "^(?i)(MALE|FEMALE)$", message = "성별은 MALE, FEMALE 중 하나여야 합니다.")
    private String gender;
}
