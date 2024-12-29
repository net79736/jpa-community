package com.jpacommunity.common.handler;

import com.jpacommunity.common.handler.exception.JpaCommunityException;
import com.jpacommunity.common.web.response.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class CustomExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @ExceptionHandler(value = JpaCommunityException.class)
    public ResponseEntity<?> serviceException(JpaCommunityException e) {
        log.error("CustomExceptionHandler serviceException status: {}", e.getErrorCode().getStatus().value());
        log.error("CustomExceptionHandler serviceException message: {}", e.getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(
                        new ResponseDto<>(
                                e.getErrorCode().getStatus().value(),
                                e.getMessage(),
                                e.getErrorMap()
                        )
                );
    }
}
