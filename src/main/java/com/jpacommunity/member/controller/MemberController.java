package com.jpacommunity.member.controller;


import com.jpacommunity.common.web.response.ResponseDto;
import com.jpacommunity.member.controller.response.MemberResponse;
import com.jpacommunity.member.dto.exist.ExistMemberRequest;
import com.jpacommunity.member.dto.update.MemberRoleUpdateRequest;
import com.jpacommunity.member.dto.update.MemberStatusUpdateRequest;
import com.jpacommunity.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.jpacommunity.jwt.util.JwtProvider;

import java.time.Duration;

import static com.jpacommunity.common.web.response.ResponseStatus.SUCCESS;
import static com.jpacommunity.jwt.util.JwtProvider.*;


@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    /**
     * 이메일로 사용자 존재 여부 확인
     */
    @GetMapping("/exists/email")
    public ResponseEntity<?> existsByEmail(@Valid ExistMemberRequest existMemberRequest, BindingResult bindingResult) {
        log.info("MemberController existsByEmail 메서드 실행 : {}", existMemberRequest.getEmail());
        boolean exists = memberService.existsByEmail(existMemberRequest.getEmail());
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "이메일 존재 여부 확인", exists));
    }

    /**
     * 닉네임으로 사용자 존재 여부 확인
     */
    @GetMapping("/exists/nickname")
    public ResponseEntity<?> existsByNickname(@Valid ExistMemberRequest existMemberRequest, BindingResult bindingResult) {
        log.info("MemberController existsByNickname 메서드 실행 : {}", existMemberRequest.getNickname());
        boolean exists = memberService.existsByNickname(existMemberRequest.getNickname());
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "닉네임 존재 여부 확인", exists));
    }

    // TODO_ : 헤더에 publicId 를 넣어달라고 하고 받아서... 끄내고 아이디 조회해서 그걸로 다시 own 검토하면 될 것으로 보임
    @PutMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestBody @Valid MemberStatusUpdateRequest memberStatusUpdateRequest,
                                          BindingResult bindingResult,
                                          HttpServletRequest httpServletRequest) {
        log.info("MyInfoController updateStatus 메서드 실행");
        String authorizationHeader = httpServletRequest.getHeader(HEADER_AUTHORIZATION);

        // accessToken 으로 부터 유저 정보 반환
        MemberResponse response = memberService.getAuthenticatedMember(authorizationHeader);

        log.info("email : {}" , response.getEmail());

        // 서비스 호출
        String targetEmail = response.getEmail(); // 변경을 시도하는 유저의 이메일 (본인 또는 관리자)

        memberService.updateStatus(targetEmail, memberStatusUpdateRequest);

        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "회원 상태가 성공적으로 변경되었습니다.", null));
    }

    @PutMapping("/role")
    public ResponseEntity<?> updateRole(@RequestBody @Valid MemberRoleUpdateRequest memberRoleUpdateRequest) {
        log.info("MemberController updateRole 메서드 실행. memberTypeUpdateRequest : {}", memberRoleUpdateRequest);
        MemberResponse response = memberService.updateRole(memberRoleUpdateRequest);
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "권한 변경 성공", response), HttpStatus.OK);
    }

    @GetMapping("/get-token/user/{email}")
    public ResponseEntity<?> getToken(@PathVariable String email) {
        log.info("getToken 메서드가 실행되었습니다.");
        MemberResponse response = memberService.getByEmail(email);
        String encode = TOKEN_PREFIX + jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofHours(10), response.getPublicId(), response.getRole().name(), response.getStatus().name());
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "토큰 조회 성공", encode), HttpStatus.OK);
    }
}
