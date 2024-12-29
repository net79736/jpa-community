package com.jpacommunity.cert.controller;

import com.jpacommunity.cert.dto.CertEmailRequest;
import com.jpacommunity.cert.service.CertService;
import com.jpacommunity.common.handler.exception.JpaCommunityException;
import com.jpacommunity.common.web.response.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.jpacommunity.common.handler.exception.ErrorCode.UNAUTHORIZED;
import static com.jpacommunity.common.web.response.ResponseStatus.SUCCESS;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/certification")
public class CertController {
    private final CertService certService;

    @PostMapping("/send")
    public ResponseEntity<?> sendCertificationEmail(@Valid @RequestBody CertEmailRequest certEmailRequest, BindingResult bindingResult) {
        log.info("send-certification email: {}", certEmailRequest.getEmail());
         certService.send(certEmailRequest.getEmail());
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "인증 코드 이메일 전송 성공", null), HttpStatus.OK);
    }

    @PostMapping("/certify-code")
    public ResponseEntity<?> certifyCode(@Valid @RequestBody CertEmailRequest certEmailRequest, BindingResult bindingResult) {
        String code = certEmailRequest.getCode(); // 인증 코드
        String email = certEmailRequest.getEmail(); // 이메일
        boolean isValid = certService.certify(email, code);

        if (isValid) {
            log.info("certify email: {} success", email);
            return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "인증 코드 확인", null), HttpStatus.OK);
        } else {
            log.info("certify code failed");
            log.info("email: {}, code: {}", email, code);
            throw new JpaCommunityException(UNAUTHORIZED);
        }
    }
}
