package com.jpacommunity.common.repository;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.jpacommunity.security.dto.CustomUserDetails;
import java.util.Optional;
import java.util.UUID;

// 출처: https://javacpro.tistory.com/85 [버물리의 IT공부:티스토리]
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(null == authentication || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        //사용자 환경에 맞게 로그인한 사용자의 정보를 불러온다.
        CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
        UUID publicId = userDetails.getPublicId();

        return Optional.of(String.valueOf(publicId));
    }
}
