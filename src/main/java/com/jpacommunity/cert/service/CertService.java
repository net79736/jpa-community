package com.jpacommunity.cert.service;

import com.jpacommunity.cert.util.CertMailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertService {
    private final CertMailSender certMailSender;

    public void send(String email) {
        certMailSender.send(email);
    }

    public boolean certify(String email, String certificationCode) {
        certMailSender.print();
        return certMailSender.isCodeValid(email, certificationCode);
    }

    public String generateCertificationUrl(String email, String certificationCode) {
        return "http://localhost:8080/api/users/" + email + "/certify?certificationCode=" + certificationCode;
    }
}
