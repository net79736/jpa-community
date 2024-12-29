package com.jpacommunity.oauth2.config;

import com.jpacommunity.common.handler.exception.JpaCommunityException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import static com.jpacommunity.common.handler.exception.ErrorCode.UNSUPPORTED_OAUTH_PROVIDER;
import static com.jpacommunity.oauth2.constant.OAuth2ServiceProvider.*;

@Configuration
@RequiredArgsConstructor
public class OAuth2Config {

    private final OAuth2ClientProperties properties;

    public String getClientId(String provider) {
        switch (provider.toLowerCase()) {
            case DISCORD:
                return properties.getDiscord().getClientId();
            case NAVER:
                return properties.getNaver().getClientId();
            case KAKAO:
                return properties.getKakao().getClientId();
            case GOOGLE:
                return properties.getGoogle().getClientId();
            default:
                throw new JpaCommunityException(UNSUPPORTED_OAUTH_PROVIDER, provider);
        }
    }

    public String getClientSecret(String provider) {
        switch (provider.toLowerCase()) {
            case DISCORD:
                return properties.getDiscord().getClientSecret();
            case NAVER:
                return properties.getNaver().getClientSecret();
            case KAKAO:
                return properties.getKakao().getClientSecret();
            case GOOGLE:
                return properties.getGoogle().getClientSecret();
            default:
                throw new JpaCommunityException(UNSUPPORTED_OAUTH_PROVIDER, provider);
        }
    }
}
