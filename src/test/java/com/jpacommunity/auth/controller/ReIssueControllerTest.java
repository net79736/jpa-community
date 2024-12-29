package com.jpacommunity.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpacommunity.JpaCommunityApplication;
import com.jpacommunity.jwt.entity.Refresh;
import com.jpacommunity.member.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import com.jpacommunity.jwt.util.JwtProvider;
import com.jpacommunity.jwt.repository.RefreshJpaRepository;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static com.jpacommunity.common.util.cookie.CookieUtil.createCookie;
import static com.jpacommunity.jwt.util.JwtProvider.*;
import static com.jpacommunity.member.domain.GenderType.M;
import static com.jpacommunity.member.domain.MemberRole.USER;
import static com.jpacommunity.member.domain.MemberStatus.ACTIVE;
import static com.jpacommunity.member.domain.MemberType.LOCAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
// @WithUserDetails(value = "net1506@naver.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class ReIssueControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager em;
    @Autowired
    JwtProvider jwtUtil;
    @Autowired
    EntityManager entityManager;
    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper om;
    @Autowired
    RefreshJpaRepository refreshJpaRepository;
    private final String publicId = "98b58825-23d9-48aa-bc37-11d87441aca1";
    private Member member;

    @BeforeEach
    public void init() {
        member = Member.builder()
                .email("net1506@naver.com")
                .tel("01077776666")
                .name("jongwook")
                .password(passwordEncoder.encode("12345"))
                .nickname("play_gogo종욱")
                .gender(M)
                .birthdate(LocalDate.of(2011, 1, 13))
                .role(USER)
                .type(LOCAL)
                .status(ACTIVE)
                .publicId(UUID.fromString("98b58825-23d9-48aa-bc37-11d87441aca1"))
                .build();

        entityManager.merge(member);
        entityManager.flush();
    }

    @Test
    @DisplayName("만료된 어세스 토큰을 사용하여 API 통신시 에러를 반환한다.")
    public void expired_access_code_erorr_code_test() throws Exception {
        // given
        String jwtToken = decode(getAccessToken(Duration.ofMillis(-100)));

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/me")
                .header("Authorization", "Bearer " + jwtToken + "kkkk")
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized()) // 401 상태 코드 확인
                .andExpect(jsonPath("$.message").value("Invalid or expired token.")); // 에러 메시지 확인
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    public void jwt_token_decode_test() throws Exception {
        // given
        String jwtToken = getAccessToken();
        System.out.println("#### Token 출력 : " + jwtToken);

        // when
        String decode = URLDecoder.decode(jwtToken, StandardCharsets.UTF_8);

        // then
        System.out.println(decode);
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    public void jwt_provider_test() throws Exception {
        // given
        String jwtToken = getAccessToken(Duration.ofMillis(100));

        System.out.println(jwtToken);
        System.out.println(URLDecoder.decode(jwtToken, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("유효 토큰으로 API 처리 정상")
    public void normal_access_code_erorr_code_test() throws Exception {
        // given
        String jwtToken = decode(getAccessToken(Duration.ofMinutes(10)));

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/me")
                .header("Authorization", "Bearer " + jwtToken)
        );

        // then
        resultActions.andExpect(status().isOk()); // 401 상태 코드 확인
    }

    @Test
    @DisplayName("로그인 시 리프레시 토큰이 저장되는지 테스트")
    public void refresh_token_store_test() throws Exception {
        MemberLoginRequest memberSaveReqDto = new MemberLoginRequest();
        memberSaveReqDto.username = "net1506@naver.com";
        memberSaveReqDto.password = "12345";

        String requestBody = om.writeValueAsString(memberSaveReqDto);

        // when
        ResultActions resultActions = mockMvc.perform(get("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        // then
        resultActions.andExpect(status().isOk()); // 401 상태 코드 확인
    }

    @Test
    @DisplayName("정상적인 리프레시 토큰 발급 처리 후 기존 DB 리프레시 코드 삭제 확인")
    public void normal_refresh_process_test() throws Exception {
        // given
        MemberLoginRequest memberSaveReqDto = new MemberLoginRequest();
        memberSaveReqDto.username = "net1506@naver.com";
        memberSaveReqDto.password = "12345";

        String requestBody = om.writeValueAsString(memberSaveReqDto);

        mockMvc.perform(get("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        final String REFRESH_TOKEN_KEY = "X-Refresh-Token";
        final UUID publicId = member.getPublicId();
        Optional<Refresh> byRefreshAndPublicId = refreshJpaRepository.findByPublicId(publicId);

        Cookie cookie = createCookie(REFRESH_TOKEN_KEY, URLEncoder.encode(TOKEN_PREFIX + byRefreshAndPublicId.get().getRefresh(), StandardCharsets.UTF_8), "/reissue",24 * 60 * 60, true);

        // when
        ResultActions resultActions = mockMvc.perform(post("/reissue")
                .cookie(cookie)
        );

        resultActions.andExpect(status().isOk());

        // then
        // 데이터베이스에서 새로운 refreshToken 가져오기
        // String newRefreshToken = refreshJpaRepository.findByPublicId(member.getPublicId())
        //         .map(Refresh::getRefresh)
        //         .orElseThrow(() -> new RuntimeException("새로운 리프레시 토큰이 없습니다."));

        // 기존 토큰과 비교하여 변경되었는지 확인
        // assertNotEquals(byRefreshAndPublicId.get().getRefresh(), newRefreshToken, "리프레시 토큰이 변경되지 않았습니다.");
        // assertNotEquals("새로 발급된 토큰 값은 기존의 것과 달라야 합니다.", byRefreshAndPublicId.get().getRefresh(), newRefreshToken);
    }

    @Test
    @DisplayName("만료된 리프레시 코드로 토큰 재발급 경우 DB 에 저장하지 않고 에러를 반환한다.")
    @Disabled
    public void expired_refresh_not_db_save_return_error_code_test() throws Exception {
        // given
        String refreshToken = getRefreshToken(Duration.ofMinutes(-100));

        // when


        // then
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("이상한 리프레시 코드로 들어온 경우 DB 에 저장하지 않고 에러를 반환한다.")
    public void invalid_refresh_not_db_save_return_error_code_test() throws Exception {
        // given
        MemberLoginRequest memberSaveReqDto = new MemberLoginRequest();
        memberSaveReqDto.username = "net1506@naver.com";
        memberSaveReqDto.password = "12345";

        String requestBody = om.writeValueAsString(memberSaveReqDto);

        mockMvc.perform(get("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        final String REFRESH_TOKEN_KEY = "X-Refresh-Token";
        final UUID publicId = member.getPublicId();
        // String refreshToken = decode(getRefreshToken());
        Optional<Refresh> byRefreshAndPublicId = refreshJpaRepository.findByPublicId(publicId);
        String oldRefreshToken = byRefreshAndPublicId.get().getRefresh();

        Cookie cookie = createCookie(REFRESH_TOKEN_KEY, TOKEN_PREFIX + byRefreshAndPublicId.get().getRefresh() + "playHybe",  "/reissue",24 * 60 * 60, true);

        // when
        ResultActions resultActions = mockMvc.perform(post("/reissue")
                .cookie(cookie)
        );

        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("로그아웃 시 기존의 리프레시 토큰을 삭제한다.")
    public void logout_test() throws Exception {
        // given
        MemberLoginRequest memberLoginRequest = new MemberLoginRequest("net1506@naver.com", "12345");

        String loginRequestBody = om.writeValueAsString(memberLoginRequest);

        // 로그인 요청으로 리프레시 토큰 발급
        mockMvc.perform(get("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequestBody)
        );

        final String REFRESH_TOKEN_KEY = "X-Refresh-Token";
        final UUID publicId = member.getPublicId();

        Optional<Refresh> initialRefresh = refreshJpaRepository.findByPublicId(publicId);
        Assertions.assertTrue(initialRefresh.isPresent(), "로그인 후 리프레시 토큰이 저장 되었습니다.");

        // 쿠키 생성
        Cookie cookie = createCookie(REFRESH_TOKEN_KEY, URLEncoder.encode(TOKEN_PREFIX + initialRefresh.get().getRefresh(), StandardCharsets.UTF_8), "/reissue", 24 * 60 * 60, true);

        // when
        ResultActions resultActions = mockMvc.perform(post("/logout")
                .cookie(cookie)
        );

        // then
        resultActions.andExpect(status().isOk());

        // 로그아웃 후 리프레시 토큰 삭제 확인
        Optional<Refresh> afterLogoutRefresh = refreshJpaRepository.findByPublicId(publicId);
        assertThat(afterLogoutRefresh).isNotPresent();
    }

    private String getRefreshToken() {
        return URLEncoder.encode(TOKEN_PREFIX + jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, Duration.ofSeconds(30), UUID.fromString(publicId), USER.name(), ACTIVE.name()), StandardCharsets.UTF_8);
    }

    private String getRefreshToken(Duration duration) {
        return URLEncoder.encode(TOKEN_PREFIX + jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, duration, UUID.fromString(publicId), USER.name(), ACTIVE.name()), StandardCharsets.UTF_8);
    }

    private String getAccessToken() {
        return URLEncoder.encode(TOKEN_PREFIX + jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofSeconds(30), UUID.fromString(publicId), USER.name(), ACTIVE.name()), StandardCharsets.UTF_8);
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