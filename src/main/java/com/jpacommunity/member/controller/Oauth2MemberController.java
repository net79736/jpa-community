package com.jpacommunity.member.controller;

import com.jpacommunity.common.web.response.ResponseDto;
import com.jpacommunity.member.controller.response.Oauth2MemberResponse;
import com.jpacommunity.member.domain.validator.MemberValidator;
import com.jpacommunity.member.service.Oauth2MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.jpacommunity.common.web.response.ResponseStatus.FAIL;
import static com.jpacommunity.common.web.response.ResponseStatus.SUCCESS;
import static com.jpacommunity.global.exception.ErrorCode.INVALID_PARAMETER;

@Slf4j
@RestController
@RequestMapping("/api/oauth2/members")
@RequiredArgsConstructor
public class Oauth2MemberController {
    private final Oauth2MemberService oauth2MemberService;
    private final com.jpacommunity.jwt.util.JwtProvider jwtProvider;

    /**
     * 이메일로 사용자 조회
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable String email) {
        log.info("Oauth2MemberController getByEmail 메서드 실행 : {}", email);

        // 이메일 유효성 검사
        if (MemberValidator.validateEmail(email) == null) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("email", "이메일 형식으로 작성해주세요");
            return ResponseEntity.badRequest().body(
                    new ResponseDto<>(FAIL.getValue(), INVALID_PARAMETER.getMsg(), errorMap)
            );
        }

        Oauth2MemberResponse response = oauth2MemberService.getByEmail(email);
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "사용자 조회 성공", response));
    }
}
