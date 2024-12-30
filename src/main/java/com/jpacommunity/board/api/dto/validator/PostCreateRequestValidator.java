package com.jpacommunity.board.api.dto.validator;

import com.jpacommunity.board.api.dto.PostCreateRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PostCreateRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PostCreateRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PostCreateRequest request = (PostCreateRequest) target;

        if (request.getTitle() == null || request.getTitle().length() < 4) {
            errors.rejectValue("title", "field.min.length", "Title must be at least 4 characters long");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            errors.rejectValue("content", "field.required", "Content must not be empty");
        }
    }
}