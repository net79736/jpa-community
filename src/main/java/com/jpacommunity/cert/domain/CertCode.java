package com.jpacommunity.cert.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CertCode {
    private final String code;
    private final LocalDateTime expirationTime;
}
