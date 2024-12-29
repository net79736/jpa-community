package com.jpacommunity.security.handler;

import com.jpacommunity.jwt.repository.RefreshJpaRepository;
import com.jpacommunity.jwt.util.JwtProvider;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.jpacommunity.global.exception.ErrorCode.*;
import static com.jpacommunity.jwt.util.JwtProvider.REFRESH_TOKEN_KEY;
import static com.jpacommunity.jwt.util.JwtProvider.TOKEN_CATEGORY_REFRESH;
import static org.springframework.http.HttpMethod.POST;

public class LogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    final JwtProvider jwtProvider;
    final RefreshJpaRepository refreshJpaRepository;

    public LogoutSuccessHandler(JwtProvider jwtProvider, RefreshJpaRepository refreshJpaRepository) {
        this.jwtProvider = jwtProvider;
        this.refreshJpaRepository = refreshJpaRepository;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        logger.info("LogoutSuccessHandler onLogoutSuccess() 메서드를 실행하였습니다");

        String method = request.getMethod();
        if (!method.equals(POST.name())) {
            return;
        }

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            sendErrorResponse(response, EMPTY_COOKIE.getStatus(), "쿠키가 비어있습니다.");
            return;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(REFRESH_TOKEN_KEY)) {
                refresh = cookie.getValue();
            }
        }

        //refresh null check
        if (refresh == null) {
            sendErrorResponse(response, INVALID_REFRESH_TOKEN.getStatus(), "인증되지 않은 토큰");
            return;
        }

        refresh = jwtProvider.getAccessToken(URLDecoder.decode(refresh, StandardCharsets.UTF_8));

        //expired check
        logger.debug("===========================");
        logger.debug(refresh);
        logger.debug("===========================");
        try {
            Boolean expired = jwtProvider.isExpired(refresh);
            if (expired) {
                sendErrorResponse(response, REFRESH_TOKEN_EXPIRED.getStatus(), "만료된 토큰");
            }
        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, INVALID_TOKEN_TYPE.getStatus(), "잘못된 JWT 토큰 형식");
            return;
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtProvider.getCategory(refresh);
        if (!category.equals(TOKEN_CATEGORY_REFRESH)) {
            sendErrorResponse(response, INVALID_TOKEN_TYPE.getStatus(), "잘못된 JWT 토큰 형식");
            return;
        }

        UUID publicId = jwtProvider.getPublicId(refresh);

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshJpaRepository.existsByRefreshAndPublicId(refresh, publicId);
        if (!isExist) {
            sendErrorResponse(response, INVALID_REFRESH_TOKEN.getStatus(), "인증되지 않은 토큰");
            return;
        }

        //로그아웃 진행
        //Refresh 토큰 DB 에서 제거
        refreshJpaRepository.deleteByRefreshAndPublicId(refresh, publicId);

        // 1. Security Context 해제
        SecurityContextHolder.clearContext();

        //Refresh 토큰 Cookie 값 0
        Cookie cookie = new Cookie(REFRESH_TOKEN_KEY, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * 공통 에러 응답 처리 메서드
     *
     * @param response HttpServletResponse
     * @param httpStatus HTTP 상태 오브젝트
     * @param message 메시지
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus httpStatus, String message) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"message\":\"%s\",\"status\":\"%s\"}", message, httpStatus.name()));
    }
}
