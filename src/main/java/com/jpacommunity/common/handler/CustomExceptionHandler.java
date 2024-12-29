package com.jpacommunity.common.handler;

import com.jpacommunity.common.handler.exception.*;
import com.jpacommunity.common.web.response.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.jpacommunity.common.web.response.ResponseStatus.FAIL;

@RestControllerAdvice
public class CustomExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    // API 익셉션 에러
    @ExceptionHandler(GeneralApiException.class)
    public ResponseEntity<?> apiException(GeneralApiException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(FAIL.getValue(), e.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    // 유저 권한 없음
    @ExceptionHandler(UserForbiddenException.class)
    public ResponseEntity<?> forbiddenException(GeneralApiException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(FAIL.getValue(), e.getMessage(), null), HttpStatus.FORBIDDEN);
    }

    // validate 익셉션
    @ExceptionHandler(ValidationFailedException.class)
    public ResponseEntity<?> validationApiException(ValidationFailedException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(FAIL.getValue(), e.getMessage(), e.getErrorMap()), HttpStatus.BAD_REQUEST);
    }

    // 리소스 중복 에러
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> duplicateResourceException(DuplicateResourceException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(FAIL.getValue(), e.getMessage(), null), HttpStatus.CONFLICT);
    }

    // 리소스를 찾을 수 없음
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> resourceNotFoundException(ResourceNotFoundException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(FAIL.getValue(), e.getMessage(), null), HttpStatus.NOT_FOUND);
    }

    // 잘못된 작업 요청
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<?> invalidOperationException(InvalidOperationException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(FAIL.getValue(), e.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    // 요청 제한 초과
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<?> rateLimitExceededException(RateLimitExceededException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(FAIL.getValue(), e.getMessage(), null), HttpStatus.TOO_MANY_REQUESTS);
    }

    // 잘못된 인자 전달
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgumentException(IllegalArgumentException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(FAIL.getValue(), e.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    // 데이터베이스 무결성 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> dataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(FAIL.getValue(), "데이터베이스 무결성 제약 조건 위반", null), HttpStatus.CONFLICT);
    }

    // 인증되지 않은 사용자 접근
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> unauthorizedException(UnauthorizedException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDto<>(FAIL.getValue(), e.getMessage(), null), HttpStatus.UNAUTHORIZED);
    }
}
