package com.jpacommunity.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpacommunity.member.domain.GenderType;
import com.jpacommunity.member.domain.MemberRole;
import com.jpacommunity.member.domain.MemberStatus;
import com.jpacommunity.member.domain.MemberType;
import com.jpacommunity.member.dto.update.MemberRoleUpdateRequest;
import com.jpacommunity.member.dto.update.MemberStatusUpdateRequest;
import com.jpacommunity.member.entity.Member;
import com.jpacommunity.member.repository.MemberRepository;
import com.jpacommunity.member.service.MemberService;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jpacommunity.jwt.util.JwtProvider;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import static com.jpacommunity.jwt.util.JwtProvider.TOKEN_CATEGORY_ACCESS;
import static com.jpacommunity.jwt.util.JwtProvider.TOKEN_CATEGORY_REFRESH;
import static com.jpacommunity.member.domain.MemberRole.USER;
import static com.jpacommunity.member.domain.MemberStatus.ACTIVE;
import static com.jpacommunity.member.domain.MemberStatus.PENDING;
import static com.jpacommunity.member.domain.MemberType.LOCAL;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@WithUserDetails(value = "net1506@naver.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@DisplayName("멤버 Service 테스트")
class MemberServiceTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper om;
    @Autowired
    EntityManager entityManager;
    @Autowired
    JwtProvider jwtProvider;
    String jwtToken = null;
    private final String publicId = "98b58825-23d9-48aa-bc37-11d87441aca3";
    private final String publicId2 = "98b58825-23d9-48aa-bc37-11d87441aca4";
    private final String publicId3 = "98b58825-23d9-48aa-bc37-11d87441aca5";
    public final static String TOKEN_PREFIX = "Bearer ";

    private Member member;
    private Member member2;
    private Member member3;
    @Autowired
    private MemberService memberService;

    @BeforeEach
    public void init() {
        Member.MemberBuilder builder = Member.builder();
        builder.email("net1506@naver.com");
        builder.tel("01077776666");
        builder.name("jongwook");
        builder.password(passwordEncoder.encode("12345"));
        builder.nickname("일반 계정 종욱");
        builder.gender(GenderType.M);
        builder.birthdate(LocalDate.of(2011, 1, 13));
        builder.role(USER);
        builder.type(LOCAL);
        builder.status(PENDING);
        builder.publicId(UUID.fromString("98b58825-23d9-48aa-bc37-11d87441aca3"));
        member = builder
                .build();

        entityManager.merge(member);

        member2 = Member.builder()
                .email("net1507@naver.com")
                .tel("01077776666")
                .name("jongwook")
                .password(passwordEncoder.encode("12345"))
                .nickname("관리자 계정 종욱")
                .gender(GenderType.M)
                .birthdate(LocalDate.of(2011, 1, 13))
                .role(MemberRole.ADMIN)
                .type(LOCAL)
                .status(ACTIVE)
                .publicId(UUID.fromString("98b58825-23d9-48aa-bc37-11d87441aca4"))
                .build();

        entityManager.merge(member2);

        member3 = Member.builder()
                .email("net1510@naver.com")
                .tel("01077779999")
                .name("jongwook2")
                .password(passwordEncoder.encode("12345"))
                .nickname("수정될 계정")
                .gender(GenderType.M)
                .birthdate(LocalDate.of(2011, 1, 13))
                .role(USER)
                .type(LOCAL)
                .status(MemberStatus.PENDING)
                .publicId(UUID.fromString("98b58825-23d9-48aa-bc37-11d87441aca5"))
                .build();

        entityManager.merge(member3);
        entityManager.flush();
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @Disabled
    public void token_invalid_test() throws Throwable {
        // given
        // 30sec 유효 토큰
        String encode = URLEncoder.encode(jwtToken, StandardCharsets.UTF_8);
        System.out.println(encode);

        // when
        Boolean expired = jwtProvider.isExpired(jwtToken);

        // then
        Assertions.assertThat(expired).isFalse();
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("존재하지 않는 계정을 조회 시 서비스 단에서 PlayHiveException 을 반환한다.")
    public void select_test() throws Exception {
        // given
        String jwtToken = getAccessToken();
        String decode = URLDecoder.decode(jwtToken, StandardCharsets.UTF_8);

        // when
        // then
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> memberService.getByEmail("hihi"));
        String message = runtimeException.getMessage();
        System.out.println(message);
    }

    @Test
    @DisplayName("유저 타입을 업데이트 한다.")
    public void update_type_test() throws Exception {
        // given
        MemberRoleUpdateRequest memberRoleUpdateRequest = MemberRoleUpdateRequest.builder()
                .email("net1506@naver.com")
                .role(MemberRole.ADMIN)
                .clientId("1ZDYmrYNwVcioFZNKVQ5VSqylF")
                .secretKey("RK1j0C33CUexMs")
                .build();

        memberService.updateRole(memberRoleUpdateRequest);
    }

    @Test
    @DisplayName("유저 타입을 업데이트 한다.")
    public void update_type_web_test() throws Exception {
        // given
        MemberRoleUpdateRequest memberRoleUpdateRequest = MemberRoleUpdateRequest.builder()
                .email("net1506@naver.com")
                .role(USER)
                .clientId("1ZDYmrYNwVcioFZNKVQ5VSqylF")
                .secretKey("RK1j0C33CUexMs")
                .build();

        String requestBody = om.writeValueAsString(memberRoleUpdateRequest);
        requestBody = requestBody.replace("USER", "ADMIN");

        System.out.println(requestBody);

        ResultActions resultActions = mockMvc.perform(put("/api/members/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("본인의 유저 상태를 업데이트 한다.")
    public void update_self_status_web_test() throws Exception {
        // given
        String jwtToken = getAccessToken(); // 일반 계정
        jwtToken = decode(jwtToken);
        System.out.println("jwtToken print : " + jwtToken);

        MemberStatusUpdateRequest memberStatusUpdateRequest = MemberStatusUpdateRequest.builder()
                .email("net1506@naver.com")
                .status(MemberStatus.INACTIVE)
                .build();

        String requestBody = om.writeValueAsString(memberStatusUpdateRequest);
        // requestBody = requestBody.replace("USER", "ADMIN");

        System.out.println(requestBody);

        ResultActions resultActions = mockMvc.perform(put("/api/members/status")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자가 아닌 사람이 다른 유저의 상태를 업데이트 하면 에러가 발생한다.")
    public void update_no_admin_user_to_other_status_web_test() throws Exception {
        // given
        String jwtToken = getAccessToken(); // 일반 계정
        jwtToken = decode(jwtToken);
        System.out.println("jwtToken print : " + jwtToken);

        MemberStatusUpdateRequest memberStatusUpdateRequest = MemberStatusUpdateRequest.builder()
                .email("net1510@naver.com")
                .status(MemberStatus.INACTIVE)
                .build();

        String requestBody = om.writeValueAsString(memberStatusUpdateRequest);
        // requestBody = requestBody.replace("USER", "ADMIN");

        System.out.println(requestBody);

        ResultActions resultActions = mockMvc.perform(put("/api/members/status")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자인 사람이 다른 유저의 상태를 업데이트 하면 정상 작동한다.")
    public void update_admin_user_to_other_status_web_test() throws Exception {
        // given
        String jwtToken = getAdminAccessToken(); // 일반 계정
        jwtToken = decode(jwtToken);
        System.out.println("jwtToken print : " + jwtToken);

        MemberStatusUpdateRequest memberStatusUpdateRequest = MemberStatusUpdateRequest.builder()
                .email("net1510@naver.com")
                .status(MemberStatus.INACTIVE)
                .build();

        String requestBody = om.writeValueAsString(memberStatusUpdateRequest);
        // requestBody = requestBody.replace("USER", "ADMIN");

        System.out.println(requestBody);

        ResultActions resultActions = mockMvc.perform(put("/api/members/status")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        resultActions.andExpect(status().isOk());
    }

//    public MemberResponse getByEmail(String email) {
//        return memberRepository.findByEmail(email)
//                .map(MemberResponse::new)
//                .orElseThrow(() -> new PlayHiveException(email + " 는 존재하지 않는 이메일 입니다"));
//    }
//
//    public MemberResponse getByNickname(String nickname) {
//        return memberRepository.findByNickname(nickname)
//                .map(MemberResponse::new)
//                .orElseThrow(() -> new PlayHiveException(nickname + " 는 존재하지 않는 닉네임 입니다"));
//    }

    private String getRefreshToken() {
        return URLEncoder.encode(TOKEN_PREFIX + jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, Duration.ofSeconds(30), UUID.fromString(publicId), USER.name(), ACTIVE.name()), StandardCharsets.UTF_8);
    }

    private String getRefreshToken(Duration duration) {
        return URLEncoder.encode(TOKEN_PREFIX + jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, duration, UUID.fromString(publicId), USER.name(), ACTIVE.name()), StandardCharsets.UTF_8);
    }

    private String getAccessToken() {
        return URLEncoder.encode(TOKEN_PREFIX + jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofSeconds(30), UUID.fromString(publicId), USER.name(), ACTIVE.name()), StandardCharsets.UTF_8);
    }

    private String getAdminAccessToken() {
        return URLEncoder.encode(TOKEN_PREFIX + jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofSeconds(30), UUID.fromString(publicId2), MemberRole.ADMIN.name(), ACTIVE.name()), StandardCharsets.UTF_8);
    }

    private String getAccessToken(Duration duration) {
        return URLEncoder.encode(TOKEN_PREFIX + jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, duration, UUID.fromString(publicId), USER.name(), ACTIVE.name()), StandardCharsets.UTF_8);
    }

    private String decode(String token) {
        return URLDecoder.decode(token, StandardCharsets.UTF_8);
    }

    static class MemberLoginRequest {
        private String username;
        private String password;

        // Default Constructor
        public MemberLoginRequest() {}

        // All-Args Constructor
        public MemberLoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        // Getters
        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        // Setters
        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}