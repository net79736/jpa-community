package com.jpacommunity.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class WebConfig {

    private final String[] ALLOWED_ORIGIN = {
            "http://localhost:3000",
            // "http://123.141.189.142:3000",
            "http://playhive.com:3000",
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

        source.registerCorsConfiguration("/**", config);

        // TODO: 타입 확인해보기
        return new CorsFilter(source);
    }
}
