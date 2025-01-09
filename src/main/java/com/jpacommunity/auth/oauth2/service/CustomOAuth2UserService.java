package com.jpacommunity.auth.oauth2.service;

import com.jpacommunity.auth.oauth2.response.*;
import com.jpacommunity.global.exception.ExistingUserAuthenticationException;
import com.jpacommunity.global.exception.JpaCommunityException;
import com.jpacommunity.member.domain.MemberStatus;
import com.jpacommunity.member.domain.MemberType;
import com.jpacommunity.member.entity.Member;
import com.jpacommunity.member.entity.Oauth2Member;
import com.jpacommunity.member.repository.MemberJpaRepository;
import com.jpacommunity.member.repository.Oauth2MemberJpaRepository;
import com.jpacommunity.oauth2.dto.CustomOAuth2User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.jpacommunity.global.exception.ErrorCode.UNSUPPORTED_OAUTH_PROVIDER;
import static com.jpacommunity.global.exception.ErrorCode.USER_ALREADY_EXISTS;
import static com.jpacommunity.common.util.password.PasswordUtil.generateRandomPassword;
import static com.jpacommunity.member.domain.MemberRole.USER;
import static com.jpacommunity.member.domain.MemberStatus.PENDING;
import static com.jpacommunity.member.domain.MemberType.LOCAL;
import static com.jpacommunity.oauth2.constant.OAuth2ServiceProvider.*;


@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberJpaRepository memberJpaRepository;
    private final Oauth2MemberJpaRepository oauth2MemberJpaRepository;

    public CustomOAuth2UserService(MemberJpaRepository memberJpaRepository, Oauth2MemberJpaRepository oauth2MemberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
        this.oauth2MemberJpaRepository = oauth2MemberJpaRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("CustomOAuth2UserService > Oauth2User Request: {}", oAuth2User.toString());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = createOAuth2Response(registrationId, oAuth2User.getAttributes());

        // 지원하지 않는 PROVIDER
        if (oAuth2Response == null) {
            throw new JpaCommunityException(UNSUPPORTED_OAUTH_PROVIDER);
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String providerId = createProviderId(oAuth2Response);
        Optional<Member> existDataOP = memberJpaRepository.findByEmail(oAuth2Response.getEmail());

        if (existDataOP.isPresent()) {
            return handleExistingMember(existDataOP.get(), oAuth2Response);
        } else {
            // 신규 회원
            log.debug("CustomOAuth2UserService create NewUser");
            log.debug("providerId : {}", providerId);
            log.debug("email : {}", oAuth2Response.getEmail());
            log.debug("name : {}", oAuth2Response.getName());
            log.debug("MemberType.SOCIAL : {}", LOCAL);
            log.debug("MemberRole.ROLE_USER : {}", USER);
            log.debug("Provider() : {}", oAuth2Response.getProvider());
            log.debug("registrationId : {}", registrationId);

            return createNewMember(oAuth2Response, providerId); // 우선 Oauth2 사용자 테이블에 저장;
        }
    }

    /**
     * 기존 존재하는 회원 처리
     */
    private OAuth2User handleExistingMember(Member member, OAuth2Response oAuth2Response) {
        Optional<Member> memberByEmailAndType = memberJpaRepository.findByEmailAndType(
                oAuth2Response.getEmail(),
                MemberType.fromOAuth2Provider(oAuth2Response.getProvider())
        );

        if (memberByEmailAndType.isPresent()) {
            // 로그인한 소셜 정보가 이미 존재하는 경우 업데이트 처리
            log.debug("CustomOAuth2UserService isPresentUser");
            log.debug("providerId : {}", oAuth2Response.getProvider());
            log.debug("email : {}", oAuth2Response.getEmail());
            log.debug("name : {}", oAuth2Response.getName());
            log.debug("provider : {}", oAuth2Response.getProvider());
            log.debug("provider.name : {}", MemberType.fromOAuth2Provider(oAuth2Response.getProvider()).name());
            log.debug("provider.status : {}", member.getStatus().name());

            member.updateEmail(oAuth2Response.getEmail());

            return new CustomOAuth2User(member.getEmail(), member.getRole().name(), member.getPublicId(), member.getStatus());
        } else {
            // 로컬 이메일 계정으로 존재하는 유저
            throw new ExistingUserAuthenticationException(
                    "로컬 이메일 계정으로 이미 가입된 이메일입니다: " + maskEmail(oAuth2Response.getEmail())
            );
        }
    }

    private OAuth2User createNewMember(OAuth2Response oAuth2Response, String providerId) {
        log.debug("Creating new member for providerId: {}", providerId);

        UUID publicId = UUID.randomUUID();
        Oauth2Member newMember = Oauth2Member.builder()
                .email(oAuth2Response.getEmail())
                .password(generateRandomPassword())
                .name(oAuth2Response.getName())
                .role(USER)
                .tel(oAuth2Response.getTel())
                .nickname(oAuth2Response.getNickname())
                .gender(oAuth2Response.getGender())
                .birthdate(oAuth2Response.getBirthdate())
                .publicId(publicId)
                .type(MemberType.fromOAuth2Provider(oAuth2Response.getProvider()))
                .build();

        oauth2MemberJpaRepository.save(newMember); // OAuth2 사용자 테이블에 보관. 추후 사용자가 추가 항목 기입 후 회원가입 완료하면 일반 사용자 테이블에 저장
        return new CustomOAuth2User(oAuth2Response.getEmail(), USER.name(), publicId, MemberStatus.PENDING);
    }

    /**
     * provider 값을 사용하여 providerId 를 생성한다.
     */
    private String createProviderId(OAuth2Response oAuth2Response) {
        return oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
    }

    /**
     * provider 를 기준으로 OAuth2Response 를 생성한다.
     */
    private OAuth2Response createOAuth2Response(String registrationId, Map<String, Object> attributes) {
        switch (registrationId) {
            case NAVER:
                return new NaverResponse(attributes);
            case GOOGLE:
                return new GoogleResponse(attributes);
            case DISCORD:
                return new DiscordResponse(attributes);
            case KAKAO:
                return new KakaoResponse(attributes);
            default:
                return null;
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        // 이메일 분리 (아이디와 도메인 부분)
        int atIndex = email.indexOf("@");
        String localPart = email.substring(0, atIndex); // 아이디 부분
        String domainPart = email.substring(atIndex);  // 도메인 부분

        // 아이디의 앞 3글자는 유지, 나머지는 '*'로 마스킹
        if (localPart.length() <= 3) {
            return localPart + domainPart;
        }

        String visiblePart = localPart.substring(0, 3); // 앞 3글자
        String maskedPart = "*".repeat(localPart.length() - 3); // 나머지는 '*'
        return visiblePart + maskedPart + domainPart;
    }
}