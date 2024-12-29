package com.jpacommunity.security.filter;

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
                log.error("JWT 처리 중 오류 발생: {}", e.getMessage());
                filterChain.doFilter(request, response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
