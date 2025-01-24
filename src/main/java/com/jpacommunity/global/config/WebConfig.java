package com.jpacommunity.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

import static com.jpacommunity.jwt.util.JwtProvider.HEADER_AUTHORIZATION;
import static com.jpacommunity.jwt.util.JwtProvider.REFRESH_TOKEN_KEY;

@Configuration
public class WebConfig {

    private final String[] ALLOWED_ORIGIN = {
            "http://localhost:3000", // 실제 아이피 주소를 넣어서 CORS 처리할 수도 있음
            // "http://123.141.189.142:3000",
            "http://playhive.com:3000", // 프론트 서버 개발자는 host 파일에 localhost 를 http://playhive.com:3000 로 바꿔서 개발하면 된다.
    };


    protected WebConfig() {
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(ALLOWED_ORIGIN));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        config.addExposedHeader(HEADER_AUTHORIZATION);
        config.addExposedHeader(REFRESH_TOKEN_KEY);

        source.registerCorsConfiguration("/**", config);

        // TODO: 타입 확인해보기
        return new CorsFilter(source);
    }
}
