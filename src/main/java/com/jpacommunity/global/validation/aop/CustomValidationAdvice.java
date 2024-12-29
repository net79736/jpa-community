package com.jpacommunity.global.validation.aop;

import com.jpacommunity.global.exception.ErrorCode;
import com.jpacommunity.global.exception.JpaCommunityException;
import com.jpacommunity.global.exception.old.ValidationFailedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

import static com.jpacommunity.global.exception.ErrorCode.INVALID_PARAMETER;

@Aspect
@Component
public class CustomValidationAdvice {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {}
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {}
    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void deleteMapping() {}
    @Pointcut("postMapping() || putMapping() || deleteMapping()")
    public void validationPointcut() {}

    @Around("validationPointcut()")
    public Object validationAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs(); // JoinPoint의 매개변수
        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                BindingResult bindingResult = (BindingResult) arg;

                if (bindingResult.hasErrors()) {
                    Map<String, String> errorMap = new HashMap<>();

                    for (FieldError error : bindingResult.getFieldErrors()) {
                        System.out.println("error = " + error);
                        errorMap.put(error.getField(), error.getDefaultMessage());
                    }

                    throw new JpaCommunityException(INVALID_PARAMETER, errorMap);
                }
            }
        }
        return proceedingJoinPoint.proceed();
    }
}

/**
 * get, delete, post(body), put(body)
 */
