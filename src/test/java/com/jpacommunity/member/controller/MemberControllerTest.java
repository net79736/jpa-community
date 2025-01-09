package com.jpacommunity.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpacommunity.member.domain.MemberStatus;
import com.jpacommunity.member.dto.create.MemberCreateRequest;
import com.jpacommunity.member.dto.update.MemberStatusUpdateRequest;
import com.jpacommunity.member.entity.Member;
import com.jpacommunity.member.repository.MemberJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.jpacommunity.jwt.util.JwtProvider;

import java.util.Optional;

import static com.jpacommunity.member.domain.MemberStatus.PENDING;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@SqlGroup({
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/category-insert-date.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
})
class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private MemberJpaRepository memberJpaRepository;
    @Autowired
    EntityManager em;
    @Autowired
    JwtProvider jwtUtil;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // token_invalid_test 함수에서 정보 받아서 테스트 하세요
    // END 는 자동 완성 후 커서 위치
    @Test
    @Disabled
    public void jwt_token_get_test() throws Exception {
        // given
        String jwtToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJteXRlYW0ub3JnIiwiaWF0IjoxNzM0Nzg4MDMyLCJleHAiOjE3MzQ3ODgwNTIsImlkIjoiMDZhZWJiODQtOWIwMy00ZWYzLTgwNzctOTRlOWY0NDc1YWNkIiwicm9sZSI6IlVTRVIifQ.2JgRyVV45jLovHzA-R8cs_Qq72PSen3Stp6nMV_IVESy4IEg8-tpAsx7DfzBnhuvXQ2quVyc5-w8LD08dZEe9w";
        MemberStatusUpdateRequest memberStatusUpdateRequest = MemberStatusUpdateRequest.builder()
                .email("net1506@naver.com")
                .status(PENDING)
                .build();
        String requestBody = om.writeValueAsString(memberStatusUpdateRequest);

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/members/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", jwtToken)
                )
                .andExpect(status().isOk());

        // then
    }

    // token_invalid_test 함수에서 정보 받아서 테스트 하세요
    // 남의 계정을 수정
    @Test
    @Disabled
    public void jwt_bad_token_update_test() throws Exception {
        // given
        String jwtToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJteXRlYW0ub3JnIiwiaWF0IjoxNzM0Nzg4MTUwLCJleHAiOjE3MzQ3OTExNTAsImlkIjoiMDZhZWJiODQtOWIwMy00ZWYzLTgwNzctOTRlOWY0NDc1YWNkIiwicm9sZSI6IlVTRVIifQ.gMBAJjr7gN6mg63cLu3CPAHmhdFm4TLm6kuK99oA1Jx4wSIwr1SpjXNw1-7paDftENuiahFPflQhseTqCWgZyQ";
        MemberStatusUpdateRequest memberStatusUpdateRequest = MemberStatusUpdateRequest.builder()
                .email("teamplayhybe@gmail.com")
                .status(PENDING)
                .build();
        String requestBody = om.writeValueAsString(memberStatusUpdateRequest);

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/members/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", jwtToken)
                )
                .andExpect(status().isForbidden());

        // then
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @Disabled
    public void status_print_test() throws Exception {
        // given
        MemberStatus memberStatus = MemberStatus.valueOf("pending");

        // when
        // then
        System.out.println(memberStatus);
    }

    @Test
    @DisplayName("사용자를 생성한다. 토큰을 전달하지 않으며 permitAll 여부에 따라 API 가 잘 작동 하는지 테스트 한다.")
    public void user_create_test() throws Exception {
        // given
        MemberCreateRequest memberSaveRequest = MemberCreateRequest.builder()
                .email("teamplayhybe@gmail.com")
                .nickname("jongwook")
                .password("jongwook234")
                .tel("01088876666")
                .build();
        String requestBody = om.writeValueAsString(memberSaveRequest);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/me/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                );

        // then
        resultActions.andExpect(status().isCreated());
        Optional<Member> byEmail = memberJpaRepository.findByEmail("teamplayhybe@gmail.com");
        byEmail.ifPresent(member -> memberJpaRepository.delete(member));
    }
}