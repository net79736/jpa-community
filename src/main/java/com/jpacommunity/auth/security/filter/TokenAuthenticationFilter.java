package com.jpacommunity.auth.security.filter;

import com.jpacommunity.jwt.util.JwtProvider;
import com.jpacommunity.member.domain.MemberRole;
import com.jpacommunity.member.domain.MemberStatus;
import com.jpacommunity.member.entity.Member;
import com.jpacommunity.security.dto.CustomUserDetails;
import io.jsonwebtoken.JwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.jpacommunity.jwt.util.JwtProvider.HEADER_AUTHORIZATION;
import static com.jpacommunity.jwt.util.JwtProvider.TOKEN_CATEGORY_ACCESS;

/**
 * 스프링 시큐리티 참고함
 * https://chaewsscode.tistory.com/233
 *
 * 모든 주소에서 동작함 (토큰 검증)
 * <p>
 * successfulAuthentication 메서드에서는 로그인 시 JWT를 생성하지만, 클라이언트가 서버에 요청할 때 이 토큰이 유효한지 검증하는 과정이 필요합니다.
 * JWTFilter는 이 검증 작업을 수행합니다.
 * <p>
 * 사용자가 로그인한 후, 이후의 요청에서 이 JWT를 사용하여 사용자 인증을 계속 유지해야 합니다.
 * JWTFilter가 각 요청을 가로채어 JWT의 유효성을 확인하고, 유효하다면 인증 정보를 SecurityContextHolder에 설정하는 역할을 합니다.
 * <p>
 * 즉, 로그인 과정에서 JWT를 생성하는 것은 첫 번째 단계이고, 그 이후의 요청에서 JWT의 유효성을 검사하고 인증 정보를 설정하는 것은 또 다른 중요한 단계입니다.
 * 이 두 과정이 함께 작동하여 전체적인 인증 흐름이 완성되는 것입니다.
 * <p>
 * 결론적으로, JWTFilter는 JWT의 유효성을 검증하고, 이를 통해 요청을 안전하게 처리하기 위한 필수적인 컴포넌트입니다.
 */
@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("Token Authenticate Filter 토큰을 검사중");
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        String accessToken = jwtProvider.getAccessToken(authorizationHeader);

        log.info("accessToken : " + accessToken);
        if (StringUtils.isNotBlank(accessToken)) {
            try {
                if (!jwtProvider.validToken(accessToken)) {
                    log.warn("인증되지 않은 토큰입니다");
                    filterChain.doFilter(request, response);
                    return;
                }

                String accessCategory = jwtProvider.getCategory(accessToken);
                if (!TOKEN_CATEGORY_ACCESS.equals(accessCategory)) {
                    log.warn("잘못된 토큰 유형입니다");
                    filterChain.doFilter(request, response);
                    return;
                }

                UUID publicId = jwtProvider.getPublicId(accessToken);
                String role = jwtProvider.getRole(accessToken);
                String status = jwtProvider.getStatus(accessToken);

                log.info("publicId : " + publicId);
                log.info("role : " + role);
                log.info("status : " + status);

                Member member = Member.builder()
                        .publicId(publicId)
                        .role(MemberRole.valueOf(role))
                        .status(MemberStatus.valueOf(status))
                        .build();

                CustomUserDetails customUserDetails = new CustomUserDetails(member);
                Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("SecurityContext 에 인증 정보 저장 완료");
            } catch (JwtException e) {
                // 회원 가입 시 토큰이 없으니 여기에 걸려 버렸음.
                // 그래도 다음 필터로 진행된 것 같음
                // 그리고 permitAll 을 실행시켰음. join 인 경우 Security Context 의 Authentication 객체 존재 여부를 체크하지 않았음.
                log.error("JWT 처리 중 오류 발생: {}", e.getMessage());
                filterChain.doFilter(request, response);
                return;
            }
            // ExceptionTranslationFilter 까지 가야지 permitAll 대상인 여부를 판단할 수 있다.
            // 예외가 터져서 캐치되지 못하는 상황이 오면 안된다.
            // 필터를 계속 거쳐 인증까지 완료되면 이제 Spring 의 엔드 포인트를 실행하게 된다.
        }
        filterChain.doFilter(request, response); // 다음 필터로 넘어가야 Security Context 에 인증 객체가 없을 때 처리하는 에러 핸들러를 만나서 에러를 터트림
    }
}
