package com.jpacommunity.member.controller;

import com.jpacommunity.common.web.response.ResponseDto;
import com.jpacommunity.member.controller.response.MemberResponse;
import com.jpacommunity.member.dto.create.MemberCreateRequest;
import com.jpacommunity.member.dto.delete.MemberDeleteRequest;
import com.jpacommunity.member.dto.update.MemberUpdateRequest;
import com.jpacommunity.member.dto.update.PasswordChangeRequest;
import com.jpacommunity.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.jpacommunity.security.dto.CustomUserDetails;

import com.jpacommunity.jwt.util.JwtProvider;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static com.jpacommunity.common.util.cookie.CookieUtil.createCookie;
import static com.jpacommunity.common.web.response.ResponseStatus.SUCCESS;
import static com.jpacommunity.jwt.controller.ReIssueController.LOGOUT_PATH;
import static com.jpacommunity.jwt.controller.ReIssueController.TOKEN_REISSUE_PATH;
import static com.jpacommunity.jwt.util.JwtProvider.*;


@Slf4j
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MyInfoController {
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid MemberCreateRequest memberCreateRequest,
                                    BindingResult bindingResult,
                                    HttpServletResponse httpServletResponse
    ) {
        log.info("MyInfoController create 메서드 실행");
        MemberResponse response = memberService.create(memberCreateRequest);

        // Authorization
        String accessToken = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofMinutes(10), response.getPublicId(), response.getRole().name(), response.getStatus().name());
        // X-Refresh-Token
        String refreshToken = jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, Duration.ofDays(1), response.getPublicId(), response.getRole().name(), response.getStatus().name());
        // URLEncoder.encode: 공백을 %2B 로 처리
        String cookieValue = URLEncoder.encode(TOKEN_PREFIX + refreshToken, StandardCharsets.UTF_8);

        // 응답 헤더 설정
        httpServletResponse.addHeader(HEADER_AUTHORIZATION, TOKEN_PREFIX + accessToken);
        httpServletResponse.addCookie(createCookie(REFRESH_TOKEN_KEY, cookieValue, TOKEN_REISSUE_PATH, 24 * 60 * 60, true));
        httpServletResponse.addCookie(createCookie(REFRESH_TOKEN_KEY, cookieValue, LOGOUT_PATH, 24 * 60 * 60, true));
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "회원가입 성공", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> get(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController get 메서드 실행");
        log.info("publicId : {}", userDetails.getPublicId());

        UUID publicId = userDetails.getPublicId();
        MemberResponse response = memberService.getByPublicId(publicId);
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "로그인 회원 정보 조회 성공", response), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody @Valid MemberUpdateRequest memberUpdateRequest,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController update 메서드 실행 : {}", memberUpdateRequest.toString());
        String loginUserEmail = memberService.getCurrentLoginUserEmail(userDetails.getPublicId()); // 현재 로그인한 사용자 이메일
        MemberResponse response = memberService.update(loginUserEmail, memberUpdateRequest);
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "회원정보 수정 성공", response), HttpStatus.OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordChangeRequest passwordChangeRequest,
                                            BindingResult bindingResult,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController changePassword 메서드 실행 : {}", passwordChangeRequest.toString());
        String email = memberService.getCurrentLoginUserEmail(userDetails.getPublicId()); // 현재 로그인한 사용자 이메일
        memberService.changePassword(email, passwordChangeRequest);
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "비밀번호 변경 성공", null), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody @Valid MemberDeleteRequest memberDeleteRequest,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController delete 메서드 실행");
        String loginUserEmail = memberService.getCurrentLoginUserEmail(userDetails.getPublicId()); // 현재 로그인한 사용자 이메일

        memberService.delete(memberDeleteRequest.getEmail(), loginUserEmail, memberDeleteRequest.getPassword());

        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "회원 삭제 성공", null), HttpStatus.OK);
    }
}
