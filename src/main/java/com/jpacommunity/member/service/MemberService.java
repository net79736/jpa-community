package com.jpacommunity.member.service;

import com.jpacommunity.global.exception.JpaCommunityException;
import com.jpacommunity.jwt.util.JwtProvider;
import com.jpacommunity.member.controller.response.MemberResponse;
import com.jpacommunity.member.dto.create.MemberCreateRequest;
import com.jpacommunity.member.dto.update.MemberRoleUpdateRequest;
import com.jpacommunity.member.dto.update.MemberStatusUpdateRequest;
import com.jpacommunity.member.dto.update.MemberUpdateRequest;
import com.jpacommunity.member.dto.update.PasswordChangeRequest;
import com.jpacommunity.member.entity.Member;
import com.jpacommunity.member.repository.MemberJpaRepository;
import com.jpacommunity.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.jpacommunity.common.dto.validator.JpaCommunityValidator.validate;
import static com.jpacommunity.global.exception.ErrorCode.*;
import static com.jpacommunity.jwt.util.JwtProvider.TOKEN_PREFIX;
import static com.jpacommunity.member.domain.MemberStatus.PENDING;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberJpaRepository memberJpaRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse create(MemberCreateRequest memberCreateRequest) throws JpaCommunityException {
        // 1. 동일한 유저 이름 존재 검사
        Optional<Member> memberOP = memberRepository.findByEmail(memberCreateRequest.getEmail());

        if (memberOP.isPresent()) {
            // 아이디가 중복 되었다는 것
            throw new JpaCommunityException(USER_ALREADY_EXISTS);
        }

        // 2. 패스워드인코딩 + 회원 가입
        Member member = memberJpaRepository.save(new Member(memberCreateRequest, passwordEncoder));

        // 4. dto 응답
        return new MemberResponse(member);
    }

    @Transactional
    public MemberResponse update(String loginUserEmail, MemberUpdateRequest memberUpdateRequest) {
        // 1. 동일한 유저 이름 존재 검사
        Optional<Member> memberOP = memberRepository.findByEmail(loginUserEmail);

        // 2. 아이디 미존재 체크
        if (memberOP.isEmpty()) {
            throw new JpaCommunityException(USER_NOT_FOUND);
        }

        // 3. 자신의 계정이 아닌 다른 계정을 수정하려고 함
        if (!memberOP.get().verifyOwnEmail(memberUpdateRequest.getEmail())) {
            throw new JpaCommunityException(NO_PERMISSION);
        }

        // 4. 패스워드인코딩 + 회원 정보 변경
        Member member = memberOP.get();
        member.update(memberUpdateRequest, passwordEncoder);

        // 5. dto 응답
        return new MemberResponse(member);
    }

    public MemberResponse getByPublicId(UUID publicId) {
        return new MemberResponse(memberRepository.getByPublicId(publicId));
    }

    // 엔티티 반환 get~
    // public Member getByEmail(String email) {
    //     return memberJpaRepository.findByEmail(email)
    //             .orElseThrow(() -> new JpaCommunityException()(email + " 는 존재하지 않는 사용자 입니다"));
    // }

    // Optional 반환은 find~
    // public Member findByNickname(String nickname) {
    //     return memberJpaRepository.findByNickname(nickname)
    //             .orElseThrow(() -> new JpaCommunityException()(nickname + " 는 존재하지 않는 사용자 입니다"));
    // }

    public MemberResponse getByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(MemberResponse::new)
                .orElseThrow(() -> new JpaCommunityException(RESOURCE_NOT_FOUND, email + " 는 존재하지 않는 이메일 입니다"));
    }

    public MemberResponse getByNickname(String nickname) {
        return memberRepository.findByNickname(nickname)
                .map(MemberResponse::new)
                .orElseThrow(() -> new JpaCommunityException(RESOURCE_NOT_FOUND, nickname + " 는 존재하지 않는 닉네임 입니다"));
    }

    @Transactional
    public void delete(String requestEmail, String loginUserEmail, String password) {
        Member findMember = memberRepository.getByEmail(loginUserEmail);

        // 자신의 계정인지 체크
        boolean isOwnValid = findMember.verifyOwnEmail(requestEmail);
        if (!isOwnValid) throw new JpaCommunityException(NO_PERMISSION);

        // 비밀번호 일치 여부 확인
        boolean isPWValid = findMember.validatePassword(password, passwordEncoder);
        if (!isPWValid) throw new JpaCommunityException(NO_PERMISSION);

        memberJpaRepository.delete(findMember);
    }

    @Transactional
    public void delete(String email) {
        Member findMember = memberRepository.getByEmail(email);
        memberJpaRepository.delete(findMember);
    }

    @Transactional
    public MemberResponse updateRole(MemberRoleUpdateRequest memberRoleUpdateRequest) {
        boolean isValid = validate(memberRoleUpdateRequest);
        log.info("playHive updateRole isValid: {}", isValid);

        if (!isValid) {
              // 빈 Response 객체 반환
            throw new JpaCommunityException(NO_PERMISSION, "인증 키와 패스워드가 일치하지 않습니다");
        }

        // 1. 동일한 유저 이름 존재 검사
        Optional<Member> memberOP = memberRepository.findByEmail(memberRoleUpdateRequest.getEmail());

        // 2. 아이디 미존재 체크
        if (memberOP.isEmpty()) {
            throw new JpaCommunityException(USER_NOT_FOUND);
        }

        Member member = memberOP.get();
        member.updateType(memberRoleUpdateRequest.getRole());

        // 5. dto 응답
        return new MemberResponse(member);
    }

    public List<Member> list() {
        return Optional.of(memberJpaRepository.findAll()).orElse(Collections.emptyList());
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest passwordChangeRequest) {
        Member findMember = memberRepository.getByEmail(email);
        boolean isEqual = passwordChangeRequest.checkPasswordAndConfirmPassword();
        if (!isEqual) throw new JpaCommunityException(INVALID_PARAMETER, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        boolean isValid = findMember.validatePassword(passwordChangeRequest.getPassword(), passwordEncoder);
        if (!isValid) throw new JpaCommunityException(UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");

        findMember.updatePassword(passwordChangeRequest, passwordEncoder); // 비밀번호 변경
    }

    @Transactional
    public void updateStatus(String targetEmail, MemberStatusUpdateRequest memberStatusUpdateRequest) {
        log.info("토큰에서 추출된 이메일: {}, 상태를 변경할 대상 이메일: {}, 새로운 상태: {}",
                targetEmail, memberStatusUpdateRequest.getEmail(), memberStatusUpdateRequest.getStatus().name());

        // 요청자와 대상 사용자 정보 조회
        Member requester = memberRepository.getByEmail(targetEmail); // 요청자
        Member targetMember = memberRepository.getByEmail(memberStatusUpdateRequest.getEmail()); // 상태 변경 대상자

        // 1. 요청자가 본인의 상태를 변경하려는 경우
        if (requester.verifyOwnEmail(memberStatusUpdateRequest.getEmail())) {
            log.info("사용자가 자신의 상태를 변경 중: {}", targetEmail);
            if (!requester.getStatus().equals(PENDING)) throw new JpaCommunityException(NO_PERMISSION); // PENDING 인 경우에만 본인의 상태 변경 가능하도록 처리
            requester.updateStatus(memberStatusUpdateRequest.getStatus());
            return;
        }

        // 2. 관리자가 다른 사용자의 상태를 변경하려는 경우
        if (requester.isAdmin()) {
            log.info("관리자가 상태를 변경 중: {}, 대상자: {}", targetEmail, memberStatusUpdateRequest.getEmail());
            targetMember.updateStatus(memberStatusUpdateRequest.getStatus());
            return;
        }

        // 3. 권한 없는 사용자가 다른 사용자의 상태를 변경하려고 시도한 경우
        log.warn("권한 없는 요청: 요청자 {}, 대상자 {}", targetEmail, memberStatusUpdateRequest.getEmail());
        throw new JpaCommunityException(NO_PERMISSION, "상태 수정 권한이 없습니다.");
    }

    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return memberJpaRepository.existsByNickname(nickname);
    }

    /**
     * publicId 를 통한 사용자 아이디 조회
     *
     * @param publicId token 에 저장할 고유 번호
     * @return
     */
    public String getCurrentLoginUserEmail(UUID publicId) {
        MemberResponse response = getByPublicId(publicId);
        return response != null ? response.getEmail() : null;
    }

    /**
     * jwt 토큰에서 publicId 를 추출한다.
     *
     * @param authorizationHeader JWT 토큰
     * @return
     */
    public MemberResponse getAuthenticatedMember(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
            throw new JpaCommunityException(NO_PERMISSION);
        }

        String accessToken = jwtProvider.getAccessToken(authorizationHeader);
        UUID publicId = jwtProvider.getPublicId(accessToken);
        return getByPublicId(publicId);
    }
}
