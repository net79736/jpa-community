package com.jpacommunity.oauth2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration")
public class OAuth2ClientProperties {
    private Discord discord;
    private Naver naver;
    private Kakao kakao;
    private Google google;

    @Getter
    @Setter
    public static class Discord {
        private String clientId;
        private String clientSecret;
    }

    @Getter
    @Setter
    public static class Naver {
        private String clientId;
        private String clientSecret;
    }

    @Getter
    @Setter
    public static class Kakao {
        private String clientId;
        private String clientSecret;
    }

    @Getter
    @Setter
    public static class Google {
        private String clientId;
        private String clientSecret;
    }
}
